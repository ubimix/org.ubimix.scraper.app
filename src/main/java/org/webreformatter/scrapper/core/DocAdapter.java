/**
 * 
 */
package org.webreformatter.scrapper.core;

import java.io.IOException;

import org.webreformatter.commons.uri.Uri;
import org.webreformatter.commons.xml.XmlException;
import org.webreformatter.commons.xml.XmlWrapper;
import org.webreformatter.commons.xml.atom.AtomFeed;
import org.webreformatter.resources.IWrfResource;
import org.webreformatter.resources.adapters.cache.CachedResourceAdapter;
import org.webreformatter.resources.adapters.html.HTMLAdapter;
import org.webreformatter.resources.adapters.xml.XmlAdapter;
import org.webreformatter.scrapper.protocol.HttpStatusCode;
import org.webreformatter.scrapper.transformer.CompositeTransformer;
import org.webreformatter.scrapper.transformer.XslBasedDocumentTransformer;

/**
 * @author kotelnikov
 */
public class DocAdapter extends AppContextAdapter {

    public interface IXmlTransformation {
        XmlWrapper transform(XmlWrapper xhtml) throws XmlException, IOException;
    }

    private CompositeTransformer fDocumentTransformer = new CompositeTransformer();

    public DocAdapter(AppContext appContext) {
        super(appContext);
    }

    public XmlWrapper loadXsl(Uri xslUri) throws IOException, XmlException {
        IWrfResource xslResource = fContext.getResource("tmp", xslUri, null);
        HttpStatusCode status = fContext
            .getAdapter(DownloadAdapter.class)
            .loadResource(xslUri, xslResource);
        if (!status.isOkOrNotModified()) {
            throw new IllegalArgumentException("An XSL transformation '"
                + xslUri
                + "' could not be loaded. Status: "
                + status);
        }
        XmlAdapter xslAdapter = xslResource.getAdapter(XmlAdapter.class);
        XmlWrapper xsl = xslAdapter.getWrapper();
        return xsl;
    }

    public void setDocumentTransformer(
        Uri url,
        XslBasedDocumentTransformer transformer) {
        fDocumentTransformer.addTransformer(url, transformer);
    }

    public void setXslTransformation(Uri urlBase, Uri xslUri)
        throws IOException,
        XmlException {
        XmlWrapper xsl = loadXsl(xslUri);
        XslBasedDocumentTransformer transformer = new XslBasedDocumentTransformer(
            xsl);
        setDocumentTransformer(urlBase, transformer);
    }

    public void toXml(
        Uri resourceUri,
        IWrfResource rawResource,
        IWrfResource xmlResource) throws IOException, XmlException {
        toXml(resourceUri, rawResource, xmlResource, null);
    }

    public void toXml(
        Uri resourceUri,
        IWrfResource rawResource,
        IWrfResource xmlResource,
        IXmlTransformation transformation) throws IOException, XmlException {
        CachedResourceAdapter rawCache = rawResource
            .getAdapter(CachedResourceAdapter.class);
        CachedResourceAdapter xmlCache = xmlResource
            .getAdapter(CachedResourceAdapter.class);
        long rawResourceModificationTime = rawCache.getLastModified();
        long atomModificationTime = xmlCache.getLastModified();
        boolean needUpdates = rawResourceModificationTime < 0
            || atomModificationTime < 0
            || atomModificationTime < rawResourceModificationTime;
        if (needUpdates) {
            HTMLAdapter htmlAdapter = rawResource.getAdapter(HTMLAdapter.class);
            XmlWrapper doc = htmlAdapter.getWrapper();
            if (transformation != null) {
                doc = transformation.transform(doc);
            }
            if (doc != null) {
                XmlAdapter xmlAdapter = xmlResource
                    .getAdapter(XmlAdapter.class);
                xmlAdapter.setDocument(doc);
                xmlCache.copyPropertiesFrom(rawCache);
            }
        }
    }

    public void transformToAtom(
        final Uri resourceUri,
        IWrfResource rawResource,
        IWrfResource atomResource) throws IOException, XmlException {
        toXml(resourceUri, rawResource, atomResource, new IXmlTransformation() {
            @Override
            public XmlWrapper transform(XmlWrapper xhtml)
                throws XmlException,
                IOException {
                AtomFeed atomDoc = transformToAtom(resourceUri, xhtml);
                return atomDoc;
            }
        });
    }

    public AtomFeed transformToAtom(Uri resourceUri, XmlWrapper xml)
        throws XmlException,
        IOException {
        AtomFeed atomDoc = fDocumentTransformer.transformDocument(
            resourceUri,
            xml);
        return atomDoc;
    }

}
