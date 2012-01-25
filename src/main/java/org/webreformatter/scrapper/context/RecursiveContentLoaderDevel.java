package org.webreformatter.scrapper.context;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Element;
import org.webreformatter.commons.uri.Uri;
import org.webreformatter.resources.IWrfResource;
import org.webreformatter.server.xml.XmlAcceptor;
import org.webreformatter.server.xml.XmlException;
import org.webreformatter.server.xml.XmlWrapper;
import org.webreformatter.server.xml.atom.AtomFeed;

/**
 * @author kotelnikov
 */
public class RecursiveContentLoaderDevel {

    public interface IProgressListener<T> {

        void beginDownload();

        void close();

        void endDownload(T result);

        void open(Uri url);

    }

    public interface IProgressListenerProvider<T> {

        Map<Uri, IProgressListener<T>> getListeners(
            IProgressListener<?> parentListener,
            Uri documentUri,
            Collection<Uri> referenceUri);

    }

    public static class ProgressListenerProvider<T>
        implements
        IProgressListenerProvider<T> {

        public interface ITracePrinter {

            void print(String message);

            void println(String message);

        }

        private class ProgressListener implements IProgressListener<T> {

            private ProgressListener fParent;

            private int fShift;

            private Uri fUri;

            public ProgressListener(ProgressListener parent) {
                fParent = parent;
            }

            public void beginDownload() {
                printShift();
                fPrinter.print("Download... ");
            }

            public void close() {
                dec();
                printShift();
                fPrinter.println("Close '" + fUri + "'.");
            }

            public void dec() {
                fShift--;
            }

            public void endDownload(T result) {
                fResults.put(fUri, result);
                fPrinter.println(" OK");
            }

            public void inc() {
                fShift++;
            }

            public void open(Uri url) {
                fUri = url;
                printShift();
                fPrinter.println("Open '" + fUri + "'.");
                inc();
            }

            public void printShift() {
                if (fParent != null) {
                    fParent.printShift();
                }
                for (int i = 0; i < fShift; i++) {
                    fPrinter.print("  ");
                }
            }
        }

        public static class TracePrinter implements ITracePrinter {

            public void print(String msg) {
                System.out.print(msg);
            }

            public void println(String msg) {
                print(msg);
                print("\n");
            }

        }

        private ITracePrinter fPrinter;

        private Map<Uri, T> fResults = new HashMap<Uri, T>();

        public ProgressListenerProvider(ITracePrinter printer) {
            fPrinter = printer;
        }

        protected boolean allowed(Uri documentUri, Uri referenceUri) {
            return true;
        }

        public Map<Uri, IProgressListener<T>> getListeners(
            IProgressListener<?> parentListener,
            Uri documentUri,
            Collection<Uri> references) {
            Map<Uri, IProgressListener<T>> result = new LinkedHashMap<Uri, IProgressListener<T>>();
            for (Uri referenceUri : references) {
                if (allowed(documentUri, referenceUri)) {
                    IProgressListener<T> listener = newListener(
                        parentListener,
                        documentUri,
                        referenceUri);
                    result.put(referenceUri, listener);
                }
            }
            return result;
        }

        public Map<Uri, T> getResults() {
            return fResults;
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        protected IProgressListener<T> newListener(
            IProgressListener<?> parentListener,
            Uri documentUri,
            Uri referenceUri) {
            ProgressListener listener = (parentListener instanceof ProgressListenerProvider.ProgressListener)
                ? (ProgressListenerProvider.ProgressListener) parentListener
                : null;
            return new ProgressListener(listener);
        }
    }

    public static Uri getNormalizedUri(Uri uri) {
        if (uri.getFragment() != null) {
            uri = uri.getBuilder().setFragment(null).build();
        }
        return uri;
    }

    private IProgressListenerProvider<AtomFeed> fDocumentListenerProvider;

    private RuntimeContext fParentContext;

    private IProgressListenerProvider<IWrfResource> fResourceListenerProvider;

    public RecursiveContentLoaderDevel(
        RuntimeContext parentContext,
        IProgressListenerProvider<AtomFeed> documentListenerProvider,
        IProgressListenerProvider<IWrfResource> resourceListenerProvider) {
        fParentContext = parentContext;
        fDocumentListenerProvider = documentListenerProvider;
        fResourceListenerProvider = resourceListenerProvider;
    }

    protected boolean addToMap(
        String type,
        final Map<String, Set<Uri>> map,
        Uri uri) {
        Set<Uri> set = map.get(type);
        if (set == null) {
            set = new HashSet<Uri>();
            map.put(type, set);
        }
        uri = getNormalizedUri(uri);
        return set.add(uri);
    }

    public void loadRecursively(List<Uri> urls)
        throws XmlException,
        IOException {
        Set<Uri> set = new HashSet<Uri>();
        for (int i = 0; i < urls.size(); i++) {
            Uri url = urls.get(i);
            url = getNormalizedUri(url);
            urls.set(i, url);
        }
        Map<Uri, IProgressListener<AtomFeed>> listeners = fDocumentListenerProvider
            .getListeners(null, null, urls);
        loadRecursively(set, listeners);
    }

    private void loadRecursively(
        Set<Uri> set,
        Map<Uri, IProgressListener<AtomFeed>> listeners)
        throws XmlException,
        IOException {
        for (Map.Entry<Uri, IProgressListener<AtomFeed>> entry : listeners
            .entrySet()) {
            Uri url = entry.getKey();
            url = getNormalizedUri(url);
            if (set.contains(url)) {
                continue;
            }
            set.add(url);
            IProgressListener<AtomFeed> listener = entry.getValue();
            listener.open(url);
            try {
                AtomFeed doc = null;
                listener.beginDownload();
                try {
                    RuntimeContext context = fParentContext.newContext(url);
                    AtomProcessing atomProcessing = context
                        .getAdapter(AtomProcessing.class);
                    doc = atomProcessing.getResourceAsAtomFeed();

                    if (doc == null) {
                        return;
                    }
                } finally {
                    listener.endDownload(doc);
                }
                Set<Uri> documentReferences = new HashSet<Uri>();
                Set<Uri> resourceReferences = new HashSet<Uri>();
                visit(doc, documentReferences, resourceReferences);
                Map<Uri, IProgressListener<AtomFeed>> docListeners = fDocumentListenerProvider
                    .getListeners(listener, url, documentReferences);
                Map<Uri, IProgressListener<IWrfResource>> resourceListeners = fResourceListenerProvider
                    .getListeners(listener, url, resourceReferences);

                loadRecursively(set, docListeners);
                loadResources(set, resourceListeners);
            } finally {
                listener.close();
            }
        }
    }

    public void loadRecursively(Uri... urls) throws XmlException, IOException {
        loadRecursively(Arrays.asList(urls));
    }

    private void loadResources(
        Set<Uri> set,
        Map<Uri, IProgressListener<IWrfResource>> listeners) throws IOException {
        for (Map.Entry<Uri, IProgressListener<IWrfResource>> entry : listeners
            .entrySet()) {
            Uri url = entry.getKey();
            url = getNormalizedUri(url);
            if (set.contains(url)) {
                continue;
            }
            set.add(url);
            IProgressListener<IWrfResource> listener = entry.getValue();
            listener.open(url);
            try {
                IWrfResource resource = null;
                listener.beginDownload();
                try {
                    RuntimeContext context = fParentContext.newContext(url);
                    DownloadAdapter downloadAdapter = context
                        .getAdapter(DownloadAdapter.class);
                    resource = downloadAdapter.loadResource();
                } finally {
                    listener.endDownload(resource);
                }
            } finally {
                listener.close();
            }
        }
    }

    private void visit(
        XmlWrapper xml,
        final Set<Uri> documentReferences,
        final Set<Uri> resourceReferences) throws XmlException {
        if (xml == null) {
            return;
        }
        Element node = xml.getRootElement();
        XmlAcceptor.accept(node, new XmlAcceptor.XmlVisitor() {

            private void addUrl(Element node, String attr, Set<Uri> set) {
                String value = node.getAttribute(attr);
                if (value != null) {
                    Uri ref = new Uri(value);
                    set.add(ref);
                }
            }

            @Override
            public void visit(Element node) {
                String name = node.getNodeName();
                if ("a".equals(name)) {
                    addUrl(node, "href", documentReferences);
                } else if ("img".equals(name)) {
                    addUrl(node, "src", resourceReferences);
                } else {
                    super.visit(node);
                }
            }
        });
    }

}