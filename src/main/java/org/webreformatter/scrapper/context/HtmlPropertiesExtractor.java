package org.webreformatter.scrapper.context;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.webreformatter.commons.xml.XmlAcceptor;
import org.webreformatter.commons.xml.XmlException;
import org.webreformatter.commons.xml.XmlTagExtractor;
import org.webreformatter.commons.xml.XmlWrapper;
import org.webreformatter.commons.xml.XmlAcceptor.XmlVisitor;
import org.webreformatter.commons.xml.XmlTagExtractor.HtmlBlockElementsAcceptor;
import org.webreformatter.commons.xml.XmlTagExtractor.HtmlNamedNodeAcceptor;
import org.webreformatter.commons.xml.XmlTagExtractor.IElementAcceptor;
import org.webreformatter.commons.xml.XmlTagExtractor.SimpleElementAcceptor;
import org.webreformatter.commons.xml.XmlWrapper.XmlContext;

/**
 * @author kotelnikov
 */
public class HtmlPropertiesExtractor {

    private final static Logger log = Logger
        .getLogger(HtmlPropertiesExtractor.class.getName());

    protected static <T> T[] toArray(T... array) {
        return array;
    }

    protected void buildPropertiesFromList(
        XmlWrapper ul,
        Map<String, Object> properties) throws XmlException {
        if (ul == null) {
            return;
        }
        XmlWrapper child = ul.getFirstElement();
        while (child != null) {
            XmlWrapper next = child.getNextElement();
            String name = getHTMLName(child.getRootElement());
            if ("li".equals(name)) {
                String[] key = { visitPropertyItem(child) };
                Object value = getPropertyValue(key, child);
                properties.put(key[0], value);
            }
            child = next;
        }
    }

    protected boolean extractNodeProperties(
        XmlWrapper xml,
        Map<String, Object> properties) throws XmlException {
        boolean result = false;
        String name = getHTMLName(xml.getRootElement());
        if ("ul".equals(name)) {
            buildPropertiesFromList(xml, properties);
            result = true;
        }
        return result;
    }

    public Map<String, Object> extractProperties(final XmlWrapper xml)
        throws XmlException {
        return extractProperties(xml, null, null);
    }

    public Map<String, Object> extractProperties(
        final XmlWrapper xml,
        XmlWrapper start) throws XmlException {
        return extractProperties(xml, start, null);
    }

    public Map<String, Object> extractProperties(
        final XmlWrapper xml,
        XmlWrapper start,
        XmlWrapper stop) throws XmlException {
        XmlTagExtractor extractor = new XmlTagExtractor();

        String[] propertyElementNames = getPropertyElementNames();
        HtmlNamedNodeAcceptor propertyElementsAcceptor = new HtmlNamedNodeAcceptor(
            propertyElementNames);

        IElementAcceptor begin;
        if (start != null) {
            begin = new SimpleElementAcceptor(start.getRootElement());
        } else {
            begin = IElementAcceptor.YES_ACCEPTOR;
        }

        IElementAcceptor end;
        if (stop != null) {
            end = new SimpleElementAcceptor(stop.getRootElement());
        } else {
            HtmlBlockElementsAcceptor e = new HtmlBlockElementsAcceptor();
            e.removeNames(propertyElementNames);
            end = e;
        }

        List<Element> list = extractor.loadElements(
            xml.getRootElement(),
            propertyElementsAcceptor,
            begin,
            end);

        final Map<String, Object> properties = new HashMap<String, Object>();
        XmlContext context = xml.getXmlContext();
        for (Element element : list) {
            XmlWrapper wrapper = context.wrap(element);
            boolean extracted = extractNodeProperties(wrapper, properties);
            if (extracted) {
                removeNode(wrapper);
            }
        }
        return properties;
    }

    protected String getHTMLName(Element tag) {
        String name = tag.getLocalName();
        return name;
    }

    protected String[] getPropertyElementNames() {
        return toArray("ul");
    }

    protected Object getPropertyValue(String[] key, XmlWrapper value)
        throws XmlException {
        String str = value.toString(false, false);
        return str.trim();
    }

    protected XmlException handleError(String msg, Throwable e) {
        log.log(Level.FINE, msg, e);
        if (e instanceof XmlException) {
            return (XmlException) e;
        }
        return new XmlException(msg, e);
    }

    protected void removeNode(XmlWrapper wrapper) throws XmlException {
        Element node = wrapper.getRootElement();
        Node parent = node.getParentNode();
        if (parent == null) {
            return;
        }
        parent.removeChild(node);
        boolean parentIsEmpty = true;
        Node child = parent.getFirstChild();
        while (child != null) {
            if (child instanceof Element) {
                parentIsEmpty = false;
            } else if (child instanceof Text) {
                String str = ((Text) child).getData();
                parentIsEmpty = str.trim().length() == 0;
            }
            if (!parentIsEmpty) {
                break;
            }
            child = child.getNextSibling();
        }
        if (parentIsEmpty) {
            Node parentOfParent = parent.getParentNode();
            if (parentOfParent != null && !(parentOfParent instanceof Document)) {
                parentOfParent.removeChild(parent);
            }
        }
    }

    private String visitPropertyItem(XmlWrapper item) throws XmlException {
        final String[] result = { null };
        XmlAcceptor.accept(item.getRoot(), new XmlVisitor() {
            @Override
            public void visit(Element node) {
                if (result[0] == null) {
                    super.visit(node);
                }
            }

            @Override
            public void visit(Text n) {
                if (result[0] == null) {
                    String str = n.getData();
                    int idx = str.indexOf(':');
                    if (idx > 0) {
                        result[0] = str.substring(0, idx).trim();
                        str = str.substring(idx + 1);
                        n.setData(str);
                    }
                }
            }
        });
        if (result[0] == null) {
            result[0] = "";
        }
        return result[0];
    }
}