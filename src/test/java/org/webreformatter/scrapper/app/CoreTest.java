/**
 * 
 */
package org.webreformatter.scrapper.app;

import java.io.ByteArrayInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.webreformatter.commons.events.EventManager;
import org.webreformatter.commons.events.IEventManager;
import org.webreformatter.commons.events.calls.CallEvent;
import org.webreformatter.commons.events.calls.CallListener;
import org.webreformatter.commons.uri.Path;
import org.webreformatter.commons.uri.Uri;
import org.webreformatter.resources.AbstractResourceTest;
import org.webreformatter.resources.IContentAdapter;
import org.webreformatter.resources.IWrfResource;
import org.webreformatter.resources.adapters.mime.MimeTypeAdapter;

/**
 * @author kotelnikov
 */
public class CoreTest extends AbstractResourceTest {

    public static class LoadContent
        extends
        CallEvent<LoadContent.Request, LoadContent.Response> {

        public static class Request {

            private IWrfResource fResource;

            private Uri fUri;

            public Request(String uri, IWrfResource resource) {
                this(new Uri(uri), resource);
            }

            public Request(Uri uri, IWrfResource resource) {
                fUri = uri;
                fResource = resource;
            }

            public IWrfResource getResource() {
                return fResource;
            }

            public Uri getUri() {
                return fUri;
            }

            public void setResource(IWrfResource resource) {
                fResource = resource;
            }

            public void setUri(Uri uri) {
                fUri = uri;
            }

        }

        public static class Response {

        }

        public LoadContent(Request request) {
            super(request);
        }

        public LoadContent(String uri, IWrfResource resource) {
            this(new Uri(uri), resource);
        }

        public LoadContent(Uri uri, IWrfResource resource) {
            super(new LoadContent.Request(uri, resource));
        }

        public IWrfResource getResource() {
            return getRequest().getResource();
        }

        public Uri getUri() {
            return getRequest().getUri();
        }
    }

    private final static Logger log = Logger
        .getLogger(CoreTest.class.getName());

    /**
     * @param name
     */
    public CoreTest(String name) {
        super(name);
    }

    private void handleError(String msg, Throwable e) {
        log.log(Level.FINE, msg, e);
    }

    // TODO:
    // * Create a composite XML reader where all SiteConfigs all created
    // recursively (handle "reference" tag in XML configurations for
    // SiteConfigs".
    // It is useful to build "composite" structures where a site
    // configuration
    // references other configurations on the net.

    // * siteConfig - configuration of files
    // * path - the path of the resource to call
    // * Map<String, List<String>> params - parameters
    // ** type - the type of the handler to use for the requested resource
    //
    // ContentHandlers for the "text/html" mime type:
    // * "clean" - just cleans up the content of the initial page to XHTML
    // and fixes all references (resolves them to absolute ones)
    // * "atom" (default) - transforms clean XHTML pages to Atom objects;
    // the feed contains meta-information about the page; The single entry
    // is used as a container for the page content.
    // * "myhandler" - a handler used to change the content of the Atom/XML
    // document and transform it to something else.
    //
    // ContentHandlers for the "text/html" mime type:
    //

    // ** IContentNormalizer - used to get content normalizer ???

    // MimeType HandlerType Description
    // "text/html" "zip" Loads the resources and all referenced items,
    // applies the
    // "text/html" "" Transforms the loaded HTML in an Atom feed entry
    // * "" Just copies the resource to the output
    public void test() throws Exception {
        IEventManager eventManager = new EventManager();
        eventManager.addListener(
            LoadContent.class,
            new CallListener<LoadContent>() {
                @Override
                protected void handleRequest(LoadContent event) {
                    try {
                        System.out.println(event.getUri());
                        IWrfResource resource = event.getResource();
                        IContentAdapter contentAdapter = resource
                            .getAdapter(IContentAdapter.class);
                        String str = "Hello, world!";
                        ByteArrayInputStream out = new ByteArrayInputStream(str
                            .getBytes("UTF-8"));
                        contentAdapter.writeContent(out);
                        LoadContent.Response response = new LoadContent.Response();
                        event.setResponse(response);
                    } catch (Throwable t) {
                        handleError("Can not write the test content", t);
                    }
                }
            });

        // Now we can call the loading...
        Path path = new Path("/abc/def/toto");
        IWrfResource resource = fResourceProvider.getResource(path, true);
        eventManager.fireEvent(new LoadContent(
            "http://www.foo.bar/test",
            resource), new CallListener<LoadContent>() {
            @Override
            protected void handleResponse(LoadContent event) {
                try {
                    IWrfResource resource = event.getResource();
                    MimeTypeAdapter mimeTypeAdapter = resource
                        .getAdapter(MimeTypeAdapter.class);
                    String mimeType = mimeTypeAdapter.getMimeType();
                    System.out.println("Result: " + mimeType);
                } catch (Throwable t) {
                    handleError(
                        "Can not get the mime type of the selected resource.",
                        t);
                }
            }
        });
        // call(uri, resource);

    }
}
