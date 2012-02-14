/**
 * 
 */
package org.webreformatter.scrapper.utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.webreformatter.commons.xml.XmlException;
import org.webreformatter.commons.xml.XmlTagExtractor.HtmlBlockElementsAcceptor;
import org.webreformatter.commons.xml.XmlTagExtractor.HtmlNamedNodeAcceptor;
import org.webreformatter.commons.xml.XmlTagExtractor.IElementAcceptor;
import org.webreformatter.commons.xml.XmlTagExtractor.SimpleElementAcceptor;
import org.webreformatter.commons.xml.XmlWrapper;

/**
 * @author kotelnikov
 */
public class HtmlTablePropertiesExtractor extends HtmlPropertiesExtractor {

    /**
     * This set contains names of the column with properties.
     */
    protected Set<String> fPropertyNameHeaders = new HashSet<String>();

    /**
     * This set contains possible names of the column containing property
     * values.
     */
    protected Set<String> fPropertyValueHeaders = new HashSet<String>();

    public HtmlTablePropertiesExtractor() {
        setPropertyNameHeaders("property", "properties");
        setPropertyValueHeaders("value", "values");
    }

    protected boolean checkTableHeader(XmlWrapper headerRow)
        throws XmlException {
        String propertyHeader = getCellContent(headerRow, "./html:th[1]");
        String valueHeader = getCellContent(headerRow, "./html:th[2]");
        return fPropertyNameHeaders.contains(propertyHeader.toLowerCase())
            && fPropertyValueHeaders.contains(valueHeader.toLowerCase());
    }

    @Override
    protected boolean extractNodeProperties(
        XmlWrapper xml,
        IPropertyListener listener) throws XmlException {
        List<XmlWrapper> rows = xml.evalList("./html:tr");
        if (rows.isEmpty()) {
            return false;
        }
        if (!checkTableHeader(rows.get(0))) {
            return false;
        }
        for (int i = 1; i < rows.size(); i++) {
            XmlWrapper row = rows.get(i);
            String propertyName = getCellContent(row, "./html:td[1]");
            XmlWrapper propertyNode = row.eval("./html:td[2]");
            listener.onPropertyNode(propertyName, propertyNode);
        }
        return true;
    }

    private String getCellContent(XmlWrapper cell) {
        if (cell == null) {
            return "";
        }
        String text = cell.toString(false, false);
        text = trim(text);
        return text;
    }

    private String getCellContent(XmlWrapper xml, String xpath)
        throws XmlException {
        XmlWrapper cell = xml.eval(xpath);
        return getCellContent(cell);
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
            e.removeNames("table");
            result = e;
        }
        return result;
    }

    @Override
    protected IElementAcceptor newPropertyElementAcceptor(
        XmlWrapper xml,
        XmlWrapper start,
        XmlWrapper stop) {
        return new HtmlNamedNodeAcceptor("table");
    }

    /**
     * Sets a new set of possible headers for the column containing property
     * names
     * 
     * @param values set of headers of the column for property names
     */
    public void setPropertyNameHeaders(Collection<String> values) {
        fPropertyNameHeaders.clear();
        fPropertyNameHeaders.addAll(values);
    }

    /**
     * Sets a new set of possible headers for the column containing property
     * names
     * 
     * @param values set of headers of the column for property names
     */
    public void setPropertyNameHeaders(String... values) {
        setPropertyNameHeaders(Arrays.asList(values));
    }

    /**
     * Sets a new set of possible headers for the column containing property
     * values.
     * 
     * @param values set of headers for the column with property values
     */
    public void setPropertyValueHeaders(Collection<String> values) {
        fPropertyValueHeaders.clear();
        fPropertyValueHeaders.addAll(values);
    }

    /**
     * Sets a new set of possible headers for the column containing property
     * values.
     * 
     * @param values set of headers for the column with property values
     */
    public void setPropertyValueHeaders(String... values) {
        setPropertyValueHeaders(Arrays.asList(values));
    }

}
