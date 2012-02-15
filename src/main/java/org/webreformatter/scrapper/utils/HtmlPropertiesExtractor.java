/**
 * 
 */
package org.webreformatter.scrapper.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.webreformatter.commons.xml.XmlException;
import org.webreformatter.commons.xml.XmlTagExtractor;
import org.webreformatter.commons.xml.XmlTagExtractor.HtmlBlockElementsAcceptor;
import org.webreformatter.commons.xml.XmlTagExtractor.IElementAcceptor;
import org.webreformatter.commons.xml.XmlTagExtractor.SimpleElementAcceptor;
import org.webreformatter.commons.xml.XmlWrapper;
import org.webreformatter.commons.xml.XmlWrapper.XmlContext;

/**
 * Instances of this type are used to extract properties from XML/XHTML content.
 * 
 * @author kotelnikov
 */
public abstract class HtmlPropertiesExtractor {

    /**
     * This is the default implementation of the listener. It extracts
     * properties from the found XML property nodes and notify about them.
     * 
     * @author kotelnikov
     */
    public static abstract class AbstractPropertyListener
        implements
        IPropertyListener {

        /**
         * This set contains all image properties. So all images will be
         * replaced by the corresponding image URLs.
         */
        private Set<String> fImageProperties = new HashSet<String>();

        /**
         * This map defines property name replacements. Keys are properties
         * found in the HTML and the corresponding values - the property names
         * returned in the final property map.
         */
        private Map<String, String> fPropertyNameReplacements = new LinkedHashMap<String, String>();

        /**
         * This set contains all reference properties. So all found reference
         * HTML elements corresponding to these properties will be replaced by
         * the corresponding URLs.
         */
        private Set<String> fReferenceProperties = new HashSet<String>();

        /**
         * Adds new properties which should be interpreted as images (so the
         * image URLs from the corresponding XML nodes are extracted and
         * returned instead of the original XML nodes).
         * 
         * @param propertyNames the names of the image properties
         */
        public void addImageProperties(String... propertyNames) {
            for (String propertyName : propertyNames) {
                addImageProperty(propertyName);
            }
        }

        /**
         * Adds a new property which should be interpreted as an image (so the
         * image URL from the corresponding XML node is extracted and returned
         * instead of the original XML).
         * 
         * @param propertyName the name of the image property
         */
        public void addImageProperty(String propertyName) {
            fImageProperties.add(propertyName);
        }

        /**
         * Adds a new property replacement.
         * 
         * @param propertyName the property name found in the HTML
         * @param newPropertyName a new property name to use; this value is
         *        returned in the final property map
         */
        public void addPropertyReplacement(
            String propertyName,
            String newPropertyName) {
            fPropertyNameReplacements.put(propertyName, newPropertyName);
        }

        /**
         * This method adds new property replacements to the internal map; on
         * the pair positions there are names to replace; the odd positions
         * contain the corresponding new names.
         * 
         * @param names a list of old/new name pairs
         */
        public void addPropertyReplacements(String... names) {
            for (int i = 0; i < names.length;) {
                String oldName = names[i++];
                String newName = i < names.length ? names[i++] : null;
                addPropertyReplacement(oldName, newName);
            }
        }

        /**
         * Adds new properties which should be interpreted as references (so the
         * link URLs from the corresponding XML nodes are extracted and returned
         * instead of the original XML nodes).
         * 
         * @param propertyName the name of the image property
         */
        public void addReferenceProperties(String... propertyNames) {
            for (String propertyName : propertyNames) {
                addReferenceProperty(propertyName);
            }
        }

        /**
         * Adds a new property which should be interpreted as an reference (so
         * the link URL from the corresponding XML node is extracted and
         * returned instead of XML itself).
         * 
         * @param propertyName the name of the image property
         */
        public void addReferenceProperty(String propertyName) {
            fReferenceProperties.add(propertyName);
        }

        /**
         * Returns a reference to the image extracted from the specified XML
         * node
         * 
         * @param propertyName the name of the property
         * @param value the XML node containing images
         * @return a reference to the image
         * @throws XmlException
         */
        protected Object getImageValue(String propertyName, XmlWrapper value)
            throws XmlException {
            XmlWrapper img = value.eval(".//html:img");
            String str = null;
            if (img != null) {
                str = img.getAttribute("src");
            }
            Object result = getReference(propertyName, str);
            return result;
        }

        /**
         * Returns the property value extracted from the given XML node.
         * 
         * @param propertyName the name of the property
         * @param value the XML node containing value of the property
         * @return a value of a simple property extracted from the given XML
         *         node
         */
        protected Object getPlainValue(String propertyName, XmlWrapper value)
            throws XmlException {
            String str = value.toString(false, false);
            return trim(str);
        }

        /**
         * This method is used to fix/replace/change found key/value pairs
         * before adding them to the resulting map.
         * 
         * @param key the key of the property; this is a one-value array. It
         *        could be used to change the initial property name.
         * @param value the XML node containing the value of the property
         * @return a new property value
         * @throws XmlException
         */
        protected Object getPropertyValue(String[] key, XmlWrapper value)
            throws XmlException {
            Object result = null;
            if (fPropertyNameReplacements.containsKey(key[0])) {
                key[0] = fPropertyNameReplacements.get(key[0]);
            }
            if (fReferenceProperties.contains(key[0])) {
                result = getReferenceValue(key[0], value);
            } else if (fImageProperties.contains(key[0])) {
                result = getImageValue(key[0], value);
            } else {
                result = getPlainValue(key[0], value);
            }
            return result;
        }

        /**
         * Transforms the specified URL to the resulting property value. This
         * method is used by the {@link #getImageValue(String, XmlWrapper)} and
         * {@link #getReferenceValue(String, XmlWrapper)} methods to transform
         * extracted references to the final property values.
         * 
         * @param propertyName the name of the property
         * @param url the URL corresponding to the specified property name
         * @return a reference object corresponding to the specified URL
         */
        protected Object getReference(String propertyName, String url) {
            return url;
        }

        /**
         * Returns a reference extracted from the specified XML node.
         * 
         * @param propertyName the name of the property
         * @param value the XML node containing references
         * @return reference extracted from the specified XML node
         * @throws XmlException
         */
        protected Object getReferenceValue(String propertyName, XmlWrapper value)
            throws XmlException {
            XmlWrapper a = value.eval(".//html:a");
            String str = null;
            if (a != null) {
                str = a.getAttribute("href");
            }
            Object result = getReference(propertyName, str);
            return result;
        }

        /**
         * This method is called to notify about extracted property key/value
         * pairs
         * 
         * @param propertyName the name the property
         * @param propertyValue the value of the property
         */
        protected abstract void onProperty(
            String propertyName,
            Object propertyValue);

        /**
         * @see org.webreformatter.scrapper.utils.HtmlPropertiesExtractor.IPropertyListener#onPropertyNode(java.lang.String,
         *      org.webreformatter.commons.xml.XmlWrapper)
         */
        public void onPropertyNode(String propertyName, XmlWrapper valueNode)
            throws XmlException {
            String[] key = { propertyName };
            Object value = getPropertyValue(key, valueNode);
            onProperty(key[0], value);
        }

    }

