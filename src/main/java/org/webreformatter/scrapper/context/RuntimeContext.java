/**
 * 
 */
package org.webreformatter.scrapper.context;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.webreformatter.commons.adapters.AdaptableObject;
import org.webreformatter.commons.uri.Path;
import org.webreformatter.commons.uri.Uri;
import org.webreformatter.commons.uri.UriToPath;
import org.webreformatter.pageset.IUrlTransformer;
import org.webreformatter.resources.IWrfRepository;
import org.webreformatter.resources.IWrfResource;
import org.webreformatter.resources.IWrfResourceProvider;
import org.webreformatter.resources.adapters.cache.CachedResourceAdapter;
import org.webreformatter.scrapper.protocol.AccessManager;

/**
 * @author kotelnikov
 */
public class RuntimeContext extends AdaptableObject {

    public static class Builder extends RuntimeContext {

        public Builder(ApplicationContext appContext) {
            super(appContext);
        }

        public Builder(RuntimeContext context) {
            super(context);
        }

        public RuntimeContext build() {
            return new RuntimeContext(this);
        }

        @Override
        protected void checkFields() {
        }

        public RuntimeContext.Builder setAccessManager(
                AccessManager accessManager) {
            fAccessManager = accessManager;
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

        public RuntimeContext.Builder setUrl(Uri url) {
            fUrl = url;
            return this;
        }
    }

    public static RuntimeContext.Builder builder(ApplicationContext appContext) {
        return new RuntimeContext.Builder(appContext);
    }

    public static RuntimeContext.Builder builder(RuntimeContext parentContext) {
        return new RuntimeContext.Builder(parentContext);
    }

    protected AccessManager fAccessManager;

    protected ApplicationContext fApplicationContext;

    protected IUrlTransformer fDownloadUrlTransformer;

    protected IUrlTransformer fLocalizeUrlTransformer;

    protected Map<String, String> fParams = new HashMap<String, String>();

    protected Uri fUrl;

    protected RuntimeContext(ApplicationContext appContext) {
        super(appContext.getAdapterFactory());
        fApplicationContext = appContext;
    }

    /**
     * 
     */
    protected RuntimeContext(RuntimeContext context) {
        this(context.fApplicationContext);
        fParams.putAll(context.fParams);
        fUrl = context.fUrl;
        fAccessManager = context.fAccessManager;
        fLocalizeUrlTransformer = context.fLocalizeUrlTransformer;
        fDownloadUrlTransformer = context.fDownloadUrlTransformer;
        checkFields();
    }

    protected void assertTrue(String msg, boolean condition) {
        if (!condition) {
            throw new IllegalArgumentException(msg);
        }
    }

    public RuntimeContext.Builder builder() {
        return builder(this);
    }

    protected void checkFields() {
        assertTrue("Application context can not be null",
                fApplicationContext != null);
        assertTrue("URL can not be empty", fUrl != null);
        if (fDownloadUrlTransformer == null) {
            fDownloadUrlTransformer = IUrlTransformer.EMPTY;
        }
        if (fLocalizeUrlTransformer == null) {
            fLocalizeUrlTransformer = IUrlTransformer.EMPTY;
        }
        if (fAccessManager == null) {
            fAccessManager = new AccessManager();
        }
    }

    public AccessManager getAccessManager() {
        return fAccessManager;
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

    public Uri getLocalPath(Uri pageUrl) {
        Uri result = pageUrl;
        IUrlTransformer transformer = getLocalizeUrlTransformer();
        if (transformer != null) {
            result = transformer.transform(pageUrl);
        }
        return result;
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

    public IWrfResource getResource(String storeName) {
        return getResource(storeName, null);
    }

    public IWrfResource getResource(String storeName, String suffix) {
        Uri url = getUrl();
        return getResource(storeName, url, suffix);

    }

    public IWrfResource getResource(String storeName, Uri url, String suffix) {
        ApplicationContext applicationContext = getApplicationContext();
        IWrfRepository repository = applicationContext.getRepository();
        IWrfResourceProvider store = repository.getResourceProvider(storeName,
                true);
        Path path = UriToPath.getPath(url);
        Path.Builder builder = path.getBuilder();
        if (suffix != null) {
            builder.appendPath("$").appendPath(suffix);
        }
        Path targetResultPath = builder.build();
        IWrfResource targetResource = store.getResource(targetResultPath, true);
        return targetResource;
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

    public RuntimeContext newContext(Uri url) {
        RuntimeContext result = builder(this).setUrl(url).build();
        return result;
    }

    public void touch(IWrfResource resource) throws IOException {
        CachedResourceAdapter cacheAdapter = resource
                .getAdapter(CachedResourceAdapter.class);
        cacheAdapter.touch();
    }

}
