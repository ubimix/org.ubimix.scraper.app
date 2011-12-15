package org.webreformatter.scrapper.normalizer;

import java.io.IOException;

import org.webreformatter.commons.uri.Uri;
import org.webreformatter.pageset.IUrlMapper;
import org.webreformatter.pageset.IUrlTransformer;
import org.webreformatter.pageset.PageSetConfig;
import org.webreformatter.resources.IWrfResource;
import org.webreformatter.resources.adapters.xml.XmlAdapter;
import org.webreformatter.scrapper.context.DownloadAdapter;
import org.webreformatter.scrapper.context.RuntimeContext;
import org.webreformatter.server.xml.XmlException;
import org.webreformatter.server.xml.XmlWrapper;
import org.webreformatter.server.xml.atom.AtomFeed;

/**
 * @author kotelnikov
 */
public class XslBasedContentNormalizer implements IDocumentNormalizer {

    public AtomFeed getNormalizedContent(RuntimeContext context, XmlWrapper doc)
        throws XmlException,
        IOException {
        Uri url = context.getUrl();
        AtomFeed result = null;
        XmlWrapper xsl = getXsl(context, url);
        if (xsl != null) {
            result = doc.applyXSL(xsl, AtomFeed.class);
            String prefix = XslUtils
                .getNamespacePrefix(
                    result.getXmlContext().getNamespaceContext(),
                    "http://www.w3.org/1999/xhtml",
                    "xhtml");
            if (prefix != null && !"".equals(prefix)) {
                prefix += ":";
            }
            XslUtils.resolveLinks(result, url, prefix + "a", "href");
            XslUtils.resolveLinks(result, url, prefix + "img", "src");
        }
        return result;
    }

    public int getPriority() {
        return 10;
    }

    private XmlWrapper getXsl(RuntimeContext context, Uri url)
        throws XmlException,
        IOException {
        PageSetConfig pageSetConfig = context.getPageSetConfig();
        IUrlMapper urlMapper = pageSetConfig.getUriMapper();
        Uri xslUrl = urlMapper != null ? urlMapper.getUrl("xsl", url) : null;
        if (xslUrl == null) {
            return null;
        }
        RuntimeContext xslContext = context
            .builder()
            .setUrl(xslUrl)
            .setDownloadUrlTransformer(IUrlTransformer.EMPTY)
            .build();
        DownloadAdapter downloadAdapter = xslContext
            .getAdapter(DownloadAdapter.class);
        IWrfResource resource = downloadAdapter.loadResource();
        XmlWrapper xsl = null;
        boolean ok = downloadAdapter.isOK();
        if (ok) {
            XmlAdapter xmlAdapter = resource.getAdapter(XmlAdapter.class);
            xsl = xmlAdapter.getWrapper();
        }
        return xsl;
    }

}