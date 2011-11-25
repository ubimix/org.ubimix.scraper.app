package org.webreformatter.scrapper.events;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.webreformatter.commons.events.calls.CallEvent;
import org.webreformatter.commons.uri.Uri;
import org.webreformatter.pageset.IUrlTransformer;
import org.webreformatter.pageset.PageSetConfig;
import org.webreformatter.resources.IContentAdapter;
import org.webreformatter.resources.IWrfResource;
import org.webreformatter.resources.adapters.cache.CachedResourceAdapter;
import org.webreformatter.scrapper.context.ApplicationContext;
import org.webreformatter.scrapper.context.HttpStatusCode;
import org.webreformatter.scrapper.context.RuntimeContext;

/**
 * @author kotelnikov
 */
public abstract class ProcessResource
    extends
    CallEvent<ProcessResource.ActionRequest, ProcessResource.ActionResponse> {

    public static class ActionRequest extends RuntimeContext {

        public static class Builder extends RuntimeContext.Builder {

            protected IWrfResource fInitialResource;

            public Builder() {
                super();
            }

            public Builder(ActionRequest request) {
                super(request);
                if (request != null) {
                    fInitialResource = request.fInitialResource;
                }
            }

            @Override
            public ActionRequest build() {
                return new ActionRequest(this);
            }

            @Override
            public ActionRequest.Builder setApplicationContext(
                ApplicationContext applicationContext) {
                return (ActionRequest.Builder) super
                    .setApplicationContext(applicationContext);
            }

            @Override
            public ActionRequest.Builder setDownloadUrlTransformer(
                IUrlTransformer downloadUrlTransformer) {
                super.setDownloadUrlTransformer(downloadUrlTransformer);
                return this;
            }

            @Override
            public ActionRequest.Builder setLocalizeUrlTransformer(
                IUrlTransformer localizeUrlTransformer) {
                super.setLocalizeUrlTransformer(localizeUrlTransformer);
                return this;
            }

            @Override
            public ActionRequest.Builder setPageSetConfig(
                PageSetConfig pageSetConfig) {
                return (ActionRequest.Builder) super
                    .setPageSetConfig(pageSetConfig);
            }

            @Override
            public ActionRequest.Builder setParameter(String key, String value) {
                return (ActionRequest.Builder) super.setParameter(key, value);
            }

            @Override
            public ActionRequest.Builder setParams(Map<String, String> params) {
                return (ActionRequest.Builder) super.setParams(params);
            }

            @Override
            public ActionRequest.Builder setRelativePathUrlTransformer(
                String prefix) {
                super.setRelativePathUrlTransformer(prefix);
                return this;
            }

            @Override
            public ActionRequest.Builder setUrl(Uri url) {
                return (ActionRequest.Builder) super.setUrl(url);
            }

        }

        public final static String DOWNLOAD_STORE = "download";

        public final static String PARAM_ACTION = "action";

        public final static String RESULT_STORE = "results";

        public static ActionRequest.Builder builder() {
            return new ActionRequest.Builder();
        }

        public static ActionRequest.Builder builder(ActionRequest request) {
            return new ActionRequest.Builder(request);
        }

        protected IWrfResource fInitialResource;

        public ActionRequest() {
            super();
        }

        public ActionRequest(ActionRequest.Builder context) {
            super(context);
            if (context != null) {
                fInitialResource = context.fInitialResource;
            }
        }

        public String getActionName() {
            String actionName = normalize(getParameter(PARAM_ACTION));
            return actionName;
        }

        public IWrfResource getInitialResource() {
            if (fInitialResource == null) {
                fInitialResource = getResource(DOWNLOAD_STORE, null);
            }
            return fInitialResource;
        }

        public IWrfResource newTargetResource(String mimeType) {
            String actionName = getActionName();
            String suffix = mimeType + "/" + actionName;
            return getResource(RESULT_STORE, suffix);
        }

    }

    public static class ActionResponse {

        private IWrfResource fResultResource;

        private HttpStatusCode fResultStatus;

        public ActionResponse() {
        }

        public InputStream getResponseContent() throws IOException {
            IContentAdapter adapter = getResultResource().getAdapter(
                IContentAdapter.class);
            return adapter.getContentInput();
        }

        public IWrfResource getResultResource() {
            return fResultResource;
        }

        public HttpStatusCode getResultStatus() {
            return fResultStatus;
        }

        public ActionResponse setResultResource(IWrfResource resultResource) {
            fResultResource = resultResource;
            return this;
        }

        public ActionResponse setResultStatus(HttpStatusCode resultStatus)
            throws IOException {
            fResultStatus = resultStatus;
            CachedResourceAdapter adapter = fResultResource
                .getAdapter(CachedResourceAdapter.class);
            adapter.setStatus(resultStatus);
            adapter.touch();
            return this;
        }

    }

    static String normalize(String str) {
        if (str == null) {
            return "";
        }
        str = str.trim();
        str = str.replace('\\', '/');
        if (str.startsWith("/")) {
            str = str.substring(1);
        }
        if (str.endsWith("/")) {
            str = str.substring(0, str.length() - 1);
        }
        return str;
    }

    public ProcessResource(ActionRequest request) {
        super(request);
    }
}