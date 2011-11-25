/**
 * 
 */
package org.webreformatter.scrapper.context;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.webreformatter.commons.uri.Path;
import org.webreformatter.commons.uri.Uri;
import org.webreformatter.commons.uri.UriToPath;
import org.webreformatter.pageset.AccessManager;
import org.webreformatter.pageset.IUrlMapper;
import org.webreformatter.pageset.IUrlTransformer;
import org.webreformatter.pageset.PageSetConfig;
import org.webreformatter.resources.IWrfRepository;
import org.webreformatter.resources.IWrfResource;
import org.webreformatter.resources.IWrfResourceProvider;
import org.webreformatter.resources.adapters.cache.CachedResourceAdapter;
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

    public boolean getParameter(String key, boolean defaultValue) {
        String str = getParameter(key);
        boolean result = defaultValue;
        if (str != null) {
            str = str.trim().toLowerCase();
            result = "1".equals(str) || "yes".equals(str) || "ok".equals(str);
        }
        return result;
    }

    public int getParameter(String key, int defaultValue) {
        String str = getParameter(key);
        int result = defaultValue;
        if (str != null) {
            str = str.trim();
            try {
                result = Integer.parseInt(str);
            } catch (Throwable t) {
            }
        }
        return result;
    }

    public Map<String, String> getParams() {
        return fParams;
    }

    protected IWrfResource getResource(String storeName, String suffix) {
        ApplicationContext applicationContext = getApplicationContext();
        IWrfRepository repository = applicationContext.getRepository();
        IWrfResourceProvider store = repository.getResourceProvider(
            storeName,
            true);
        Path path = UriToPath.getPath(getUrl());
        Path.Builder builder = path.getBuilder();
        if (suffix != null) {
            builder.appendPath("$").appendPath(suffix);
        }
        Path targetResultPath = builder.build();
        IWrfResource targetResource = store.getResource(targetResultPath, true);
        return targetResource;

    }

    public IUrlMapper getUriMapper() {
        return fPageSetConfig.getUriMapper();
    }

    public Uri getUrl() {
        return fUrl;
    }

    public boolean isExpired(IWrfResource resource) throws IOException {
        boolean result = true;
        boolean cache = getParameter("cache", true);
        if (cache) {
            CachedResourceAdapter cacheAdapter = resource
                .getAdapter(CachedResourceAdapter.class);
            result = cacheAdapter.isExpired();
        }
        return result;
    }

    public void touch(IWrfResource resource) throws IOException {
        CachedResourceAdapter cacheAdapter = resource
            .getAdapter(CachedResourceAdapter.class);
        cacheAdapter.touch();
    }

}
