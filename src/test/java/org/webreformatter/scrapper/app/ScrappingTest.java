/**
 * 
 */
package org.webreformatter.scrapper.app;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.webreformatter.commons.adapters.CompositeAdapterFactory;
import org.webreformatter.commons.events.IEventListener;
import org.webreformatter.commons.events.IEventManager;
import org.webreformatter.commons.events.calls.CallEvent;
import org.webreformatter.commons.events.calls.CallListener;
import org.webreformatter.commons.events.server.AsyncEventManager;
import org.webreformatter.commons.events.server.CallBarrier;
import org.webreformatter.commons.io.IOUtil;
import org.webreformatter.commons.uri.Path;
import org.webreformatter.commons.uri.Uri;
import org.webreformatter.commons.uri.UriToPath;
import org.webreformatter.resources.IWrfResource;
import org.webreformatter.resources.IWrfResourceProvider;
import org.webreformatter.resources.adapters.html.HTMLAdapter;
import org.webreformatter.resources.adapters.mime.MimeTypeAdapter;
import org.webreformatter.resources.impl.WrfRepositoryUtils;
import org.webreformatter.resources.impl.WrfResourceRepository;
import org.webreformatter.scrapper.protocol.ClasspathProtocolHandler;
import org.webreformatter.scrapper.protocol.CompositeProtocolHandler;
import org.webreformatter.scrapper.protocol.HttpProtocolHandler;
import org.webreformatter.scrapper.protocol.HttpStatusCode;
import org.webreformatter.scrapper.protocol.IProtocolHandler;
import org.webreformatter.scrapper.protocol.UrlBasedProtocolHandler;
import org.webreformatter.scrapper.transformer.IDocumentTransformer;
import org.webreformatter.scrapper.transformer.RegexCompositeTransformer;
import org.webreformatter.scrapper.transformer.XslBasedDocumentTransformerFactory;
import org.webreformatter.server.xml.XmlWrapper;
import org.webreformatter.server.xml.atom.AtomEntry;
import org.webreformatter.server.xml.atom.AtomFeed;

/**
 * @author kotelnikov
 */
public class ScrappingTest extends TestCase {

    // ApplicationContext
    // SessionContext
    // RequestContext

    // Configuration:
    // * URL => download URL
    // * URL => access credentials
    // * URL => local path (for export)
    // * URL => XSL URL (for XSL-based document normalization)
    //
    //

    public static class Container {
        protected Map<String, Object> fMap = new HashMap<String, Object>();

        public void copyFrom(Container container) {
            fMap.putAll(container.fMap);
        }

        @SuppressWarnings("unchecked")
        public <T> T getValue(String key) {
            Object value = fMap.get(key);
            return (T) value;
        }

        public void setValue(String key, Object value) {
            fMap.put(key, value);
        }
    }

    public static class LoadPage extends SimpleCall {

        public LoadPage() {
            super();
        }

        public AtomFeed getResultDocument() {
            Container response = getResponse();
            return response != null ? (AtomFeed) response.getValue("doc")
                    : null;
        }

        public Uri getUri() {
            return getRequest().getValue("url");
        }

        public LoadPage setResultDocument(AtomFeed feed) {
            Container response = new Container();
            response.setValue("doc", feed);
            setResponse(response);
            return this;
        }

        public <T extends LoadPage> T setUri(String uri) {
            Uri u = new Uri(uri);
            return setUri(u);
        }

        public <T extends LoadPage> T setUri(Uri uri) {
            getRequest().setValue("url", uri);
            return cast();
        }

    }

    public static class LoadResource extends SimpleCall {

        public LoadResource() {
            super();
        }

        public IWrfResource getResource() {
            return getRequest().getValue("resource");
        }

        public HttpStatusCode getResultStatus() {
            Container result = getResponse();
            return result != null ? (HttpStatusCode) result.getValue("status")
                    : null;
        }

        public Uri getUri() {
            return getRequest().getValue("url");
        }

        public <T extends LoadResource> T setResource(IWrfResource resource) {
            getRequest().setValue("resource", resource);
            return cast();
        }

        public void setResultStatus(HttpStatusCode status) {
            Container result = new Container();
            result.setValue("status", status);
            setResponse(result);
        }

        public <T extends LoadResource> T setUri(String uri) {
            Uri u = new Uri(uri);
            return setUri(u);
        }

        public <T extends LoadResource> T setUri(Uri uri) {
            getRequest().setValue("url", uri);
            return cast();
        }

    }

    public static class SimpleCall extends CallEvent<Container, Container> {

        public SimpleCall() {
            super(new Container());
        }

        public SimpleCall(SimpleCall call) {
            this();
            copyRequestFrom(call);
        }

        @SuppressWarnings("unchecked")
        public <T extends SimpleCall> T cast() {
            return (T) this;
        }

        public <T extends SimpleCall> T copyRequestFrom(SimpleCall call) {
            getRequest().copyFrom(call.getRequest());
            return cast();
        }
    }

    private final static Logger log = Logger.getLogger(ScrappingTest.class
            .getName());

    protected CompositeAdapterFactory fAdapters = new CompositeAdapterFactory();

    private IDocumentTransformer fNormalizer;

    private CompositeProtocolHandler fProtocolHandler = new CompositeProtocolHandler();

    private IWrfResourceProvider fResourceProvider;

    /**
     * @param name
     */
    public ScrappingTest(String name) {
        super(name);
    }

    private void handleError(String msg, Throwable e) {
        log.log(Level.FINE, msg, e);
    }

