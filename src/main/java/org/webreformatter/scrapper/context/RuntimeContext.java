/**
 * 
 */
package org.webreformatter.scrapper.context;

import java.util.HashMap;
import java.util.Map;

import org.webreformatter.commons.uri.Uri;
import org.webreformatter.pageset.AccessManager;
import org.webreformatter.pageset.IUrlMapper;
import org.webreformatter.pageset.IUrlTransformer;
import org.webreformatter.pageset.PageSetConfig;
import org.webreformatter.scrapper.events.ProcessResource.ActionRequest;

/**
 * @author kotelnikov
 */
public class RuntimeContext {

    public static class Builder extends RuntimeContext {

        public Builder() {
            this(null);
        }

        public Builder(ActionRequest request) {
            super(request);
        }

        public RuntimeContext build() {
            return new RuntimeContext(this);
        }

        @Override
        protected void checkFields() {
        }

        public RuntimeContext.Builder setApplicationContext(
            ApplicationContext applicationContext) {
            fApplicationContext = applicationContext;
            return this;
        }

        public RuntimeContext.Builder setDownloadUrlTransformer(
            IUrlTransformer downloadUrlTransformer) {
            fDownloadUrlTransformer = downloadUrlTransformer;
            return this;
        }

        public RuntimeContext.Builder setLocalizeUrlTransformer(
            IUrlTransformer localizeUrlTransformer) {
            fLocalizeUrlTransformer = localizeUrlTransformer;
            return this;
        }

        public RuntimeContext.Builder setPageSetConfig(
            PageSetConfig pageSetConfig) {
            fPageSetConfig = pageSetConfig;
            return this;
        }

        public RuntimeContext.Builder setParameter(String key, String value) {
            fParams.put(key, value);
            return this;
        }

        public RuntimeContext.Builder setParams(Map<String, String> params) {
            fParams.clear();
            if (params != null) {
                fParams.putAll(params);
            }
            return this;
        }

        public RuntimeContext.Builder setRelativePathUrlTransformer(
            final String prefix) {
            fLocalizeUrlTransformer = new IUrlTransformer() {
                private Uri docLocalUri;

                private Uri doTransform(Uri uri) {
                    IUrlTransformer transformer = getPageSetConfig()
                        .getLocalizeUrlTransformer();
                    Uri result = transformer.transform(uri);
                    if (result != null && !result.isAbsoluteUri()) {
                        Uri.Builder builder = new Uri.Builder(result);
                        builder.getPathBuilder().appendPath(prefix, true);
                        result = builder.build();
                    }
                    return result;
                }

                public Uri transform(Uri uri) {
                    if (docLocalUri == null) {
                        Uri docUri = getUrl();
                        docLocalUri = doTransform(docUri);
                    }
                    Uri localUri = doTransform(uri);
                    Uri result = docLocalUri.getRelative(localUri);
                    return result;
                }
            };
            return this;
        }

        public RuntimeContext.Builder setUrl(Uri url) {
            fUrl = url;
            return this;
        }
    }

    public static Builder builder() {
        return new RuntimeContext.Builder();
    }

    protected ApplicationContext fApplicationContext;

    protected IUrlTransformer fDownloadUrlTransformer;

    protected IUrlTransformer fLocalizeUrlTransformer;

    protected PageSetConfig fPageSetConfig;

    protected Map<String, String> fParams = new HashMap<String, String>();

    protected Uri fUrl;

    public RuntimeContext() {
    }

    /**
     * 
     */
    protected RuntimeContext(RuntimeContext context) {
        if (context != null) {
            fApplicationContext = context.fApplicationContext;
            fPageSetConfig = context.fPageSetConfig;
            fParams.putAll(context.fParams);
            fUrl = context.fUrl;
            fLocalizeUrlTransformer = context.fLocalizeUrlTransformer;
            fDownloadUrlTransformer = context.fDownloadUrlTransformer;
        }
        checkFields();
    }

    protected void assertTrue(String msg, boolean condition) {
        if (!condition) {
            throw new IllegalArgumentException(msg);
        }
    }

    protected void checkFields() {
        assertTrue(
            "Application context can not be null",
            fApplicationContext != null);
        assertTrue(
            "PageSet configuration can not be empty",
            fPageSetConfig != null);
        assertTrue("URL can not be empty", fUrl != null);
        if (fDownloadUrlTransformer == null) {
            fDownloadUrlTransformer = fPageSetConfig
                .getDownloadUrlTransformer();
        }
        if (fLocalizeUrlTransformer == null) {
            fLocalizeUrlTransformer = fPageSetConfig
                .getLocalizeUrlTransformer();
        }
    }

    public AccessManager getAccessManager() {
        return fPageSetConfig.getAccessManager();
    }

    public ApplicationContext getApplicationContext() {
        return fApplicationContext;
    }

    public IUrlTransformer getDownloadUrlTransformer() {
        return fDownloadUrlTransformer;
    }

    public IUrlTransformer getLocalizeUrlTransformer() {
        return fLocalizeUrlTransformer;
    }

    public PageSetConfig getPageSetConfig() {
        return fPageSetConfig;
    }

    public String getParameter(String key) {
        return fParams.get(key);
    }

    public Map<String, String> getParams() {
        return fParams;
    }

    public IUrlMapper getUriMapper() {
        return fPageSetConfig.getUriMapper();
    }

    public Uri getUrl() {
        return fUrl;
    }

}
