/**
 * 
 */
package org.webreformatter.server.mime;

import java.io.IOException;
import java.io.InputStream;

/**
 * Service of this type is used to detect MIME type of the binary content.
 * 
 * @author kotelnikov
 */
public interface IMimeTypeDetector {

    /**
     * Reads the initial bytes from the given stream and tries to detect the
     * mime type of the content. Note that this method <strong>DOES NOT</strong>
     * close the given stream.
     * 
     * <pre>
     * Example of usage:
     * 
     *  IEncodingDetector detector = ...
     *  InputStream input = ...
     *  try {
     *      if (!input.markSupported())
     *              input = new BufferedInputStream(input);
     *      input.mark(5 * 1000);
     *      String mimeType = detector.getMimeType(input);
     *      input.reset();
     *      // Do something useful with the stream 
     *      // and with the MIME type. 
     *      ...
     *   } finally {
     *      input.close();
     *   }
     * </pre>
     * 
     * @param input the input stream giving access to the binary content.
     * @return the type MIME of the content
     * @throws IOException
     */
    String getMimeType(InputStream input) throws IOException;

    String getMimeTypeByExtension(String fileName);
}
