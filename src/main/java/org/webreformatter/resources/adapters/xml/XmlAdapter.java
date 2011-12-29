/**
 * 
 */
package org.webreformatter.resources.adapters.xml;

import java.io.IOException;
import java.io.InputStream;

import org.w3c.dom.Document;
import org.webreformatter.resources.IContentAdapter;
import org.webreformatter.resources.IWrfResource;
import org.webreformatter.server.xml.XmlException;
import org.webreformatter.server.xml.XmlWrapper;

/**
 * @author kotelnikov
 */
public class XmlAdapter extends AbstractXmlAdapter {

    public XmlAdapter(IWrfResource instance) {
        super(instance);
    }

    public XmlWrapper getWrapperCopy() throws IOException, XmlException {
        return getWrapperCopy(XmlWrapper.class);
    }

    public <T extends XmlWrapper> T getWrapperCopy(Class<T> type)
        throws IOException,
        XmlException {
        XmlWrapper wrapper = getWrapper();
        return wrapper.newCopy(type);
    }

    @Override
    protected Document readDocument() throws IOException, XmlException {
        IContentAdapter content = fResource.getAdapter(IContentAdapter.class);
        InputStream input = content.getContentInput();
        try {
            return XmlWrapper.readXML(input);
        } finally {
            input.close();
        }
    }

}
