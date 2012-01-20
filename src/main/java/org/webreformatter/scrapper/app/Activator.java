/**
 * 
 */
package org.webreformatter.scrapper.app;

import java.io.File;
import java.io.IOException;
import java.util.Dictionary;
import java.util.Map;

import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.webreformatter.commons.adapters.AdapterFactoryUtils;
import org.webreformatter.commons.adapters.CompositeAdapterFactory;
import org.webreformatter.commons.events.IEventManager;
import org.webreformatter.commons.events.server.AsyncEventManager;
import org.webreformatter.commons.osgi.ConfigurableMultiserviceActivator;
import org.webreformatter.commons.osgi.OSGIObjectActivator;
import org.webreformatter.commons.osgi.OSGIObjectDeactivator;
import org.webreformatter.commons.osgi.OSGIService;
import org.webreformatter.commons.osgi.OSGIServiceActivator;
import org.webreformatter.commons.osgi.OSGIServiceDeactivator;
import org.webreformatter.commons.strings.StringUtil;
import org.webreformatter.commons.strings.StringUtil.IVariableProvider;
import org.webreformatter.commons.uri.Uri;
import org.webreformatter.pageset.PageSetConfig;
import org.webreformatter.pageset.PageSetConfigRegistry;
import org.webreformatter.resources.IWrfRepository;
import org.webreformatter.resources.impl.WrfRepositoryUtils;
import org.webreformatter.resources.impl.WrfResourceRepository;
import org.webreformatter.scrapper.context.ApplicationContext;
import org.webreformatter.scrapper.context.AtomProcessing;
import org.webreformatter.scrapper.context.DownloadAdapter;
import org.webreformatter.scrapper.context.PageSetConfigLoader;
import org.webreformatter.scrapper.events.ApplyAction;
import org.webreformatter.scrapper.events.ApplyActionHandler;
import org.webreformatter.scrapper.events.CallImage;
import org.webreformatter.scrapper.events.CallImageHandler;
import org.webreformatter.scrapper.events.CopyResourceAction;
import org.webreformatter.scrapper.events.CopyResourceHandler;
import org.webreformatter.scrapper.events.FormatHtmlAction;
import org.webreformatter.scrapper.events.FormatHtmlHandler;
import org.webreformatter.scrapper.events.GetAtomFeed;
import org.webreformatter.scrapper.events.GetAtomFeedHandler;
import org.webreformatter.scrapper.events.ZipExportAction;
import org.webreformatter.scrapper.events.ZipExportHandler;
import org.webreformatter.scrapper.normalizer.CompositeDocumentNormalizer;
import org.webreformatter.scrapper.normalizer.IDocumentNormalizer;
import org.webreformatter.scrapper.normalizer.XslBasedContentNormalizer;
import org.webreformatter.scrapper.protocol.CompositeProtocolHandler;
import org.webreformatter.scrapper.protocol.ProtocolHandlerUtils;
import org.webreformatter.server.mime.IMimeTypeDetector;
import org.webreformatter.server.mime.MimeTypeDetector;

/**
 * @author kotelnikov
 */
public class Activator extends ConfigurableMultiserviceActivator {

    private CompositeAdapterFactory fAdapterFactory = new CompositeAdapterFactory();

    private ApplicationContext fApplicationContext;

    private CompositeDocumentNormalizer fDocumentNormalizers = new CompositeDocumentNormalizer();

    private boolean fEventHandlerActivated;

    private IEventManager fEventManager;

    private HttpService fHttpService;

    private IMimeTypeDetector fMimeDetector;

    private PageSetConfigRegistry fPageSetConfigRegistry = new PageSetConfigRegistry();

    private String fPath;

    IVariableProvider fPropertyProvider = new StringUtil.IVariableProvider() {
        public String getValue(String name) {
            String value = (String) fProperties.get(name);
            if (value == null) {
                value = System.getProperty(name);
            }
            return value;
        }
    };

    private CompositeProtocolHandler fProtocolHandler;

    private IWrfRepository fRepository;

    private String fResourcePath;

    /**
     * 
     */
    public Activator() {
    }

