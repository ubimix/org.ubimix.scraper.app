/**
 * 
 */
package org.ubimix.scrapper.app;

import java.io.File;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.ubimix.commons.osgi.ConfigurableMultiserviceActivator;
import org.ubimix.commons.osgi.OSGIObjectActivator;
import org.ubimix.commons.osgi.OSGIObjectDeactivator;
import org.ubimix.commons.osgi.OSGIServiceActivator;
import org.ubimix.commons.osgi.OSGIServiceDeactivator;
import org.ubimix.commons.strings.StringUtil;
import org.ubimix.commons.strings.StringUtil.IVariableProvider;
import org.ubimix.commons.uri.Uri;
import org.ubimix.server.mime.IMimeTypeDetector;
import org.ubimix.server.mime.MimeTypeDetector;

/**
 * @author kotelnikov
 */
public class Activator extends ConfigurableMultiserviceActivator {

    private Map<File, Map<String, Object>> fFiles = Collections
        .synchronizedMap(new HashMap<File, Map<String, Object>>());

    private HttpContext fHttpContext;

    private HttpService fHttpService;

    private IMimeTypeDetector fMimeDetector;

    IVariableProvider fPropertyProvider = new StringUtil.IVariableProvider() {
        @Override
        public String getValue(String name) {
            String value = (String) fProperties.get(name);
            if (value == null) {
                value = System.getProperty(name);
            }
            return value;
        }
    };

    private String fResourcePath;

    private ResourceServlet fResourceServlet;

    private Map<Servlet, Map<String, Object>> fServlets = Collections
        .synchronizedMap(new HashMap<Servlet, Map<String, Object>>());

    /**
     * 
     */
    public Activator() {
    }

    @OSGIObjectActivator
    public void activate() throws Exception {
        fMimeDetector = new MimeTypeDetector();

        fHttpContext = fHttpService.createDefaultHttpContext();
        fResourcePath = getProperty("web.resources.path", "/*");
        fResourceServlet = new ResourceServlet(fMimeDetector);
        fHttpService.registerServlet(
            fResourcePath,
            fResourceServlet,
            fProperties,
            fHttpContext);

        for (Map.Entry<Servlet, Map<String, Object>> entry : fServlets
            .entrySet()) {
            Servlet servlet = entry.getKey();
            Map<String, Object> params = entry.getValue();
            registerServlet(servlet, params);
        }

        for (Map.Entry<File, Map<String, Object>> entry : fFiles.entrySet()) {
            File file = entry.getKey();
            Map<String, Object> params = entry.getValue();
            registerFile(file, params);
        }
    }

    @OSGIServiceActivator(min = 0)
    public void addFile(File file, Map<String, Object> params) {
        fFiles.put(file, params);
        registerFile(file, params);
    }

    @OSGIServiceActivator(min = 0)
    public void addServlet(Servlet servlet, Map<String, Object> params)
        throws ServletException,
        NamespaceException {
        fServlets.put(servlet, params);
        registerServlet(servlet, params);
    }

    @Override
    protected boolean checkPropertiesModifications(Dictionary<?, ?> properties) {
        return !equals(fProperties, properties);
    }

    @OSGIObjectDeactivator
    public void deactivate() {
        for (Map.Entry<Servlet, Map<String, Object>> entry : fServlets
            .entrySet()) {
            Map<String, Object> params = entry.getValue();
            String alias = (String) params.get("alias");
            if (alias != null) {
                fHttpService.unregister(alias);
            }
        }
        fServlets.clear();
        for (Map.Entry<File, Map<String, Object>> entry : fFiles.entrySet()) {
            unregisterFile(entry.getKey(), entry.getValue());
        }
        fFiles.clear();
        fHttpContext = null;
        if (fResourcePath != null) {
            fHttpService.unregister(fResourcePath);
            fResourcePath = null;
            fResourceServlet = null;
        }
    }

    private boolean equals(Object first, Object second) {
        return first == null || second == null ? first == second : first
            .equals(second);
    }

    protected Uri getConfigUri(String configKey, String defaultUri) {
        String str = getProperty(configKey, defaultUri);
        Uri uri = new Uri(str);
        return uri;
    }

    private String getProperty(String key, String defaultValue) {
        String value = StringUtil.resolvePropertyByKey(key, fPropertyProvider);
        if (value == null) {
            value = StringUtil.resolveProperty(defaultValue, fPropertyProvider);
        }
        return value;
    }

    @Override
    protected String getServiceID() {
        return "org.ubimix.scrapper";
    }

    protected void registerFile(File file, Map<String, Object> params) {
        if (fResourceServlet != null) {
            String alias = (String) params.get("alias");
            if (alias != null) {
                fResourceServlet.registerPath(alias, file);
            }
        }
    }

    protected void registerServlet(Servlet servlet, Map<String, Object> params)
        throws ServletException,
        NamespaceException {
        String alias = (String) params.get("alias");
        if (alias != null && fHttpService != null) {
            Hashtable<String, String> p = new Hashtable<String, String>();
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                p.put(entry.getKey(), "" + entry.getValue());
            }
            p.remove("alias");
            fHttpService.registerServlet(alias, servlet, p, fHttpContext);
        }
    }

    @OSGIServiceDeactivator
    public void removeFile(File file, Map<String, Object> params) {
        fFiles.remove(file);
    }

    @OSGIServiceDeactivator
    public void removeService(HttpService service) {
        fHttpService = null;
    }

    @OSGIServiceDeactivator
    public void removeServlet(Servlet servlet, Map<String, Object> params) {
        fServlets.remove(servlet);
    }

    @OSGIServiceActivator
    public void setService(HttpService service) {
        fHttpService = service;
    }

    protected void unregisterFile(File file, Map<String, Object> params) {
        if (fResourceServlet != null) {
            String alias = (String) params.get("alias");
            if (alias != null) {
                fResourceServlet.unregisterPath(alias);
            }
        }
    }

}
