/**
 * 
 */
package org.webreformatter.server.mime;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class MimeTypeDetector implements IMimeTypeDetector {

    private String fDefaultMimeType = "application/octet-stream";

    private Map<String, String> fMap = new HashMap<String, String>();

    public MimeTypeDetector() {
        fMap.put("css", "text/css");
        fMap.put("gif", "image/gif");
        fMap.put("js", "text/plain");
        fMap.put("json", "application/json");
        fMap.put("html", "text/html");
        fMap.put("jpg", "image/jpeg");
        fMap.put("jpeg", "image/jpeg");
        fMap.put("png", "image/png");
        fMap.put("txt", "text/plain");
        fMap.put("zip", "application/zip");
        fMap.put("xml", "application/xml");
    }

    public String getMimeType(InputStream input) throws IOException {
        return fDefaultMimeType;
    }

    public String getMimeTypeByExtension(String name) {
        if (name == null) {
            name = "";
        }
        name = name.toLowerCase();
        String mimeType = null;
        int idx = name.lastIndexOf('.');
        if (idx > 0) {
            String ext = name.substring(idx + 1);
            mimeType = fMap.get(ext);
        }
        if (mimeType == null) {
            mimeType = fDefaultMimeType;
        }
        return mimeType;
    }

}