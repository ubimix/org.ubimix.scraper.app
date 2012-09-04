/**
 * 
 */
package org.ubimix.scrapper.app;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ubimix.commons.io.IOUtil;
import org.ubimix.commons.strings.StringUtil;
import org.ubimix.commons.uri.path.PathManager;
import org.ubimix.server.mime.IMimeTypeDetector;

/**
 * @author kotelnikov
 */
public class ResourceServlet extends HttpServlet {

    private static final long serialVersionUID = 1897193224436470890L;

    private IMimeTypeDetector fMimeTypeDetector;

    private PathManager<File> fPathMapping = new PathManager<File>();

    /**
     * 
     */
    public ResourceServlet(IMimeTypeDetector mimeTypeDetector) {
        fMimeTypeDetector = mimeTypeDetector;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException,
        IOException {
        String path = req.getPathInfo();
        if (path == null) {
            path = "";
        } else {
            path = path.trim();
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
            if (path.endsWith("/")) {
                path = path.substring(path.length() - 1);
            }
        }

        File root = null;
        synchronized (fPathMapping) {
            path = fPathMapping.getCanonicalPath(path);
            Map.Entry<String, File> entry = fPathMapping.getNearestEntry(path);
            if (entry != null) {
                String prefix = entry.getKey();
                path = path.substring(prefix.length());
                root = entry.getValue();
            }
        }
        if (root == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        File file = new File(root, path);

        if (file.isDirectory()) {
            file = new File(file, "index.html");
        }
        if (!file.exists()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        if (file.isFile()) {
            String mimeType = getMimeType(file.getName());
            if (mimeType != null) {
                resp.setContentType(mimeType);
            }
            long len = file.length();
            resp.setContentLength((int) len);
            ServletOutputStream output = resp.getOutputStream();
            FileInputStream input = new FileInputStream(file);
            IOUtil.copy(input, output);
        } else {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private String getMimeType(String name) {
        return fMimeTypeDetector.getMimeTypeByExtension(name);
    }

    protected String getPropertyByKey(final ServletConfig config, String key) {
        String result = StringUtil.resolvePropertyByKey(
            key,
            new StringUtil.IVariableProvider() {
                @Override
                public String getValue(String name) {
                    String value = config.getInitParameter(name);
                    if (value == null) {
                        value = System.getProperty(name);
                    }
                    return value;
                }
            });
        return result;
    }

    public void registerPath(String alias, File file) {
        synchronized (fPathMapping) {
            fPathMapping.add(alias, file);
        }
    }

    public void unregisterPath(String alias) {
        synchronized (fPathMapping) {
            fPathMapping.remove(alias);
        }
    }
}
