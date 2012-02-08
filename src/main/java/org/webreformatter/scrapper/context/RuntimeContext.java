/**
 * 
 */
package org.webreformatter.scrapper.context;

import java.io.IOException;

import org.webreformatter.commons.adapters.IAdapterFactory;
import org.webreformatter.commons.uri.Uri;
import org.webreformatter.resources.IWrfResource;
import org.webreformatter.resources.adapters.cache.CachedResourceAdapter;

/**
 * @author kotelnikov
 */
public class RuntimeContext extends AbstractContext {

    protected RuntimeContext(RuntimeContext runtimeContext) {
        this(runtimeContext.getSessionContext());
        copyFrom(runtimeContext);
    }

    protected RuntimeContext(SessionContext sessionContext) {
        setValue(SessionContext.class, sessionContext);
    }

    @Override
    protected void checkFields() {
        assertTrue(
            "Session context can not be null",
            getSessionContext() != null);
        assertTrue("URL can not be empty", getUrl() != null);
    }

    @Override
    public IAdapterFactory getAdapterFactory() {
        return getSessionContext().getAdapterFactory();
    }

    public ApplicationContext getApplicationContext() {
        SessionContext sessionContext = getSessionContext();
        return sessionContext.getApplicationContext();
    }

    @Override
    public AbstractContext getParentContext() {
        return getSessionContext();
    }

    public IWrfResource getResource(String storeName) {
        return getResource(storeName, null);
    }

    public IWrfResource getResource(String storeName, String suffix) {
        Uri url = getUrl();
        return getSessionContext().getResource(storeName, url, suffix);
    }

    public SessionContext getSessionContext() {
        return getValue(SessionContext.class);
    }

    public Uri getUrl() {
        return getValue("$url$");
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
        return new RuntimeContext(this);
    }

    public <T extends RuntimeContext> T setUrl(Uri url) {
        return setValue("$url$", url);
    }

}
