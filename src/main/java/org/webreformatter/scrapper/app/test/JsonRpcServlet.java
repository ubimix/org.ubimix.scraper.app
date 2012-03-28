package org.webreformatter.scrapper.app.test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.webreformatter.commons.digests.Sha1Digest;
import org.webreformatter.commons.io.IOUtil;
import org.webreformatter.commons.json.JsonArray;
import org.webreformatter.commons.json.JsonObject;
import org.webreformatter.commons.json.rpc.IRpcCallHandler;
import org.webreformatter.commons.json.rpc.IRpcCallHandler.IRpcCallback;
import org.webreformatter.commons.json.rpc.RpcDispatcher;
import org.webreformatter.commons.json.rpc.RpcError;
import org.webreformatter.commons.json.rpc.RpcRequest;
import org.webreformatter.commons.json.rpc.RpcResponse;
import org.webreformatter.scrapper.protocol.HttpStatusCode;

/**
 * @author kotelnikov
 */
public class JsonRpcServlet extends HttpServlet {

    private static final String ENCODING = "UTF-8";

    private static Logger log = Logger
        .getLogger(JsonRpcServlet.class.getName());

    private static final String MIME_TYPE = "application/json";

    private static final long serialVersionUID = -6602353013259628191L;

    private RpcDispatcher fDispatcher = new RpcDispatcher();

    private String fIdBase = "id-"
        + new Random(System.currentTimeMillis()).nextLong()
        + "-";

    private int fIdCounter = 0;

    public JsonRpcServlet() {
        initListeners();
    }

    protected void initListeners() {
        fDispatcher.setDefaultHandler(new IRpcCallHandler() {
            public void handle(RpcRequest request, IRpcCallback rpcCallback) {
                Object id = request.getId();
                if (id == null) {
                    id = newRequestId();
                }
                RpcResponse response = new RpcResponse()
                    .<RpcResponse> setId(id)
                    .<RpcResponse> setError(
                        RpcError.ERROR_METHOD_NOT_FOUND,
                        "Method not found");
                rpcCallback.finish(response);
            }
        });
        // DATA
        final JsonArray tags = JsonArray.FACTORY.newValue("["
            + "'lorem','quis odio','lacus','sagittis','volutpat',"
            + "'nulla porttitor','nulla quis','urna convallis',"
            + "'elementum','arcu dictum','rhoncus vulputate','scelerisque',"
            + "'mauris'"
            + "]");
        final JsonArray spaces = JsonArray.FACTORY.newValue("["
            + "{id:123,title:'My Test Space'},"
            + "{id:345,title:'A space 2'},"
            + "{id:456,title:'New space'}"
            + "]");
        final Map<String, JsonObject> pages = new HashMap<String, JsonObject>();
        pages.put(
            "http://localhost:8888/resources/bmk/loremipsum.html",
            JsonObject.newValue("{"
                + "url:'http://localhost:8088/bmk/loremipsum.html',"
                + "visibleUrl:'http://localhost:8088/bmk/',"
                + "spaceId:345,"
                + "title:'Lorem Ipsum Test',"
                + "notes:'A very good test page',"
                + "tags:['lorem','ipsum', 'sapien enim'],"
                + "clippedContent:[ '<h1>This is a lorem ipsum</h1>' ]"
                + "}"));

        fDispatcher.registerHandler(
            "server.loadPageInfo",
            new IRpcCallHandler() {
                public void handle(RpcRequest request, IRpcCallback callback) {
                    RpcResponse response = newResponse(request);
                    String url = request.getParamsAsObject().getString("url");
                    JsonObject pageInfo = pages.get(url);
                    if (pageInfo == null) {
                        pageInfo = new JsonObject();
                    }
                    response.setResult(pageInfo);
                    callback.finish(response);
                }
            });
        fDispatcher.registerHandler(
            "server.savePageInfo",
            new IRpcCallHandler() {
                public void handle(RpcRequest request, IRpcCallback callback) {
                    RpcResponse response = newResponse(request);
                    JsonObject data = request.getParamsAsObject();
                    if (data != null) {
                        String url = data.getString("url");
                        pages.put(url, data);
                        JsonObject result = new JsonObject();
                        result.setValue(
                            "message",
                            "OK. Page annotation was successfully saved.");
                        response.setResult(result);
                    }
                    callback.finish(response);
                }
            });
        fDispatcher.registerHandler(
            "server.loadTagsAndSpaces",
            new IRpcCallHandler() {
                public void handle(RpcRequest request, IRpcCallback callback) {
                    RpcResponse response = newResponse(request);
                    JsonObject result = new JsonObject();
                    result.setValue("tags", tags);
                    result.setValue("spaces", spaces);
                    response.setResult(result);
                    callback.finish(response);
                }
            });
    }

    public synchronized String newRequestId() {
        Sha1Digest digest = new Sha1Digest.Builder()
            .update(fIdBase + System.currentTimeMillis() + "-" + (fIdCounter++))
            .build();
        return "id-" + digest.toString();
    }

    protected RpcResponse newResponse(RpcRequest request) {
        Object requestId = request.getId();
        if (requestId == null) {
            requestId = newRequestId();
        }
        return new RpcResponse().<RpcResponse> setId(requestId).setResult(
            new JsonObject());
    }

    private RpcRequest readRequest(HttpServletRequest req) throws IOException {
        String content = req.getParameter("content");
        if (content == null) {
            ServletInputStream input = req.getInputStream();
            content = IOUtil.readString(input);
        }
        RpcRequest request = RpcRequest.FACTORY.newValue(content);
        return request;
    }

    public IOException reportError(String msg, Throwable t) {
        log.log(Level.WARNING, msg, t);
        if (t instanceof IOException) {
            return (IOException) t;
        }
        return new IOException(msg, t);
    }

    // Call:
    // http://localhost:8888/json-rpc/?content={"id":123,
    // "method":"server/loadTagsAndSpaces","params":{}}
    @Override
    protected void service(
        HttpServletRequest req,
        final HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setCharacterEncoding(ENCODING);
            resp.setCharacterEncoding(ENCODING);
            resp.setContentType(MIME_TYPE);

            RpcRequest request = readRequest(req);
            fDispatcher.handle(request, new IRpcCallback() {
                public void finish(RpcResponse response) {
                    try {
                        HttpStatusCode status = HttpStatusCode.STATUS_200;
                        resp.setStatus(status.getStatusCode());
                        String msg = response.toString();
                        byte[] array = msg.getBytes("UTF-8");
                        resp.getOutputStream().write(array);
                    } catch (Throwable t) {
                        reportError("Can not send the response", t);
                    }
                }
            });
        } catch (Exception t) {
            throw reportError(
                "Can not download a resource with the specified URL",
                t);
        }
    }

}
