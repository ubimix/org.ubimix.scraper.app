package org.webreformatter.scrapper.normalizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;
import org.webreformatter.commons.uri.Uri;
import org.webreformatter.pageset.IUrlTransformer;
import org.webreformatter.server.xml.XmlAcceptor;
import org.webreformatter.server.xml.XmlAcceptor.XmlVisitor;
import org.webreformatter.server.xml.XmlException;
import org.webreformatter.server.xml.XmlWrapper;
import org.webreformatter.server.xml.XmlWrapper.CompositeNamespaceContext;
import org.webreformatter.server.xml.XmlWrapper.SimpleNamespaceContext;
import org.webreformatter.server.xml.XmlWrapper.XmlContext;

public class XslUtils {

    public static List<XmlWrapper> getElementList(XmlWrapper xml, String tagName) {
        String prefix = "";
        int idx = tagName.indexOf(':');
        if (idx > 0) {
            prefix = tagName.substring(0, idx);
            tagName = tagName.substring(idx + 1);
        }
        final XmlContext xmlContext = xml.getXmlContext();
        CompositeNamespaceContext namespaceContext = xmlContext
            .getNamespaceContext();
        final List<XmlWrapper> result = new ArrayList<XmlWrapper>();
        final String namespaceUri = namespaceContext.getNamespaceURI(prefix);
        final String name = tagName;
        XmlAcceptor.accept(xml.getRoot(), new XmlVisitor() {
            @Override
            public void visit(Element node) {
                String uri = node.getNamespaceURI();
                if (namespaceUri == null || namespaceUri.equals(uri)) {
                    String n = node.getLocalName();
                    if (name.equals(n)) {
                        XmlWrapper a = new XmlWrapper(node, xmlContext);
                        result.add(a);
                    }
                }
                super.visit(node);
            }
        });
        return result;
    }

    /**
     * This method checks if the specified namespace is already registred in the
     * given XML context and if it is not registered then adds it with the
     * default prefix; this method returns the name of the prefix corresponding
     * to the specified namespace.
     * 
     * @param xmlContext an {@link CompositeNamespaceContext} object to fix
     * @param namespace the namespace to check
     * @param defaultPrefix the default prefix used if there is no other prefix
     *        defined
     * @return the prefix corresponding to the specified namespace
     */
    public static String getNamespacePrefix(
        CompositeNamespaceContext xmlContext,
        String namespace,
        String defaultPrefix) {
        String prefix = xmlContext.getPrefix(namespace);
        if (prefix == null) {
            prefix = defaultPrefix;
            xmlContext
                .addContext(new SimpleNamespaceContext(prefix, namespace));
        }
        return prefix;
    }

    /**
     * This method resolves all references in the specified document (transforms
     * these references to absolute ones) and returns a list of wrappers
     * corresponding to all resolved links.
     * 
     * @param doc the XML document where links should be resolved
     * @param docUrl the URL of the XML document
     * @param tagName the name of the XML tags containing references
     * @param attrName the name of the XML reference attribute
     * @return a list of all resolved XML tags
     * @throws XmlException
     */
    public static List<XmlWrapper> resolveLinks(
        XmlWrapper doc,
        Uri docUrl,
        String tagName,
        String attrName) throws XmlException {
        List<XmlWrapper> result = new ArrayList<XmlWrapper>();
        List<XmlWrapper> list = getElementList(doc, tagName);
        String scheme = docUrl.getScheme();
        if (scheme != null) {
            scheme += ":";
        }
        for (XmlWrapper tag : list) {
            String attr = tag.getAttribute(attrName);
            if (attr != null) {
                if (attr.startsWith("//")) {
                    attr = scheme + attr;
                }
                Uri url = docUrl.getResolved(attr);
                if (url != null) {
                    result.add(tag);
                    tag.setAttribute(attrName, url.toString());
                }
            }
        }
        return result;
    }

    /**
     * This method transforms all absolute references to relative ones; the base
     * is the URL of the given XML document.
     * 
     * @param xml the XML document
     * @param urlTransformer the transformer used to retrieve localized URLs
     * @param docUri the URL of the document
     * @param tagName the name of the tag containing references
     * @param attrName the name of the XML attribute containing references
     * @return a list of all transformed references
     * @throws XmlException
     */
    public static Map<Uri, List<XmlWrapper>> transformLinks(
        XmlWrapper xml,
        IUrlTransformer urlTransformer,
        Uri docUri,
        String tagName,
        final String attrName) throws XmlException {
        final Map<Uri, List<XmlWrapper>> result = new HashMap<Uri, List<XmlWrapper>>();
        List<XmlWrapper> list = getElementList(xml, tagName);
        for (XmlWrapper node : list) {
            String str = node.getAttribute(attrName);
            if (str != null) {
                Uri link = new Uri(str);
                List<XmlWrapper> elements = result.get(link);
                if (elements == null) {
                    elements = new ArrayList<XmlWrapper>();
                    result.put(link, elements);
                }
                elements.add(node);

                Uri localizedLink = urlTransformer != null ? urlTransformer
                    .transform(link) : null;
                if (localizedLink != null) {
                    node.setAttribute(attrName, localizedLink.toString());
                }
            }
        }
        return result;
    }
}
