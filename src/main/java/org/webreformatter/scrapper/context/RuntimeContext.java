/**
 * 
 */
package org.webreformatter.scrapper.context;

import java.util.HashMap;
import java.util.Map;

import org.webreformatter.commons.uri.Uri;
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

        public RuntimeContext.Builder setUrl(Uri url) {
            fUrl = url;
            return this;
        }
    }

    public static Builder builder() {
        return new RuntimeContext.Builder();
    }

    protected ApplicationContext fApplicationContext;

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
    }

    public ApplicationContext getApplicationContext() {
        return fApplicationContext;
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

    public Uri getUrl() {
        return fUrl;
    }

}
