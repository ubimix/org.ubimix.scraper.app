package org.webreformatter.scrapper.events;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.webreformatter.commons.events.IEventListener;
import org.webreformatter.commons.events.IEventListenerInterceptor;
import org.webreformatter.commons.events.IEventManager;
import org.webreformatter.commons.events.calls.CallListener;
import org.webreformatter.commons.uri.Uri;
import org.webreformatter.pageset.AccessManager;
import org.webreformatter.pageset.IUrlTransformer;
import org.webreformatter.resources.IContentAdapter;
import org.webreformatter.resources.IWrfResource;
import org.webreformatter.resources.adapters.cache.CachedResourceAdapter;
import org.webreformatter.resources.adapters.mime.MimeTypeAdapter;
import org.webreformatter.scrapper.context.ApplicationContext;
import org.webreformatter.scrapper.context.CoreAdapter;
import org.webreformatter.scrapper.context.HttpStatusCode;
import org.webreformatter.scrapper.events.ProcessResource.ActionRequest;
import org.webreformatter.scrapper.events.ProcessResource.ActionResponse;

/**
 * @author kotelnikov
 */
public class ApplyActionHandler extends CallListener<ApplyAction> {

    private static class EventTypeRegistry {

        private Map<String, Map<String, Class<?>>> fRegistry = new HashMap<String, Map<String, Class<?>>>();

        public Class<?> getEventType(String mimeType, String actionName) {
            synchronized (fRegistry) {
                Class<?> result = null;
                mimeType = ProcessResource.normalize(mimeType);
                actionName = ProcessResource.normalize(actionName);
                Map<String, Class<?>> map = fRegistry.get(mimeType);
                if (map != null) {
                    result = map.get(actionName);
                }
                return result;
            }
        }

        public void registerEventType(
            String mimeType,
            String actionName,
            Class<?> type) {
            synchronized (fRegistry) {
                mimeType = ProcessResource.normalize(mimeType);
                actionName = ProcessResource.normalize(actionName);
                Map<String, Class<?>> map = fRegistry.get(mimeType);
                if (map == null) {
                    map = new HashMap<String, Class<?>>();
                    fRegistry.put(mimeType, map);
                }
                map.put(actionName, type);
            }
        }

        public Class<?> unregisterEventType(String mimeType, String actionName) {
            synchronized (fRegistry) {
                Class<?> result = null;
                mimeType = ProcessResource.normalize(mimeType);
                actionName = ProcessResource.normalize(actionName);
                Map<String, Class<?>> map = fRegistry.get(mimeType);
                if (map != null) {
                    result = map.remove(actionName);
                    if (map.isEmpty()) {
                        fRegistry.remove(mimeType);
                    }
                }
                return result;
            }
        }

    }

    public final static String DOWNLOAD_STORE = "download";

    private final static Logger log = Logger.getLogger(ApplyActionHandler.class
        .getName());

    public final static String PARAM_ACTION = "action";

    public final static String RESULT_STORE = "results";

    private ApplicationContext fApplicationContext;

    private EventTypeRegistry fRegistry = new EventTypeRegistry();

    public ApplyActionHandler(ApplicationContext applicationContext) {
        fApplicationContext = applicationContext;
        IEventManager eventManager = fApplicationContext.getEventManager();
        eventManager.addListenerInterceptor(new IEventListenerInterceptor() {
            private ProcessResourceHandler<?> getResourceHandler(
                Class<?> eventType,
                IEventListener<?> listener) {
                if (!(ProcessResource.class.isAssignableFrom(eventType))) {
                    return null;
                }
                if (!(listener instanceof ProcessResourceHandler<?>)) {
                    return null;
                }
                return (ProcessResourceHandler<?>) listener;
            }

            public void onAddListener(
                Class<?> eventType,
                IEventListener<?> listener) {
                ProcessResourceHandler<?> handler = getResourceHandler(
                    eventType,
                    listener);
                if (handler != null) {
                    String[] mimeTypes = handler.getMimeTypes();
                    String[] actions = handler.getActionNames();
                    for (String mimeType : mimeTypes) {
                        for (String action : actions) {
                            fRegistry.registerEventType(
                                mimeType,
                                action,
                                eventType);
                        }
                    }
                }
            }

            public void onRemoveListener(
                Class<?> eventType,
                IEventListener<?> listener) {
                ProcessResourceHandler<?> handler = getResourceHandler(
                    eventType,
                    listener);
                if (handler != null) {
                    String[] mimeTypes = handler.getMimeTypes();
                    String[] actions = handler.getActionNames();
                    for (String mimeType : mimeTypes) {
                        for (String action : actions) {
                            fRegistry.unregisterEventType(mimeType, action);
                        }
                    }
                }
            }
        });
    }

