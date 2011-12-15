/**
 * 
 */
package org.webreformatter.scrapper.context;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.w3c.dom.Element;
import org.webreformatter.commons.uri.Uri;
import org.webreformatter.server.xml.XmlAcceptor;
import org.webreformatter.server.xml.XmlException;
import org.webreformatter.server.xml.XmlWrapper;
import org.webreformatter.server.xml.atom.AtomFeed;

/**
 * @author kotelnikov
 */
public class AbstractRecursiveContentLoader {

    public static class AtomLinkExtractor implements ILinkExtractor {

        public Set<Uri> extractLinks(RuntimeContext context)
            throws IOException,
            XmlException {
            Set<Uri> result = new HashSet<Uri>();
            String mime = context
                .getAdapter(DownloadAdapter.class)
                .getMimeType();
            if (mime.startsWith("text/html")) {
                AtomProcessing atomProcessing = context
                    .getAdapter(AtomProcessing.class);
                AtomFeed doc = atomProcessing.getResourceAsAtomFeed();
                if (doc != null) {
                    Set<Uri> set = extractLinks(context, doc);
                    result.addAll(set);
                }
            }
            return result;
        }

        protected Set<Uri> extractLinks(
            final RuntimeContext context,
            XmlWrapper xml) throws XmlException {
            final Set<Uri> references = new HashSet<Uri>();
            if (xml == null) {
                return references;
            }
            onXmlDocument(context, xml);
            Element node = xml.getRootElement();
            XmlAcceptor.accept(node, new XmlAcceptor.XmlVisitor() {
                @Override
                public void visit(Element node) {
                    Uri uri = getUri(context, node);
                    if (uri != null) {
                        references.add(uri);
                    }
                    super.visit(node);
                }

            });
            return references;
        }

        protected Uri getUri(RuntimeContext context, Element node) {
            Uri ref = null;
            String name = node.getNodeName();
            if ("a".equals(name)) {
                ref = getUrl(node, "href");
            } else if ("img".equals(name)) {
                ref = getUrl(node, "src");
            }
            return ref;
        }

        protected Uri getUrl(Element node, String attr) {
            Uri ref = null;
            String value = node.getAttribute(attr);
            if (value != null) {
                ref = new Uri(value);
            }
            return ref;
        }

        protected void onXmlDocument(RuntimeContext context, XmlWrapper xml)
            throws XmlException {
        }

    }

    public interface ILinkExtractor {

        Set<Uri> extractLinks(RuntimeContext context) throws Exception;

    }

    public interface IProgressListener {

        void beginDownload();

        void close();

        void endDownload();

        void onError(Throwable error);

        void open();

    }

    public interface IProgressListenerProvider {

        IProgressListener getListeners(RuntimeContext context);

    }

    public abstract static class ListenerGroup {

        private int fCounter;

        public Object fMutex = new Object();

        public ListenerGroup() {
            inc();
        }

        public IProgressListener add(final IProgressListener listener) {
            inc();
            return new IProgressListener() {

                public void beginDownload() {
                    listener.beginDownload();
                }

                public void close() {
                    try {
                        listener.close();
                    } finally {
                        dec();
                    }
                }

                public void endDownload() {
                    listener.endDownload();
                }

                public void onError(Throwable error) {
                    listener.onError(error);
                }

                public void open() {
                    listener.open();
                }
            };
        }

        public void close() {
            dec();
        }

        private void dec() {
            synchronized (getMutex()) {
                fCounter--;
                if (fCounter == 0) {
                    doClose();
                }
            }
        }

        protected abstract void doClose();

        protected Object getMutex() {
            return fMutex;
        }

        private void inc() {
            synchronized (getMutex()) {
                fCounter++;
            }
        }

        protected boolean isFinished() {
            synchronized (getMutex()) {
                return fCounter == 0;
            }
        }

    }

    public static class ProgressListener implements IProgressListener {

        public void beginDownload() {

        }

        public void close() {
        }

        public void endDownload() {
        }

        public void onError(Throwable error) {
        }

        public void open() {
        }

    }

