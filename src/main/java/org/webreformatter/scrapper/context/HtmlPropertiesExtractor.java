package org.webreformatter.scrapper.context;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.webreformatter.server.xml.XmlAcceptor;
import org.webreformatter.server.xml.XmlAcceptor.XmlVisitor;
import org.webreformatter.server.xml.XmlException;
import org.webreformatter.server.xml.XmlWrapper;

/**
 * @author kotelnikov
 */
public class HtmlPropertiesExtractor {

    public static final String _NS_XHTML = "http://www.w3.org/1999/xhtml";

    private final static Logger log = Logger
        .getLogger(HtmlPropertiesExtractor.class.getName());

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
                String key = visitPropertyItem(child);
                Object value = getPropertyValue(child);
                properties.put(key, value);
            }
            child = next;
        }
    }

    protected boolean checkStopTag(Element node) {
        String name = getHTMLName(node);
        return "h1".equals(name)
            || "h2".equals(name)
            || "h3".equals(name)
            || "h4".equals(name)
            || "h5".equals(name)
            || "h6".equals(name)
            || "hr".equals(name)
            || "table".equals(name)
            || "tr".equals(name)
            || "th".equals(name)
            || "td".equals(name)
            || "ol".equals(name)
            || "dd".equals(name)
            || "dt".equals(name)
            || "li".equals(name);
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

    public Map<String, Object> extractProperties(final XmlWrapper xml) {
        return extractProperties(xml, null);
    }

    public Map<String, Object> extractProperties(
        final XmlWrapper xml,
        XmlWrapper start) {
        final Map<String, Object> properties = new HashMap<String, Object>();
        final Element startTag = start != null ? start.getRootElement() : null;
        XmlAcceptor.accept(xml.getRoot(), new XmlVisitor() {

            private boolean fStarted;

            private boolean fStopped;

            protected boolean checkStartTag(Element tag) {
                return startTag == null || startTag.equals(tag);
            }

            private boolean isStarted(Element node) {
                if (!fStarted) {
                    fStarted |= checkStartTag(startTag);
                }
                return fStarted;
            }

            private boolean isStopped(Element node) {
                if (!fStopped) {
                    fStopped |= checkStopTag(node);
                }
                return fStopped;
            }

            @Override
            public void visit(Element node) {
                try {
                    if (isStarted(node)) {
                        if (!isStopped(node)) {
                            XmlWrapper wrapper = wrap(node);
                            boolean extracted = extractNodeProperties(
                                wrapper,
                                properties);
                            if (extracted) {
                                removeNode(wrapper);
                            } else {
                                super.visit(node);
                            }
                        }
                    } else {
                        super.visit(node);
                    }
                } catch (Throwable t) {
                    handleError("Can not read page properties.", t);
                }
            }

            private XmlWrapper wrap(Element node) throws XmlException {
                return xml.getXmlContext().wrap(node);
            }
        });
        return properties;
    }

    protected String getHTMLName(Element tag) {
        String name = tag.getLocalName();
        return name;
    }

    protected Object getPropertyValue(XmlWrapper child) throws XmlException {
        StringWriter writer = new StringWriter();
        child.serializeXML(writer, false);
        String str = writer.toString();
        str = str.trim();
        return str;
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