    /**
     * Instances of this type are used to notify about found property XML
     * elements
     * 
     * @author kotelnikov
     */
    public interface IPropertyListener {

        /**
         * This method is called to notify about new XML nodes containing
         * properties
         * 
         * @param propertyName the name of the property
         * @param valueNode the XML node containing the value of the property
         * @throws XmlException
         */
        void onPropertyNode(String name, XmlWrapper valueNode)
            throws XmlException;

    }

    public static class PropertyListener extends AbstractPropertyListener {

        private Map<String, Object> fProperties;

        public PropertyListener() {
            this(new HashMap<String, Object>());
        }

        public PropertyListener(Map<String, Object> properties) {
            fProperties = properties;
        }

        public void clear() {
            fProperties.clear();
        }

        public Map<String, Object> getProperties() {
            return fProperties;
        }

        @Override
        protected void onProperty(String propertyName, Object propertyValue) {
            fProperties.put(propertyName, propertyValue);
        }

        public void setProperties(Map<String, Object> properties) {
            fProperties = properties;
        }

    }

    /**
     * Cuts of all non-character symbols at the beginning and at the end of the
     * specified string.
     * 
     * @param str the string to trim
     * @return a trimmed string
     */
    public static String trim(String str) {
        if (str == null || str.length() == 0) {
            return "";
        }
        int startPos = 0;
        int len = str.length();
        while (startPos < len) {
            char ch = str.charAt(startPos);
            if (!Character.isSpaceChar(ch)) {
                break;
            }
            startPos++;
        }
        while (startPos < len) {
            char ch = str.charAt(len - 1);
            if (!Character.isSpaceChar(ch)) {
                break;
            }
            len--;
        }
        str = str.substring(startPos, len);
        return str;
    }

    /**
     * This constructor initializes the internal listener field.
     */
    public HtmlPropertiesExtractor() {
    }

    /**
     * Extracts properties from the specified XML node and notify about found
     * elements using the {@link #onPropertyNode(String, XmlWrapper)} method.
     * 
     * @param xml the XML node containing properties to extract
     * @param listener this listener is used to notify about found properties
     * @return <code>true</code> if properties were successfully extracted
     * @throws XmlException
     */
    protected abstract boolean extractNodeProperties(
        XmlWrapper xml,
        IPropertyListener listener) throws XmlException;