    public static class ProgressListenerProvider extends ListenerGroup
        implements
        IProgressListenerProvider {

        public Set<RuntimeContext> fResults = new HashSet<RuntimeContext>();

        @Override
        public void close() {
            super.close();
            while (!isFinished()) {
                try {
                    Object mutex = getMutex();
                    synchronized (mutex) {
                        mutex.wait(100);
                    }
                } catch (InterruptedException e) {
                    break;
                }
            }
        }

        @Override
        protected void doClose() {
            Object mutex = getMutex();
            synchronized (mutex) {
                mutex.notifyAll();
            }
        }

        public IProgressListener getListeners(final RuntimeContext context) {
            return add(new ProgressListener() {

                private Set<Throwable> fErrors = new HashSet<Throwable>();

                @Override
                public void beginDownload() {
                }

                @Override
                public void endDownload() {
                    boolean ok = false;
                    if (fErrors.isEmpty()) {
                        try {
                            fResults.add(context);
                            ok = true;
                        } catch (Throwable t) {
                            onError(t);
                        }
                    }
                    println("Download '"
                        + context.getUrl()
                        + "' ... "
                        + (ok ? "OK" : "ERROR"));
                }

                @Override
                public void onError(Throwable error) {
                    fErrors.add(error);
                }

            });
        }

        public Set<RuntimeContext> getResults() {
            return fResults;
        }

        public void print(String msg) {
            System.out.print(msg);
        }

        public void println(String msg) {
            print(msg);
            print("\n");
        }

    }

    public static Uri getNormalizedUri(Uri uri) {
        if (uri.getFragment() != null) {
            uri = uri.getBuilder().setFragment(null).build();
        }
        return uri;
    }

    private ExecutorService fExecutor;

    private ILinkExtractor fLinkExtractor;

    private IProgressListenerProvider fListenerProvider;

    private RuntimeContext fParentContext;

    private Set<Uri> fUrlSet = Collections.synchronizedSet(new HashSet<Uri>());

    public AbstractRecursiveContentLoader(
        ExecutorService executor,
        RuntimeContext context,
        IProgressListenerProvider listenerProvider,
        ILinkExtractor linkExtractor) {
        fParentContext = context;
        fExecutor = executor;
        fListenerProvider = listenerProvider;
        fLinkExtractor = linkExtractor;
    }

    public AbstractRecursiveContentLoader(
        RuntimeContext context,
        IProgressListenerProvider listenerProvider,
        ILinkExtractor linkExtractor) {
        this(
            Executors.newCachedThreadPool(),
            context,
            listenerProvider,
            linkExtractor);
    }

    private void load(RuntimeContext context, final IProgressListener listener) {
        listener.open();
        Uri url = context.getUrl();
        if (fUrlSet.contains(url)) {
            listener.close();
        } else {
            fUrlSet.add(url);
            boolean ok = false;
            listener.beginDownload();
            try {
                DownloadAdapter downloadAdapter = context
                    .getAdapter(DownloadAdapter.class);
                try {
                    downloadAdapter.loadResource();
                    ok = downloadAdapter.isOK();
                } catch (Throwable t) {
                    listener.onError(t);
                }
            } finally {
                listener.endDownload();
            }
            ListenerGroup listenerGroup = new ListenerGroup() {
                @Override
                protected void doClose() {
                    listener.close();
                }
            };
            try {
                if (ok) {
                    Set<Uri> links = fLinkExtractor.extractLinks(context);
                    if (links != null && !links.isEmpty()) {
                        for (Uri childUri : links) {
                            childUri = getNormalizedUri(childUri);
                            RuntimeContext childContext = context
                                .newContext(childUri);
                            IProgressListener childListener = fListenerProvider
                                .getListeners(childContext);
                            submit(
                                childContext,
                                listenerGroup.add(childListener));
                        }
                    }
                }
            } catch (Throwable t) {
                listener.onError(t);
            } finally {
                listenerGroup.close();
            }
        }
    }

    public void load(Uri url) {
        url = getNormalizedUri(url);
        RuntimeContext context = fParentContext.newContext(url);
        IProgressListener listener = fListenerProvider.getListeners(context);
        submit(context, listener);
    }

    private void submit(
        final RuntimeContext context,
        final IProgressListener listener) {
        fExecutor.submit(new Runnable() {
            public void run() {
                load(context, listener);
            }
        });
    }

}
