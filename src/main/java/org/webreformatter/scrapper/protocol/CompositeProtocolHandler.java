/**
 * 
 */
package org.webreformatter.scrapper.protocol;

import java.util.HashMap;
import java.util.Map;

import org.webreformatter.commons.uri.Uri;
import org.webreformatter.resources.IWrfResource;

/**
 * @author kotelnikov
 */
public class CompositeProtocolHandler implements IProtocolHandler {

    private IProtocolHandler fDefaultProtocolHandler;

    private Map<String, IProtocolHandler> fHandlers = new HashMap<String, IProtocolHandler>();

    public CompositeProtocolHandler() {
        this(new IProtocolHandler() {
            public HttpStatusCode handleRequest(
                Uri uri,
                String login,
                String password,
                IWrfResource resource) {
                return HttpStatusCode.STATUS_505;
            }
        });
    }

    /**
     * This constructor initializes the internal default protocol handler.
     * 
     * @param handler a protocol handler called for all unknown protocols.
     */
    public CompositeProtocolHandler(IProtocolHandler handler) {
        setDefaultProtocolHandler(handler);
    }

    public IProtocolHandler getDefaultProtocolHandler(String protocol) {
        return fDefaultProtocolHandler;
    }

    public IProtocolHandler getProtocolHandler(String protocol) {
        synchronized (fHandlers) {
            return fHandlers.get(protocol);
        }
    }

    public HttpStatusCode handleRequest(
        Uri uri,
        String login,
        String password,
        IWrfResource resource) {
        String scheme = uri.getScheme();
        IProtocolHandler handler = getProtocolHandler(scheme);
        if (handler == null) {
            handler = fDefaultProtocolHandler;
        }
        HttpStatusCode result = handler.handleRequest(
            uri,
            login,
            password,
            resource);
        return result;
    }

    public IProtocolHandler removeProtocolHandler(String protocol) {
        synchronized (fHandlers) {
            return fHandlers.remove(protocol);
        }
    }

    public void setDefaultProtocolHandler(IProtocolHandler handler) {
        fDefaultProtocolHandler = handler;
    }

    public void setProtocolHandler(String protocol, IProtocolHandler handler) {
        synchronized (fHandlers) {
            fHandlers.put(protocol, handler);
        }
    }
}