    private void handleError(String msg, Throwable e) {
        log.log(Level.FINE, msg, e);
    }

    @Override
    protected void handleRequest(final ApplyAction topEvent) {
        ActionRequest actionRequest = topEvent.getRequest();
        boolean scheduled = false;
        HttpStatusCode resultStatus = HttpStatusCode.STATUS_404;
        try {
            IEventManager eventManager = fApplicationContext.getEventManager();
            IWrfResource initialResource = actionRequest.getInitialResource();
            CachedResourceAdapter initialResourceCacheAdapter = initialResource
                .getAdapter(CachedResourceAdapter.class);
            if (initialResourceCacheAdapter.isExpired()) {
                CoreAdapter adapter = fApplicationContext
                    .getAdapter(CoreAdapter.class);
                Uri url = actionRequest.getUrl();
                IUrlTransformer uriTransformer = actionRequest
                    .getDownloadUrlTransformer();
                AccessManager accessManager = actionRequest.getAccessManager();
                url = uriTransformer.transform(url);
                HttpStatusCode code = adapter.download(
                    accessManager,
                    url,
                    initialResource);
                boolean exists = code.isOk()
                    || HttpStatusCode.STATUS_304.equals(code) /* NOT_MODIFIED */;
                if (exists) {
                    initialResourceCacheAdapter.touch();
                }
            }
            try {
                String mimeType = "";
                IContentAdapter initialContent = initialResource
                    .getAdapter(IContentAdapter.class);
                boolean exists = initialContent.exists();
                if (exists) {
                    MimeTypeAdapter mimeTypeAdapter = initialResource
                        .getAdapter(MimeTypeAdapter.class);
                    mimeType = mimeTypeAdapter.getMimeType();
                    String actionName = actionRequest.getActionName();
                    String action = actionName;
                    TOP: while (true) {
                        String mime = mimeType;
                        while (true) {
                            Class<?> eventType = fRegistry.getEventType(
                                mime,
                                action);
                            if (eventType != null) {
                                ProcessResource newEvent = newEvent(
                                    eventType,
                                    actionRequest);
                                // FIXME : maybe we should use a barrier
                                eventManager.fireEvent(
                                    newEvent,
                                    new CallListener<ProcessResource>() {
                                        @Override
                                        protected void handleResponse(
                                            ProcessResource event) {
                                            topEvent.setResponse(event
                                                .getResponse());
                                        }
                                    });
                                scheduled = true;
                                break TOP;
                            }
                            if ("".equals(mime)) {
                                break;
                            }
                            mime = trimLastSegment(mime);
                        }
                        if ("".equals(action)) {
                            break;
                        }
                        action = trimLastSegment(action);
                    }
                }
            } catch (Throwable t) {
                handleError("Can not handle the last request. URL: "
                    + actionRequest.getUrl()
                    + ".", t);
                resultStatus = HttpStatusCode.STATUS_500;
            }
        } catch (Throwable e) {
            handleError("Can not handle resource action", e);
            topEvent.onError(e);
        } finally {
            if (!scheduled) {
                ActionResponse actionResponse = new ActionResponse();
                IWrfResource to = actionRequest.newTargetResource("");
                actionResponse.setResultResource(to);
                topEvent.setResponse(actionResponse);
                try {
                    actionResponse.setResultStatus(resultStatus);
                } catch (IOException e) {
                    // FIXME:
                    handleError("Can not set result status", e);
                }
            }
        }
    }

    private ProcessResource newEvent(
        Class<?> eventType,
        ActionRequest actionRequest) throws Exception {
        ActionRequest request = ActionRequest.builder(actionRequest).build();
        Constructor<?> constructor = eventType
            .getConstructor(ActionRequest.class);
        ProcessResource instance = (ProcessResource) constructor
            .newInstance(request);
        return instance;
    }

    private String trimLastSegment(String str) {
        int idx = str.lastIndexOf('/');
        if (idx > 0) {
            str = str.substring(0, idx);
        } else {
            str = "";
        }
        return str;
    }

}