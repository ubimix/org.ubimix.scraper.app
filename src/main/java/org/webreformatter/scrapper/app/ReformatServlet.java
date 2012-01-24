package org.webreformatter.scrapper.app;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.webreformatter.commons.uri.Path.Builder;
import org.webreformatter.commons.uri.Uri;
import org.webreformatter.pageset.PageSetConfig;
import org.webreformatter.pageset.PageSetConfigRegistry;
import org.webreformatter.pageset.UrlToPathMapper;
import org.webreformatter.scrapper.context.ApplicationContext;
import org.webreformatter.scrapper.protocol.HttpStatusCode;

public class ReformatServlet extends HttpServlet {

    private static final String DEFAULT_PAGE_SET_KEY = "";

    private static final String ENCODING = "UTF-8";

    private static Logger log = Logger.getLogger(ReformatServlet.class
            .getName());

    private static final String MIME_TYPE = "text/html";

    private static final long serialVersionUID = -6602353013259628191L;

    private ApplicationContext fApplicationContext;

    private PageSetConfigRegistry fPageSetConfigRegistry;

    public ReformatServlet(PageSetConfigRegistry pageSetConfigRegistry,
            ApplicationContext applicationContext) {
        fPageSetConfigRegistry = pageSetConfigRegistry;
        init(applicationContext);
    }

    @Override
    protected void doGet(HttpServletRequest req, final HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            req.setCharacterEncoding(ENCODING);
            resp.setCharacterEncoding(ENCODING);
            resp.setContentType(MIME_TYPE);

            // FIXME: get the page set key from parameters
            Uri.Builder builder = new Uri.Builder(req.getPathInfo());
            Builder pathBuilder = builder.getPathBuilder();
            pathBuilder.makeRelativePath();
            String pageSetKey = null;
            List<String> segments = pathBuilder.getPathSegments();
            if (segments.size() > 0) {
                String firstSegment = segments.get(0);
                if (firstSegment.startsWith(".")) {
                    pageSetKey = firstSegment.substring(1);
                    pathBuilder.removeFirstPathSegments(1);
                }
            }
            if (pageSetKey == null) {
                pageSetKey = req.getParameter("pageset");
            }
            PageSetConfig pageSetConfig = fPageSetConfigRegistry
                    .getPageSetConfig(pageSetKey);
            if (pageSetConfig == null) {
                pageSetKey = DEFAULT_PAGE_SET_KEY;
                pageSetConfig = fPageSetConfigRegistry
                        .getPageSetConfig(pageSetKey);
            }
            if (pageSetConfig == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            Uri path = builder.build();
            UrlToPathMapper mapper = pageSetConfig.getUrlToPathMapper();
            Uri url = mapper.pathToUri(path);
            if (url == null) {
                String q = req.getParameter("url");
                if (q != null) {
                    url = new Uri(q);
                }
            }
            if (url == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            Map<String, String> params = getRequestParams(req);
            String prefix = pageSetKey;
            if (!DEFAULT_PAGE_SET_KEY.equals(prefix)) {
                prefix = "." + prefix;
            }
            HttpStatusCode status = HttpStatusCode.STATUS_500;
            resp.sendError(status.getStatusCode());
            // FIXME:
        } catch (Exception t) {
            throw reportError(
                    "Can not download a resource with the specified URL", t);
        }
    }

    public ApplicationContext getExecutionContext() {
        return fApplicationContext;
    }

    protected Map<String, String> getRequestParams(HttpServletRequest req) {
        Map<String, String> params = new HashMap<String, String>();
        String query = req.getQueryString();
        if (query != null) {
            Uri queryUri = new Uri("?" + query);
            for (Uri.QueryItem item : queryUri.getQueryItems()) {
                String key = item.getName(false, false);
                String value = item.getValue(false, false);
                params.put(key, value);
            }
        }
        for (@SuppressWarnings("unchecked")
        Enumeration<String> paramNames = req.getParameterNames(); paramNames
                .hasMoreElements();) {
            String key = paramNames.nextElement();
            String value = req.getParameter(key);
            params.put(key, value);
        }
        return params;
    }

    public void init(ApplicationContext applicationContext) {
        setExecutionContext(applicationContext);
    }

    public IOException reportError(String msg, Throwable t) {
        log.log(Level.WARNING, msg, t);
        if (t instanceof IOException) {
            return (IOException) t;
        }
        return new IOException(msg, t);
    }

    public void setExecutionContext(ApplicationContext applicationContext) {
        fApplicationContext = applicationContext;
    }

    protected void setResponseProperties(HttpServletResponse resp,
            Map<String, String> properties) {
        boolean skip = false;
        if (skip) {
            return;
        }
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            String key = entry.getKey();
            String k = key.toLowerCase();
            if ("content-length".equals(k) || "statuscode".equals(k)
                    || "connection".equals(k) || "content-encoding".equals(k)) {
                continue;
            }
            resp.setHeader(key, entry.getValue());
        }
    }
}
