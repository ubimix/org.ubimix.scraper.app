package org.webreformatter.scrapper.normalizer;

import java.io.IOException;

import org.webreformatter.commons.uri.Uri;
import org.webreformatter.pageset.AccessManager;
import org.webreformatter.pageset.IUrlMapper;
import org.webreformatter.pageset.PageSetConfig;
import org.webreformatter.resources.IWrfResource;
import org.webreformatter.resources.adapters.cache.CachedResourceAdapter;
import org.webreformatter.resources.adapters.xml.XmlAdapter;
import org.webreformatter.scrapper.context.CoreAdapter;
import org.webreformatter.scrapper.context.ApplicationContext;
import org.webreformatter.scrapper.context.HttpStatusCode;
import org.webreformatter.scrapper.context.RuntimeContext;
import org.webreformatter.server.xml.XmlException;
import org.webreformatter.server.xml.XmlWrapper;
import org.webreformatter.server.xml.atom.AtomFeed;

public class XslBasedContentNormalizer implements IDocumentNormalizer {

    public AtomFeed getNormalizedContent(
        RuntimeContext context,
        Uri url,
        XmlWrapper doc) throws XmlException, IOException {
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
        ApplicationContext applicationContext = context.getApplicationContext();
        CoreAdapter coreAdapter = applicationContext
            .getAdapter(CoreAdapter.class);
        IWrfResource resource = coreAdapter.getResource(xslUrl, "xsl", true);
        XmlWrapper xsl = null;
        boolean ok = true;
        if (resource.getAdapter(CachedResourceAdapter.class).isExpired()) {
            AccessManager accessManager = pageSetConfig.getAccessManager();
            CoreAdapter downloadAdapter = applicationContext
                .getAdapter(CoreAdapter.class);
            HttpStatusCode code = downloadAdapter.download(
                accessManager,
                xslUrl,
                resource);
            if (code.isOk() || HttpStatusCode.STATUS_304.equals(code) /* NOT_MODIFIED */) {
                ok = true;
            }
        }
        if (ok) {
            XmlAdapter xmlAdapter = resource.getAdapter(XmlAdapter.class);
            xsl = xmlAdapter.getWrapper();
        }
        return xsl;
    }

}