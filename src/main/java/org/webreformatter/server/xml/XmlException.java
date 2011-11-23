/**
 * 
 */
package org.webreformatter.server.xml;

/**
 * @author kotelnikov
 */
public class XmlException extends Exception {
    private static final long serialVersionUID = 895859327888179368L;

    public XmlException(String message) {
        super(message);
    }

    public XmlException(String message, Throwable cause) {
        super(message, cause);
    }

}
