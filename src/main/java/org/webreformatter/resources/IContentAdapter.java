/**
 * 
 */
package org.webreformatter.resources;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author kotelnikov
 */
public interface IContentAdapter extends IWrfResourceAdapter {

    /**
     * This event is fired when the content is changed (written on the store).
     * 
     * @author kotelnikov
     */
    public class ContentChangeEvent extends WrfResourceEvent {
    }

    void delete() throws IOException;

    boolean exists();

    InputStream getContentInput() throws IOException;

    OutputStream getContentOutput() throws IOException;

    long getLastModified();

    void writeContent(InputStream input) throws IOException;

}
