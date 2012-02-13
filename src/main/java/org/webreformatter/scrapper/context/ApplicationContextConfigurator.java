/**
 * 
 */
package org.webreformatter.scrapper.context;

import java.io.File;
import java.io.IOException;

import org.webreformatter.commons.adapters.CompositeAdapterFactory;
import org.webreformatter.commons.io.IOUtil;
import org.webreformatter.commons.strings.StringUtil.IVariableProvider;
import org.webreformatter.commons.uri.Uri;
import org.webreformatter.commons.xml.XmlException;
import org.webreformatter.resources.IWrfRepository;
import org.webreformatter.resources.IWrfResourceProvider;
import org.webreformatter.resources.impl.WrfRepositoryUtils;
import org.webreformatter.resources.impl.WrfResourceRepository;
import org.webreformatter.scrapper.protocol.CompositeProtocolHandler;
import org.webreformatter.scrapper.protocol.ProtocolHandlerUtils;
import org.webreformatter.scrapper.transformer.CompositeTransformer;
import org.webreformatter.scrapper.transformer.XslBasedDocumentTransformer;

/**
 * This class is the parent for all application-specific configurators.
 * 
 * @author kotelnikov
 */
public class ApplicationContextConfigurator {

    private CompositeAdapterFactory fAdaptersFactory = new CompositeAdapterFactory();

    private CompositeTransformer fDocumentTransformer = new CompositeTransformer();

    private IVariableProvider fPropertiesProvider = new IVariableProvider() {
        public String getValue(String name) {
            return System.getProperty(name);
        }
    };

    private CompositeProtocolHandler fProtocolHandler = new CompositeProtocolHandler();

    private String fRepositoryPath;

    private IWrfRepository fResourceRepository;

    private XslBasedDocumentTransformerFactory fXslBasedDocumentTransformerFactory;

    /**
     * 
     */
    public ApplicationContextConfigurator() {
        this("./data");
    }

    public ApplicationContextConfigurator(String repositoryPath) {
        fRepositoryPath = repositoryPath;
    }

    public void close() {
    }

    public ApplicationContext configureApplicationContext() {
        ApplicationContext applicationContext = new ApplicationContext(
            fAdaptersFactory)
            .setPropertyProvider(fPropertiesProvider)
            .setRepository(fResourceRepository)
            .setProtocolHandler(fProtocolHandler)
            .build();
        return applicationContext;
    }

    /**
     * @return the folder where all repository is stored
     */
    public String getRepositoryPath() {
        return fRepositoryPath;
    }

    protected void initDocumentTransformers() throws IOException, XmlException {
        AtomProcessing.register(fAdaptersFactory, fDocumentTransformer);
        IWrfResourceProvider resourceProvider = fResourceRepository
            .getResourceProvider("tmp", true);
        fXslBasedDocumentTransformerFactory = new XslBasedDocumentTransformerFactory(
            resourceProvider,
            fProtocolHandler);
    }

    protected void initProtocolHandlers() throws IOException {
        ProtocolHandlerUtils.registerDefaultProtocols(fProtocolHandler);
        DownloadAdapter.register(fAdaptersFactory);
    }

    protected void initResourceRepository() {
        File root = new File(getRepositoryPath());
        if (resetRepository()) {
            IOUtil.delete(root);
        }
        WrfResourceRepository repo = new WrfResourceRepository(
            fAdaptersFactory,
            root);
        WrfRepositoryUtils.registerDefaultResourceAdapters(fAdaptersFactory);
        fResourceRepository = repo;
    }

    protected void initXslDocumentTransformations()
        throws IOException,
        XmlException {
    }

    public void open() throws IOException, XmlException {
        initResourceRepository();
        initProtocolHandlers();
        initDocumentTransformers();
        initXslDocumentTransformations();
    }

    /**
     * Returns <code>true</code> if the whole content repository should be
     * destroyed while initialization. If this flag is <code>true</code> then
     * all already downloaded resources as well as all intermediate caches are
     * removed.
     * 
     * @return <code>true</code> if the whole content repository should be
     *         destroyed while initialization.
     */
    public boolean resetRepository() {
        return false;
    }

    public void setDocumentTransformer(
        String urlBase,
        XslBasedDocumentTransformer transformer) {
        Uri url = new Uri(urlBase);
        setDocumentTransformer(url, transformer);
    }

    public void setDocumentTransformer(
        Uri url,
        XslBasedDocumentTransformer transformer) {
        fDocumentTransformer.addTransformer(url, transformer);
    }

    public void setXslTransformation(String urlBase, String xslPath)
        throws IOException,
        XmlException {
        File xslFile = new File(xslPath).getAbsoluteFile();
        Uri xslUri = new Uri(xslFile.toURI() + "");
        XslBasedDocumentTransformer transformer = fXslBasedDocumentTransformerFactory
            .getTransformer(xslUri);
        setDocumentTransformer(urlBase, transformer);
    }
}
