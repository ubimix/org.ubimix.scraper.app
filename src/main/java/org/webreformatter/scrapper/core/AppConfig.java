package org.webreformatter.scrapper.core;

import org.webreformatter.commons.json.JsonObject;
import org.webreformatter.commons.json.JsonValue.IJsonValueFactory;
import org.webreformatter.commons.strings.StringUtil.IVariableProvider;
import org.webreformatter.resources.adapters.cache.DateUtil;

/**
 * @author kotelnikov
 */
public class AppConfig {

    public static class CompositePropertyProvider implements IVariableProvider {

        private IVariableProvider[] fProviders;

        public CompositePropertyProvider(IVariableProvider... providers) {
            fProviders = providers;
        }

        @Override
        public String getValue(String name) {
            String value = null;
            for (IVariableProvider provider : fProviders) {
                value = provider.getValue(name);
                if (value != null) {
                    break;
                }
            }
            return value;
        }

    }

    public static class JsonPropertyProvider implements IVariableProvider {

        private JsonObject fObject;

        public JsonPropertyProvider(JsonObject obj) {
            fObject = obj;
        }

        @Override
        public String getValue(String name) {
            return fObject.getString(name);
        }

    }

    public static final IJsonValueFactory<AppConfig> FACTORY = new IJsonValueFactory<AppConfig>() {
        @Override
        public AppConfig newValue(Object object) {
            final JsonObject json = JsonObject.FACTORY.newValue(object);
            return getConfig(json);
        }
    };

    public static AppConfig getConfig(final JsonObject json) {
        IVariableProvider provider = new CompositePropertyProvider(
            new IVariableProvider() {
                @Override
                public String getValue(String name) {
                    return json.getString(name);
                }
            },
            new IVariableProvider() {
                @Override
                public String getValue(String name) {
                    return System.getProperty(name);
                }
            });
        AppConfig config = new AppConfig(provider);
        return config;
    }

    private IVariableProvider fPropertyProvider;

    /**
     * 
     */
    public AppConfig(IVariableProvider propertyProvider) {
        fPropertyProvider = propertyProvider;
    }

    protected boolean getBoolean(String name, boolean defaultValue) {
        String value = getString(name);
        boolean result = defaultValue;
        if (value != null) {
            try {
                value = value.toLowerCase();
                result = "yes".equals(value)
                    || "ok".equals(value)
                    || "1".equals(value);
            } catch (Throwable t) {
            }
        }
        return result;
    }

    public long getDownloadExpirationTimeout() {
        return getLong("downloadExpirationTimeout", DateUtil.MIN * 2);
    }

    public long getDownloadRefreshTimeout() {
        return getLong("downloadExpirationTimeout", DateUtil.MIN * 2);
    }

    protected long getLong(String name, long defaultValue) {
        String value = getString(name);
        long result = defaultValue;
        try {
            result = Long.parseLong(value);
        } catch (Throwable t) {
        }
        return result;
    }

    public String getRepositoryPath() {
        return getString("repositoryPath");
    }

    protected String getString(String name) {
        return fPropertyProvider.getValue(name);
    }

    public boolean noDownload() {
        boolean result = false;
        String str = getString("download");
        if (str != null) {
            str = str.trim();
            str = str.toLowerCase();
            result = "no".equals(str) || "false".equals(str) || "0".equals(str);
        }
        return result;
    }

    public boolean resetRepository() {
        return getBoolean("resetRepository", false);
    }

}