    @OSGIObjectActivator
    public void activate() throws Exception {
        fMimeDetector = new MimeTypeDetector();

        IEventManager eventManager = getEventManager();

        IWrfRepository repository = getRepository();

        // Register adapters for the ExecutionContext
        AtomProcessing.register(fAdapterFactory, fDocumentNormalizers);
        DownloadAdapter.register(fAdapterFactory);

        // Register adapters for the ApplicationContext
        AdapterFactoryUtils.registerAdapter(
            fAdapterFactory,
            ApplicationContext.class,
            PageSetConfigLoader.class);

        CompositeProtocolHandler protocolHandler = getProtocolHandler();

        fApplicationContext = ApplicationContext
            .builder(fAdapterFactory)
            .setRepository(repository)
            .setPropertyProvider(fPropertyProvider)
            .setEventManager(eventManager)
            .setProtocolHandler(protocolHandler)
            .build();

        HttpContext httpContext = fHttpService.createDefaultHttpContext();

        fResourcePath = getProperty("web.resources.path", "/resources/*");
        String dirName = getProperty("web.resources.dir", "./");
        File dir = new File(dirName);
        ResourceServlet resourceServlet = new ResourceServlet(
            dir,
            fMimeDetector);
        fHttpService.registerServlet(
            fResourcePath,
            resourceServlet,
            fProperties,
            httpContext);

        ReformatServlet servlet = new ReformatServlet(
            fPageSetConfigRegistry,
            fApplicationContext);
        fPath = getProperty("web.path", "/*");
        fHttpService.registerServlet(fPath, servlet, fProperties, httpContext);

        registerEventHandlers(fApplicationContext);
    }

    @OSGIServiceActivator(min = 0)
    public void addContentNormalizer(IDocumentNormalizer normalizer) {
        fDocumentNormalizers.addNormalizer(normalizer);
    }

    @OSGIServiceActivator(min = 0)
    public void addPageSetConfig(PageSetConfig config, Map<String, String> map) {
        String key = map.get("key");
        if (key == null) {
            key = "";
        }
        fPageSetConfigRegistry.registerPageSetConfig(key, config);
    }

    @Override
    protected boolean checkPropertiesModifications(Dictionary<?, ?> properties) {
        return !equals(fProperties, properties);
    }

    @OSGIObjectDeactivator
    public void deactivate() {
        if (fPath != null) {
            fHttpService.unregister(fPath);
            fPath = null;
        }
        if (fResourcePath != null) {
            fHttpService.unregister(fResourcePath);
            fResourcePath = null;
        }
    }

    private boolean equals(Object first, Object second) {
        return first == null || second == null ? first == second : first
            .equals(second);
    }

    protected Uri getConfigUri(String configKey, String defaultUri) {
        String str = getProperty(configKey, defaultUri);
        Uri uri = new Uri(str);
        return uri;
    }

    @OSGIService
    public ApplicationContext getContext() {
        return fApplicationContext;
    }

    private IEventManager getEventManager() {
        if (fEventManager == null) {
            fEventManager = new AsyncEventManager();
        }
        return fEventManager;
    }

    private String getProperty(String key, String defaultValue) {
        String value = StringUtil.resolvePropertyByKey(key, fPropertyProvider);
        if (value == null) {
            value = StringUtil.resolveProperty(defaultValue, fPropertyProvider);
        }
        return value;
    }

    protected CompositeProtocolHandler getProtocolHandler() throws IOException {
        if (fProtocolHandler == null) {
            fProtocolHandler = new CompositeProtocolHandler();
            ProtocolHandlerUtils.registerDefaultProtocols(fProtocolHandler);
        }
        return fProtocolHandler;
    }

    private IWrfRepository getRepository() {
        if (fRepository == null) {
            String dir = getProperty("store.dir", "${user.home}/.scrapper/data");
            File rootDir = new File(dir);
            WrfResourceRepository repository = new WrfResourceRepository(
                fAdapterFactory,
                rootDir);
            WrfRepositoryUtils.registerAdapters(repository);
            fRepository = repository;

        }
        return fRepository;
    }

    @Override
    protected String getServiceID() {
        return "org.webreformatter.scrapper";
    }

    @OSGIService
    public IDocumentNormalizer getXslDocumentNormalizer() {
        return new XslBasedContentNormalizer();
    }

    protected void registerEventHandlers(ApplicationContext applicationContext) {
        if (!fEventHandlerActivated) {
            IEventManager eventManager = applicationContext.getEventManager();
            eventManager.addListener(ApplyAction.class, new ApplyActionHandler(
                applicationContext));
            eventManager.addListener(
                CopyResourceAction.class,
                new CopyResourceHandler());
            eventManager.addListener(
                FormatHtmlAction.class,
                new FormatHtmlHandler());
            eventManager.addListener(GetAtomFeed.class, new GetAtomFeedHandler(
                fDocumentNormalizers));
            eventManager.addListener(
                ZipExportAction.class,
                new ZipExportHandler());
            eventManager.addListener(CallImage.class, new CallImageHandler());
            fEventHandlerActivated = true;
        }
    }

    @OSGIServiceDeactivator
    public void removeContentNormalizer(IDocumentNormalizer normalizer) {
        fDocumentNormalizers.removeNormalizer(normalizer);
    }

    @OSGIServiceDeactivator
    public void removeService(HttpService service) {
        fHttpService = null;
    }

    @OSGIServiceActivator
    public void setService(HttpService service) {
        fHttpService = service;
    }

}