    protected IProtocolHandler newHttpProtocolHandler() throws IOException {
        return new HttpProtocolHandler();
    }

    @Override
    protected void setUp() throws Exception {
        File root = new File("./tmp");
        IOUtil.delete(root);
        WrfResourceRepository resourceRepository = new WrfResourceRepository(
                fAdapters, root);
        WrfRepositoryUtils.registerAdapters(resourceRepository);
        fResourceProvider = resourceRepository.getResourceProvider("download",
                true);

        IProtocolHandler handler = newHttpProtocolHandler();
        fProtocolHandler.setProtocolHandler("http", handler);
        fProtocolHandler.setProtocolHandler("https", handler);
        fProtocolHandler
                .setDefaultProtocolHandler(new UrlBasedProtocolHandler());
        fProtocolHandler.setProtocolHandler("classpath",
                new ClasspathProtocolHandler());

        IWrfResourceProvider xslResourceProvider = resourceRepository
                .getResourceProvider("xsl", true);
        RegexCompositeTransformer normalizer = new RegexCompositeTransformer();

        XslBasedDocumentTransformerFactory factory = new XslBasedDocumentTransformerFactory(
                xslResourceProvider, fProtocolHandler);
        normalizer.addTransformer("http://\\w+.wikipedia.org", factory
                .getTransformer(new Uri(
                        "classpath:/xsl/transform-wikipedia.xsl")));
        normalizer.addTransformer("^.*$",
                factory.getTransformer(new Uri("classpath:/xsl/main.xsl")));

        fNormalizer = normalizer;
    }

    public void test() throws Exception {
        IEventManager eventManager = new AsyncEventManager();
        eventManager.addListener(LoadResource.class,
                new CallListener<LoadResource>() {
                    @Override
                    protected void handleRequest(LoadResource event) {
                        if (event.hasResponse()) {
                            return;
                        }
                        String login = null;
                        String password = null;
                        Uri uri = event.getUri();
                        IWrfResource resource = event.getResource();
                        HttpStatusCode status = fProtocolHandler.handleRequest(
                                uri, login, password, resource);
                        event.setResultStatus(status);
                    }
                });
        eventManager.addListener(LoadPage.class, new CallListener<LoadPage>() {
            @Override
            protected void handleRequest(final LoadPage loadEvent) {
                IEventManager eventManager = loadEvent.getEventManager();
                Path path = UriToPath.getPath(loadEvent.getUri());
                IWrfResource resource = fResourceProvider.getResource(path,
                        true);
                eventManager.fireEvent(
                        new LoadResource().<LoadResource> copyRequestFrom(
                                loadEvent).setResource(resource),
                        new CallListener<LoadResource>() {
                            @Override
                            protected void handleResponse(LoadResource event) {
                                AtomFeed result = null;
                                if (event.hasErrors()) {
                                    Set<Throwable> errors = event.getErrors();
                                    for (Throwable error : errors) {
                                        event.onError(error);
                                    }
                                } else {
                                    try {
                                        HttpStatusCode status = event
                                                .getResultStatus();
                                        if (status.isError()) {
                                            throw new Exception(
                                                    "Required resource was not loaded.");
                                        }
                                        IWrfResource resource = event
                                                .getResource();
                                        MimeTypeAdapter mimeTypeAdapter = resource
                                                .getAdapter(MimeTypeAdapter.class);
                                        String mimeType = mimeTypeAdapter
                                                .getMimeType();
                                        if ("text/html".equals(mimeType)) {
                                            HTMLAdapter htmlAdapter = resource
                                                    .getAdapter(HTMLAdapter.class);
                                            XmlWrapper doc = htmlAdapter
                                                    .getWrapper();
                                            Uri url = event.getUri();
                                            result = fNormalizer
                                                    .transformDocument(url, doc);
                                        } else {
                                            throw new Exception(
                                                    "Not expected document mime type.");
                                        }
                                    } catch (Throwable t) {
                                        loadEvent.onError(t);
                                    }
                                }
                                loadEvent.setResultDocument(result);
                            }
                        });
            }
        });

        eventManager.addListener(LoadPage.class,
                new IEventListener<LoadPage>() {
                    public void handleEvent(LoadPage event) {
                        if (event.isRequestStage()) {
                            System.out.println("Start loading page: "
                                    + event.getUri());
                        } else {
                            System.out.println("Page loaded: " + event.getUri());
                        }
                    }
                });

        // Now we can call the loading...
        Uri uri = new Uri("http://www.mediawiki.org/");
        uri = new Uri("http://fr.wikipedia.org/wiki/France");
        CallBarrier barrier = new CallBarrier();

        eventManager.fireEvent(new LoadPage().setUri(uri),
                barrier.add(new CallListener<LoadPage>() {
                    @Override
                    protected void handleResponse(LoadPage event) {
                        try {
                            AtomFeed feed = event.getResultDocument();
                            List<AtomEntry> entries = feed.getEntries();
                            for (AtomEntry entry : entries) {
                                System.out.println("Page references:");
                                List<XmlWrapper> references = entry
                                        .evalList("./atom:content//html:a[@href]");
                                for (XmlWrapper ref : references) {
                                    String str = ref.getAttribute("href");
                                    System.out.println(str);
                                }
                            }
                        } catch (Throwable t) {
                            handleError(
                                    "Can not get the mime type of the selected resource.",
                                    t);
                        }
                    }
                }));
        barrier.await();

    }
}
