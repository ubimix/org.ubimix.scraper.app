/**
 * 
 */
package org.webreformatter.resources.adapters.html;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.w3c.dom.Document;
import org.webreformatter.resources.IContentAdapter;
import org.webreformatter.resources.IWrfResource;
import org.webreformatter.resources.adapters.encoding.EncodingAdapter;
import org.webreformatter.resources.adapters.xml.AbstractXmlAdapter;
import org.webreformatter.server.xml.XmlException;
import org.webreformatter.server.xml.XmlWrapper;

/**
 * @author kotelnikov
 */
public class HTMLAdapter extends AbstractXmlAdapter {

    public HTMLAdapter(IWrfResource instance) {
        super(instance);
    }

    @Override
    protected Document readDocument() throws IOException, XmlException {
        EncodingAdapter mimeTypeAdapter = fResource
            .getAdapter(EncodingAdapter.class);
        String encoding = mimeTypeAdapter.getEncoding();
        if (encoding == null) {
            encoding = "UTF-8";
        }
        IContentAdapter content = fResource.getAdapter(IContentAdapter.class);
        InputStream input = content.getContentInput();
        try {
            Reader reader = new InputStreamReader(input, encoding);
            try {
                return HTMLUtils.cleanupHTML(reader);
            } catch (Exception e) {
                throw XmlWrapper.handleError(
                    "Can not clean up the underlying HTML document.",
                    e);
            }
        } finally {
            input.close();
        }
    }

}
