/**
 * 
 */
package org.webreformatter.pageset.loaders;

import org.webreformatter.commons.uri.Uri;
import org.webreformatter.commons.xml.XmlException;
import org.webreformatter.commons.xml.XmlWrapper;

/**
 * @author kotelnikov
 */
public class AbstractXmlLoader {

    protected Uri getUrl(Uri configUri, String str) {
        Uri uri = Uri.EMPTY;
        if (!isEmpty(str)) {
            uri = new Uri(str.trim());
            if (configUri != null) {
                uri = configUri.getResolved(uri);
            }
        }
        return uri;
    }

    protected String getValue(XmlWrapper w, String name) throws XmlException {
        String str = w.evalStr("@" + name);
        if (isEmpty(str)) {
            str = w.evalStr(name);
        }
        return str;
    }

    protected boolean isEmpty(String str) {
        if (str == null) {
            return true;
        }
        str = str.trim();
        return str.length() == 0;
    }
}