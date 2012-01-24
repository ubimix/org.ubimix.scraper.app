package org.webreformatter.scrapper.transformer;

import java.io.IOException;

import org.webreformatter.commons.uri.Uri;
import org.webreformatter.scrapper.normalizer.XslUtils;
import org.webreformatter.server.xml.XmlException;
import org.webreformatter.server.xml.XmlWrapper;
import org.webreformatter.server.xml.atom.AtomFeed;

/**
 * Transformer used for applying a specific XSL transformation.
 * 
 * See also transformers applying automated transformations.
 * 
 * @author kotelnikov
 * 
 */

public class XslBasedDocumentTransformer implements IDocumentTransformer {

    private XmlWrapper fXsl;

    public XslBasedDocumentTransformer(XmlWrapper xsl) {
        fXsl = xsl;
    }

    public AtomFeed transformDocument(Uri url, XmlWrapper doc)
            throws XmlException, IOException {
        AtomFeed result = doc.applyXSL(fXsl, AtomFeed.class);
        String prefix = XslUtils
                .getNamespacePrefix(result.getXmlContext()
                        .getNamespaceContext(), "http://www.w3.org/1999/xhtml",
                        "xhtml");
        if (prefix != null && !"".equals(prefix)) {
            prefix += ":";
        }
        XslUtils.resolveLinks(result, url, prefix + "a", "href");
        XslUtils.resolveLinks(result, url, prefix + "img", "src");
        return result;
    }

}