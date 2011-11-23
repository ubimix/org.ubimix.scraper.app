/**
 * 
 */
package org.webreformatter.resources.adapters.html;

import java.io.Reader;

import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.DomSerializer;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.w3c.dom.Document;

/**
 * @author kotelnikov
 */
public class HTMLUtils {

    /**
     * @param reader
     * @return
     * @throws Exception
     */
    public static Document cleanupHTML(Reader reader) throws Exception {
        try {
            CleanerProperties properties = new CleanerProperties();
            properties.setTranslateSpecialEntities(true);
            properties.setRecognizeUnicodeChars(true);
            properties.setNamespacesAware(false);
            properties.setAdvancedXmlEscape(true);
            properties.setUseEmptyElementTags(true);
            properties.setOmitUnknownTags(true);
            properties.setTreatUnknownTagsAsContent(false);
            HtmlCleaner cleaner = new HtmlCleaner(properties);
            TagNode tag = cleaner.clean(reader);

            DomSerializer serializer = new DomSerializer(properties);
            Document document = serializer.createDOM(tag);

            // CleanerProperties properties = new CleanerProperties();
            // properties.setAdvancedXmlEscape(true);
            // properties.setNamespacesAware(false);
            // properties.setOmitDeprecatedTags(false);
            // properties.setOmitUnknownTags(true);
            // properties.setOmitXmlDeclaration(false);
            // properties.setOmitDoctypeDeclaration(false);
            // properties.setRecognizeUnicodeChars(false);
            // properties.setTranslateSpecialEntities(true);
            // properties.setTreatUnknownTagsAsContent(true);
            // properties.setUseEmptyElementTags(true);
            // HtmlCleaner cleaner = new HtmlCleaner(properties);
            // TagNode tag = cleaner.clean(reader);
            // properties = new CleanerProperties();
            // DomSerializer serializer = new DomSerializer(properties);
            // Document document = serializer.createDOM(tag);
            return document;
        } finally {
            reader.close();
        }
    }

}
