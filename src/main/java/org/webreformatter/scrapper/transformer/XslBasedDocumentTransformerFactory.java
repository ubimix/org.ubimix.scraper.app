package org.webreformatter.scrapper.transformer;

import java.io.IOException;

import org.webreformatter.commons.uri.Path;
import org.webreformatter.commons.uri.Uri;
import org.webreformatter.commons.uri.UriToPath;
import org.webreformatter.commons.xml.XmlException;
import org.webreformatter.commons.xml.XmlWrapper;
import org.webreformatter.resources.IWrfResource;
import org.webreformatter.resources.IWrfResourceProvider;
import org.webreformatter.resources.adapters.xml.XmlAdapter;
import org.webreformatter.scrapper.protocol.HttpStatusCode;
import org.webreformatter.scrapper.protocol.IProtocolHandler;

public class XslBasedDocumentTransformerFactory {

    public static XmlWrapper getXslDocument(
            IWrfResourceProvider resourceProvider,
            IProtocolHandler protocolHandler, Uri xslUrl) throws IOException,
            XmlException {
        XmlWrapper xsl = null;
        Path path = UriToPath.getPath(xslUrl);
        IWrfResource resource = resourceProvider.getResource(path, true);
        // FIXME:
        String login = null;
        String password = null;
        HttpStatusCode status = protocolHandler.handleRequest(xslUrl, login,
                password, resource);
        if (!status.isError() || HttpStatusCode.STATUS_304.equals(status)) {
            XmlAdapter xmlAdapter = resource.getAdapter(XmlAdapter.class);
            xsl = xmlAdapter.getWrapper();
        }
        return xsl;
    }

    private IProtocolHandler fProtocolHandler;

    private IWrfResourceProvider fResourceProvider;

    public XslBasedDocumentTransformerFactory(
            IWrfResourceProvider resourceProvider,
            IProtocolHandler protocolHandler) {
        fProtocolHandler = protocolHandler;
        fResourceProvider = resourceProvider;
    }

    public XslBasedDocumentTransformer getTransformer(Uri xslUri)
            throws IOException, XmlException {
        XmlWrapper xsl = getXslDocument(fResourceProvider, fProtocolHandler,
                xslUri);
        return new XslBasedDocumentTransformer(xsl);
    }
}