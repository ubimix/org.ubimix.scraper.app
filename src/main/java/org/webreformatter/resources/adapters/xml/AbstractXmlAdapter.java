/**
 * 
 */
package org.webreformatter.resources.adapters.xml;

import java.io.IOException;
import java.io.OutputStream;

import org.w3c.dom.Document;
import org.webreformatter.resources.IContentAdapter;
import org.webreformatter.resources.IContentAdapter.ContentChangeEvent;
import org.webreformatter.resources.IWrfResource;
import org.webreformatter.resources.WrfResourceAdapter;
import org.webreformatter.server.xml.XmlException;
import org.webreformatter.server.xml.XmlWrapper;
import org.webreformatter.server.xml.XmlWrapper.CompositeNamespaceContext;
import org.webreformatter.server.xml.XmlWrapper.ElementBasedNamespaceContext;
import org.webreformatter.server.xml.XmlWrapper.XmlContext;

/**
 * @author kotelnikov
 */
public abstract class AbstractXmlAdapter extends WrfResourceAdapter {

    private CompositeNamespaceContext fNamespaceContext = new CompositeNamespaceContext();

    private ElementBasedNamespaceContext fNamespaceElementContext;

    private XmlWrapper fWrapper;

    private XmlContext fXmlContext = XmlContext
        .builder(fNamespaceContext)
        .build();

    public AbstractXmlAdapter(IWrfResource resource) {
        super(resource);
    }

    public XmlWrapper applyXSLT(IWrfResource resource)
        throws IOException,
        XmlException {
        XmlWrapper result = applyXSLT(resource, XmlWrapper.class);
        return result;
    }

    public <T extends XmlWrapper> T applyXSLT(
        IWrfResource xslResource,
        Class<T> type) throws IOException, XmlException {
        XmlWrapper wrapper = getWrapper();
        XmlAdapter adapter = xslResource.getAdapter(XmlAdapter.class);
        XmlWrapper xsl = adapter.getWrapper();
        T result = wrapper.applyXSL(xsl, type);
        return result;
    }

    public void applyXSLT(IWrfResource xslResource, IWrfResource targetResource)
        throws Exception {
        IContentAdapter content = targetResource
            .getAdapter(IContentAdapter.class);
        OutputStream out = content.getContentOutput();
        try {
            XmlWrapper wrapper = getWrapper();
            XmlAdapter adapter = xslResource.getAdapter(XmlAdapter.class);
            XmlWrapper xsl = adapter.getWrapper();
            wrapper.applyXSL(xsl, out);
        } finally {
            out.close();
        }
    }

    public XmlWrapper getWrapper() throws IOException, XmlException {
        if (fWrapper == null) {
            Document doc = readDocument();
            updateDocumentNamespaceContext(doc);
            XmlContext context = getXmlContext();
            fWrapper = new XmlWrapper(doc, context);
        }
        return fWrapper;
    }

    public <T extends XmlWrapper> T getWrapper(Class<T> type)
        throws IOException,
        XmlException {
        XmlWrapper wrapper = getWrapper();
        return wrapper.to(type);
    }

    public XmlContext getXmlContext() {
        return fXmlContext;
    }

    /**
     * @see org.webreformatter.resources.WrfResourceAdapter#handleEvent(java.lang.Object)
     */
    @Override
    public void handleEvent(Object event) {
        synchronized (this) {
            if (event instanceof ContentChangeEvent) {
                updateDocumentNamespaceContext(null);
                fWrapper = null;
            }
        }
    }

    protected abstract Document readDocument() throws IOException, XmlException;

    public void setDocument(XmlWrapper doc) throws IOException, XmlException {
        IContentAdapter content = fResource.getAdapter(IContentAdapter.class);
        OutputStream output = content.getContentOutput();
        try {
            doc.serializeXML(output, true);
        } finally {
            output.close();
        }
        fWrapper = null;
    }

    public void updateDocumentNamespaceContext(Document doc) {
        if (fNamespaceElementContext != null) {
            fNamespaceContext.removeContext(fNamespaceElementContext);
            fNamespaceElementContext = null;
        }
        if (doc != null) {
            fNamespaceElementContext = new ElementBasedNamespaceContext(doc);
            fNamespaceContext.addContext(fNamespaceElementContext);
        }
    }

}
