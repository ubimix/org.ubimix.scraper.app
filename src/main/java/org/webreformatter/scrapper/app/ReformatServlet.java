package org.webreformatter.scrapper.app;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.webreformatter.commons.events.IEventManager;
import org.webreformatter.commons.events.calls.CallListener;
import org.webreformatter.commons.events.server.CallBarrier;
import org.webreformatter.commons.uri.Uri;
import org.webreformatter.pageset.PageSetConfig;
import org.webreformatter.pageset.PageSetConfigRegistry;
import org.webreformatter.pageset.UrlToPathMapper;
import org.webreformatter.resources.IPropertyAdapter;
import org.webreformatter.scrapper.context.ApplicationContext;
import org.webreformatter.scrapper.context.HttpStatusCode;
import org.webreformatter.scrapper.events.ApplyAction;
import org.webreformatter.scrapper.events.ProcessResource.ActionRequest;
import org.webreformatter.scrapper.events.ProcessResource.ActionResponse;

public class ReformatServlet extends HttpServlet {

    private static final String DEFAULT_PAGE_SET_KEY = "";

    private static final String ENCODING = "UTF-8";

    private static Logger log = Logger.getLogger(ReformatServlet.class
        .getName());

    private static final String MIME_TYPE = "text/html";

    private static final long serialVersionUID = -6602353013259628191L;

    private ApplicationContext fApplicationContext;

    private PageSetConfigRegistry fPageSetConfigRegistry;

    public ReformatServlet(
        PageSetConfigRegistry pageSetConfigRegistry,
        ApplicationContext applicationContext) {
        fPageSetConfigRegistry = pageSetConfigRegistry;
        init(applicationContext);
    }

    @Override
    protected void doGet(HttpServletRequest req, final HttpServletResponse resp)
        throws ServletException,
        IOException {
        try {
            req.setCharacterEncoding(ENCODING);
            resp.setCharacterEncoding(ENCODING);
            resp.setContentType(MIME_TYPE);

            // FIXME: get the page set key from parameters
            String pageSetKey = req.getParameter("pageset");
            PageSetConfig pageSetConfig = fPageSetConfigRegistry
                .getPageSetConfig(pageSetKey);
            if (pageSetConfig == null) {
                pageSetConfig = fPageSetConfigRegistry
                    .getPageSetConfig(DEFAULT_PAGE_SET_KEY);
            }
            if (pageSetConfig == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            String q = req.getParameter("url");
            Uri url = null;
            if (q != null) {
                url = new Uri(q);
            } else {
                UrlToPathMapper mapper = pageSetConfig.getUrlToPathMapper();
                q = req.getPathInfo();
                Uri.Builder builder = new Uri.Builder(q);
                builder.getPathBuilder().makeRelativePath();
                url = mapper.pathToUri(builder.build());
            }
            if (url == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            Map<String, String> params = getRequestParams(req);
            ActionRequest actionRequest = ActionRequest
                .builder()
                .setApplicationContext(fApplicationContext)
                .setPageSetConfig(pageSetConfig)
                .setUrl(url)
                .setParams(params)
                .build();
            IEventManager eventManager = fApplicationContext.getEventManager();
            ApplyAction event = new ApplyAction(actionRequest);
            final ActionResponse[] response = { null };
            CallBarrier barrier = new CallBarrier();
            eventManager.fireEvent(
                event,
                barrier.add(new CallListener<ApplyAction>() {
                    @Override
                    protected void handleResponse(ApplyAction event) {
                        response[0] = event.getResponse();
                    }
                }));
            barrier.await();
            HttpStatusCode status = response[0].getResultStatus();
            resp.setStatus(status.getStatusCode());
            if (!status.isError()) {
                IPropertyAdapter propertiesAdapter = response[0]
                    .getResultResource()
                    .getAdapter(IPropertyAdapter.class);
                Map<String, String> properties = propertiesAdapter
                    .getProperties();
                setResponseProperties(resp, properties);
                InputStream input = response[0].getResponseContent();
                if (input != null) {
                    try {
                        byte[] buf = new byte[1024 * 10];
                        int len;
                        ServletOutputStream output = resp.getOutputStream();
                        while ((len = input.read(buf)) > 0) {
                            output.write(buf, 0, len);
                        }
                    } finally {
                        input.close();
                    }
                }
            }

        } catch (Exception t) {
            throw reportError(
                "Can not download a resource with the specified URL",
                t);
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

    protected void setResponseProperties(
        HttpServletResponse resp,
        Map<String, String> properties) {
        boolean skip = false;
        if (skip) {
            return;
        }
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            String key = entry.getKey();
            String k = key.toLowerCase();
            if ("content-length".equals(k)
                || "statuscode".equals(k)
                || "connection".equals(k)
                || "content-encoding".equals(k)) {
                continue;
            }
            resp.setHeader(key, entry.getValue());
        }
    }
}