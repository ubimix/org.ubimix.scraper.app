/**
 * 
 */
package org.webreformatter.scrapper.app;

import java.io.IOException;
import java.io.InputStream;

import org.webreformatter.commons.events.EventManager;
import org.webreformatter.commons.events.IEventManager;
import org.webreformatter.commons.strings.StringUtil.IVariableProvider;
import org.webreformatter.commons.uri.Path;
import org.webreformatter.commons.uri.Uri;
import org.webreformatter.commons.uri.UriToPath;
import org.webreformatter.resources.AbstractResourceTest;
import org.webreformatter.resources.IContentAdapter;
import org.webreformatter.resources.IPropertyAdapter;
import org.webreformatter.resources.IWrfResource;
import org.webreformatter.resources.adapters.cache.CachedResourceAdapter;
import org.webreformatter.scrapper.context.ApplicationContext;
import org.webreformatter.scrapper.context.AtomProcessing;
import org.webreformatter.scrapper.context.DownloadAdapter;
import org.webreformatter.scrapper.normalizer.CompositeDocumentNormalizer;
import org.webreformatter.scrapper.normalizer.XslBasedContentNormalizer;
import org.webreformatter.scrapper.protocol.AccessManager;
import org.webreformatter.scrapper.protocol.CompositeProtocolHandler;
import org.webreformatter.scrapper.protocol.HttpStatusCode;
import org.webreformatter.scrapper.protocol.IProtocolHandler;

/**
 * @author kotelnikov
 */
public abstract class AbstractExectionContextTest extends AbstractResourceTest {

    protected AccessManager fAccessManager = new AccessManager();

    protected IEventManager fEventManager = new EventManager();

    protected IVariableProvider fPropertyProvider = new IVariableProvider() {
        public String getValue(String name) {
            return System.getProperty(name);
        }
    };

    protected CompositeProtocolHandler fProtocolHandler = new CompositeProtocolHandler();

    public AbstractExectionContextTest(String name) {
        super(name);
    }

    protected void addDocumentNormalizers(
        CompositeDocumentNormalizer documentNormalizer) {
        documentNormalizer.addNormalizer(new XslBasedContentNormalizer());
    }

    private HttpStatusCode copyClasspathResource(
        Path path,
        IWrfResource resource) {
        try {
            IContentAdapter contentAdapter = resource
                .getAdapter(IContentAdapter.class);
            path = path.getBuilder().makeAbsolutePath().build();
            InputStream input = getClass().getResourceAsStream(path.toString());
            try {
                contentAdapter.writeContent(input);
            } finally {
                input.close();
            }
            CachedResourceAdapter cacheAdapter = resource
                .getAdapter(CachedResourceAdapter.class);
            cacheAdapter.touch();

            IPropertyAdapter properties = resource
                .getAdapter(IPropertyAdapter.class);
            properties.setProperty("Content-Type", "text/html");
            return HttpStatusCode.STATUS_200;
        } catch (IOException e) {
            return HttpStatusCode.STATUS_505;
        }
    }

    protected ApplicationContext newApplicationContext() {
        ApplicationContext.Builder builder = ApplicationContext
            .builder(fAdapters)
            .setRepository(fResourceRepository)
            .setPropertyProvider(fPropertyProvider)
            .setEventManager(fEventManager)
            .setProtocolHandler(fProtocolHandler);
        return builder.build();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        // FIXME_HERE
        String configStr = ""
            + "<pageset>\n"
            + "     <site path=\"abc\""
            + "             baseUrl=\"http://en.wikipedia.org/wiki/\" >\n"
            + ""
            + "        <url2path from=\"^(.*)$\" to=\"$1.html\" />\n"
            + "        <path2url from=\"^(.*)\\.html$\" to=\"$1\" />\n"
            + ""
            + "        <url key=\"xsl\" value=\"classpath:/xsl/transform-wikipedia.xsl\" />\n"
            + "        <url key=\"test\" value=\"http://www.foo.bar/\" />\n"
            + ""
            + "     </site>"
            + "</pageset>";

        fProtocolHandler.setProtocolHandler(
            "classpath",
            new IProtocolHandler() {
                public HttpStatusCode handleRequest(
                    Uri uri,
                    String login,
                    String password,
                    IWrfResource resource) {
                    Path path = uri.getPath();
                    return copyClasspathResource(path, resource);
                }
            });
        fProtocolHandler.setDefaultProtocolHandler(new IProtocolHandler() {
            public HttpStatusCode handleRequest(
                Uri uri,
                String login,
                String password,
                IWrfResource resource) {
                Path path = UriToPath.getPath(uri);
                path = path.getBuilder().setFileName("content.txt").build();
                return copyClasspathResource(path, resource);
            }
        });

        CompositeDocumentNormalizer documentNormalizer = new CompositeDocumentNormalizer();
        addDocumentNormalizers(documentNormalizer);
        AtomProcessing.register(fAdapters, documentNormalizer);
        DownloadAdapter.register(fAdapters);

        // XmlUrlMapperLoader loader = new XmlUrlMapperLoader();
        // XmlContext context = XmlContext.build();
        // XmlWrapper xml = context.readXML(configStr);
        // loader.configure(xml, fUrlMapper);

    }

}
