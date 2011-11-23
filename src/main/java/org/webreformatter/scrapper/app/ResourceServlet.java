/**
 * 
 */
package org.webreformatter.scrapper.app;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.webreformatter.commons.io.IOUtil;
import org.webreformatter.commons.strings.StringUtil;
import org.webreformatter.server.mime.IMimeTypeDetector;

/**
 * @author kotelnikov
 */
public class ResourceServlet extends HttpServlet {

    private static final String KEY_ROOT_DIR = "root.dir";

    private static final long serialVersionUID = 1897193224436470890L;

    private IMimeTypeDetector fMimeTypeDetector;

    private File fRootDir;

    /**
     * 
     */
    public ResourceServlet(File dir, IMimeTypeDetector mimeTypeDetector) {
        fRootDir = dir;
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
        File file = new File(fRootDir, path);
        if (!file.exists()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
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

    @Override
    public void init(ServletConfig config) throws ServletException {
        if (fRootDir == null) {
            String rootDir = getPropertyByKey(config, KEY_ROOT_DIR);
            if (rootDir == null) {
                rootDir = "./root";
            }
            fRootDir = new File(rootDir);
        }
    }
}
