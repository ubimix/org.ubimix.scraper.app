/**
 * 
 */
package org.webreformatter.server.encoding;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import eu.medsea.util.EncodingGuesser;

/**
 * @author kotelnikov
 */
public class EncodingDetector implements IEncodingDetector {

    /**
     * 
     */
    public EncodingDetector() {
        EncodingGuesser.setSupportedEncodings(EncodingGuesser
            .getCanonicalEncodingNamesSupportedByJVM());
    }

    @SuppressWarnings("unchecked")
    public String getEncoding(InputStream input) throws IOException {
        byte[] buf = new byte[1024 * 10];
        int len = input.read(buf);
        if (len < buf.length) {
            byte[] newBuf = new byte[len];
            System.arraycopy(buf, 0, newBuf, 0, len);
            buf = newBuf;
        }
        Collection<String> encodings = EncodingGuesser
            .getPossibleEncodings(buf);
        if (encodings == null || encodings.isEmpty()) {
            return null;
        }
        String encoding = encodings.iterator().next();
        return encoding;
    }

}