    /**
     * This method extracts properties from an XML/HTML document starting from
     * the begin of the document and it search properties objects until the end
     * of the document. This method is the same as
     * {@link #extractProperties(XmlWrapper, XmlWrapper, XmlWrapper, IPropertyListener)}
     * with empty "start" and "stop" parameters.
     * 
     * @param xml the XML document used as a source of properties
     * @param listener
     * @param start the start element
     * @param listener the listener used to notify about found property fields
     * @throws XmlException
     */
    public void extractProperties(XmlWrapper xml, IPropertyListener listener)
        throws XmlException {
        extractProperties(xml, null, null, listener);
    }

    /**
     * This method extracts properties from an XML/HTML document starting from
     * the specified tag and it search properties objects until the end of the
     * document. This method is the same as
     * {@link #extractProperties(XmlWrapper, XmlWrapper, XmlWrapper, IPropertyListener)}
     * with an empty third parameter ("stop" tag).
     * 
     * @param xml the XML document used as a source of properties
     * @param start the start element
     * @param listener the listener used to notify about found property fields
     * @throws XmlException
     */
    public void extractProperties(
        XmlWrapper xml,
        XmlWrapper start,
        IPropertyListener listener) throws XmlException {
        extractProperties(xml, start, null, listener);
    }

    /**
     * This method extracts properties between two specified tags;
     * 
     * @param xml the XML node used to extract properties
     * @param start this method extracts properties from the XML document
     *        starting from this element; if this parameter is <code>null</code>
     *        then this method searches properties from the beginning of the
     *        document
     * @param stop the final element; this method seeks properties until it find
     *        this element; if this element is <code>null</code> then properties
     *        are searched until the end of the document
     * @param listener the listener used to notify about found property fields
     * @throws XmlException
     */
    public void extractProperties(
        XmlWrapper xml,
        XmlWrapper start,
        XmlWrapper stop,
        IPropertyListener listener) throws XmlException {
        IElementAcceptor nodes = newPropertyElementAcceptor(xml, start, stop);
        IElementAcceptor begin = newBeginElementAcceptor(xml, start, stop);
        IElementAcceptor end = newEndElementAcceptor(xml, start, stop);
        XmlTagExtractor extractor = new XmlTagExtractor();
        List<Element> list = extractor.loadElements(
            xml.getRootElement(),
            nodes,
            begin,
            end);

        XmlContext context = xml.getXmlContext();
        for (Element element : list) {
            XmlWrapper wrapper = context.wrap(element);
            boolean extracted = extractNodeProperties(wrapper, listener);
            if (extracted) {
                removeNode(wrapper);
            }
        }
    }

    /**
     * Returns a node acceptor used to detect the begin of the document segment
     * where property elements could be found
     * 
     * @param xml the XML document
     * @param start start element delimiting the block of the document
     *        containing properties
     * @param stop final element delimiting the block of the document containing
     *        properties
     * @return a node acceptor used to detect the begin of the doucument segment
     *         containing properties
     */
    protected IElementAcceptor newBeginElementAcceptor(
        XmlWrapper xml,
        XmlWrapper start,
        XmlWrapper stop) {
        IElementAcceptor result;
        if (start != null) {
            result = new SimpleElementAcceptor(start.getRootElement());
        } else {
            result = IElementAcceptor.YES_ACCEPTOR;
        }
        return result;
    }

    /**
     * Returns a node acceptor used to detect the end of the document segment
     * containing properties
     * 
     * @param xml the XML document
     * @param start start element delimiting the block of the document
     *        containing properties
     * @param stop final element delimiting the block of the document containing
     *        properties
     * @return a node acceptor used to detect the end of the document segment
     *         containing properties
     */
    protected IElementAcceptor newEndElementAcceptor(
        XmlWrapper xml,
        XmlWrapper start,
        XmlWrapper stop) {
        IElementAcceptor result;
        if (stop != null) {
            result = new SimpleElementAcceptor(stop.getRootElement());
        } else {
            result = new HtmlBlockElementsAcceptor();
        }
        return result;
    }

    /**
     * @param xml the root XML document
     * @param start the start element
     * @param stop end element
     * @return an element acceptor used to detect property elements
     */
    protected abstract IElementAcceptor newPropertyElementAcceptor(
        XmlWrapper xml,
        XmlWrapper start,
        XmlWrapper stop);

    /**
     * Removes the specified node from the parent.
     * 
     * @param wrapper the XML node to remove
     * @throws XmlException
     */
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
                parentIsEmpty = trim(str).length() == 0;
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
}
