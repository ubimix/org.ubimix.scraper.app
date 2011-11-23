/**
 * 
 */
package org.webreformatter.server.encoding;

import java.io.IOException;
import java.io.InputStream;

/**
 * Service of this type is used to detect encoding of the text context.
 * 
 * @author kotelnikov
 */
public interface IEncodingDetector {

    /**
     * Reads the initial bytes from the given stream and tries to detect the
     * encoding of the content. Note that this method <strong>DOES NOT</strong>
     * close the given stream.
     * 
     * <pre>
     * Example of usage:mime
     * 
     *  IEncodingDetector detector = ...
     *  InputStream input = ...
     *  try {
     *      if (!input.markSupported())
     *              input = new BufferedInputStream(input);
     *      input.mark(5 * 1000);
     *      String encoding = detector.getEncoding(input);
     *      input.reset();
     *      // Do something useful with the stream 
     *      // and with the content encoding. 
     *      ...
     *   } finally {
     *      input.close();
     *   }
     * </pre>
     * 
     * @param input the input stream giving access to the underlying content.
     * @return the type MIME of the content
     * @throws IOException
     */
    String getEncoding(InputStream input) throws IOException;

}
