/**
 * 
 */
package org.webreformatter.scrapper.core;

import java.io.File;
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

    private CompositeTransformer fDocumentTransformer = new CompositeTransformer();

    public DocAdapter(AppContext appContext) {
        super(appContext);
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
        if (!xslFile.exists()) {
            throw new IllegalArgumentException(
                "An XSL transformation for the '"
                    + urlBase
                    + "' resources was not found. File '"
                    + xslFile
                    + "' ('"
                    + xslPath
                    + "') dos not exist.");
        }
        Uri xslUri = new Uri(xslFile.toURI() + "");
        setXslTransformation(urlBase, xslUri);
    }

    public void setXslTransformation(String urlBase, Uri xslUri)
        throws IOException,
        XmlException {
        IWrfResource xslResource = fContext.getResource("tmp", xslUri, null);
        HttpStatusCode status = fContext
            .getAdapter(DownloadAdapter.class)
            .loadResource(xslUri, xslResource);
        if (!status.isOkOrNotModified()) {
            throw new IllegalArgumentException(
                "An XSL transformation for the '"
                    + urlBase
                    + "' resources could not be loaded. Status: "
                    + status);
        }
        XmlAdapter xslAdapter = xslResource.getAdapter(XmlAdapter.class);
        XmlWrapper xsl = xslAdapter.getWrapper();
        XslBasedDocumentTransformer transformer = new XslBasedDocumentTransformer(
            xsl);
        setDocumentTransformer(urlBase, transformer);
    }

    public void toXml(
        Uri resourceUri,
        IWrfResource rawResource,
        IWrfResource xmlResource) throws IOException, XmlException {
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
            XmlAdapter xmlAdapter = xmlResource.getAdapter(XmlAdapter.class);
            HTMLAdapter htmlAdapter = rawResource.getAdapter(HTMLAdapter.class);
            XmlWrapper doc = htmlAdapter.getWrapper();
            xmlAdapter.setDocument(doc);
            xmlCache.copyPropertiesFrom(rawCache);
        }
    }

    public void transformToAtom(
        Uri resourceUri,
        IWrfResource rawResource,
        IWrfResource atomResource) throws IOException, XmlException {
        CachedResourceAdapter rawCache = rawResource
            .getAdapter(CachedResourceAdapter.class);
        CachedResourceAdapter atomCache = atomResource
            .getAdapter(CachedResourceAdapter.class);
        long rawResourceModificationTime = rawCache.getLastModified();
        long atomModificationTime = atomCache.getLastModified();
        boolean needUpdates = rawResourceModificationTime < 0
            || atomModificationTime < 0
            || atomModificationTime < rawResourceModificationTime;
        if (needUpdates) {
            HTMLAdapter htmlAdapter = rawResource.getAdapter(HTMLAdapter.class);
            XmlWrapper doc = htmlAdapter.getWrapper();
            AtomFeed atomDoc = fDocumentTransformer.transformDocument(
                resourceUri,
                doc);
            XmlAdapter atomAdapter = atomResource.getAdapter(XmlAdapter.class);
            atomAdapter.setDocument(atomDoc);
            atomCache.copyPropertiesFrom(rawCache);
        }
    }

}
