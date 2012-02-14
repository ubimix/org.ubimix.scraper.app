package org.webreformatter.scrapper.utils;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.webreformatter.commons.xml.XHTMLUtils;
import org.webreformatter.commons.xml.XmlAcceptor;
import org.webreformatter.commons.xml.XmlAcceptor.XmlVisitor;
import org.webreformatter.commons.xml.XmlException;
import org.webreformatter.commons.xml.XmlTagExtractor.HtmlBlockElementsAcceptor;
import org.webreformatter.commons.xml.XmlTagExtractor.HtmlNamedNodeAcceptor;
import org.webreformatter.commons.xml.XmlTagExtractor.IElementAcceptor;
import org.webreformatter.commons.xml.XmlTagExtractor.SimpleElementAcceptor;
import org.webreformatter.commons.xml.XmlWrapper;

/**
 * @author kotelnikov
 */
public class HtmlListPropertiesExtractor extends HtmlPropertiesExtractor {

    private final static Logger log = Logger
        .getLogger(HtmlListPropertiesExtractor.class.getName());

    protected static <T> T[] toArray(T... array) {
        return array;
    }

    public HtmlListPropertiesExtractor() {
    }

    protected void buildPropertiesFromList(
        XmlWrapper ul,
        IPropertyListener listener) throws XmlException {
        if (ul == null) {
            return;
        }
        XmlWrapper child = ul.getFirstElement();
        while (child != null) {
            XmlWrapper next = child.getNextElement();
            String name = XHTMLUtils.getHTMLName(child.getRootElement());
            if ("li".equals(name)) {
                listener.onPropertyNode(visitPropertyItem(child), child);
            }
            child = next;
        }
    }

    @Override
    protected boolean extractNodeProperties(
        XmlWrapper xml,
        IPropertyListener listener) throws XmlException {
        boolean result = false;
        String name = XHTMLUtils.getHTMLName(xml.getRootElement());
        if ("ul".equals(name)) {
            buildPropertiesFromList(xml, listener);
            result = true;
        }
        return result;
    }

    protected String[] getPropertyElementNames() {
        return toArray("ul");
    }

    protected XmlException handleError(String msg, Throwable e) {
        log.log(Level.FINE, msg, e);
        if (e instanceof XmlException) {
            return (XmlException) e;
        }
        return new XmlException(msg, e);
    }

    @Override
    protected IElementAcceptor newEndElementAcceptor(
        XmlWrapper xml,
        XmlWrapper start,
        XmlWrapper stop) {
        IElementAcceptor result;
        if (stop != null) {
            result = new SimpleElementAcceptor(stop.getRootElement());
        } else {
            HtmlBlockElementsAcceptor e = new HtmlBlockElementsAcceptor();
            String[] propertyElementNames = getPropertyElementNames();
            e.removeNames(propertyElementNames);
            result = e;
        }
        return result;
    }

    @Override
    protected IElementAcceptor newPropertyElementAcceptor(
        XmlWrapper xml,
        XmlWrapper start,
        XmlWrapper stop) {
        String[] propertyElementNames = getPropertyElementNames();
        HtmlNamedNodeAcceptor propertyElementsAcceptor = new HtmlNamedNodeAcceptor(
            propertyElementNames);
        return propertyElementsAcceptor;
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
                        result[0] = trim(str.substring(0, idx));
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