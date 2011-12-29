/**
 * 
 */
package org.webreformatter.resources.adapters.string;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.webreformatter.commons.io.IOUtil;
import org.webreformatter.resources.IContentAdapter;
import org.webreformatter.resources.IWrfResource;
import org.webreformatter.resources.WrfResourceAdapter;

/**
 * This adapter is used to represent a resource as a string.
 * 
 * @author kotelnikov
 */
public class StringAdapter extends WrfResourceAdapter {

    public StringAdapter(IWrfResource instance) {
        super(instance);
    }

    public String getContentAsString() throws IOException {
        IContentAdapter contentAdapter = fResource
            .getAdapter(IContentAdapter.class);
        if (!contentAdapter.exists()) {
            return null;
        }
        InputStream input = contentAdapter.getContentInput();
        try {
            return IOUtil.readString(input);
        } finally {
            input.close();
        }
    }

    public void setContentAsString(String content) throws IOException {
        IContentAdapter contentAdapter = fResource
            .getAdapter(IContentAdapter.class);
        OutputStream output = contentAdapter.getContentOutput();
        IOUtil.writeString(output, content);

    }

}
