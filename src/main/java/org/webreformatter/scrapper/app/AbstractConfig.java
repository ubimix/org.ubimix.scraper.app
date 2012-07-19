/**
 * 
 */
package org.webreformatter.scrapper.app;

import org.webreformatter.commons.json.JsonObject;
import org.webreformatter.commons.strings.StringUtil;
import org.webreformatter.commons.strings.StringUtil.IVariableProvider;

/**
 * @author kotelnikov
 */
public class AbstractConfig {
    public static class CompositeVariableProvider
        implements
        StringUtil.IVariableProvider {

        private IVariableProvider[] fProviders;

        public CompositeVariableProvider(
            StringUtil.IVariableProvider... providers) {
            fProviders = providers;
        }

        @Override
        public String getValue(String name) {
            String result = null;
            for (IVariableProvider provider : fProviders) {
                result = provider.getValue(name);
                if (result != null) {
                    break;
                }
            }
            return result;
        }

    }

    /**
     * @author kotelnikov
     */
    public static class JsonVariableProvider
        implements
        StringUtil.IVariableProvider {

        /**
         * JSON object containing configuration parameters.
         */
        private JsonObject fConfig;

        public JsonVariableProvider(JsonObject config) {
            fConfig = config;
        }

        @Override
        public String getValue(String name) {
            String value = fConfig.getString(name);
            if (value == null) {
                value = System.getProperty(name);
            }
            return value;
        }
    }

    protected IVariableProvider fPropertyProvider;;

    /**
     * @param guideConfig
     */
    public AbstractConfig(IVariableProvider propertyProvider) {
        fPropertyProvider = propertyProvider;
    }

    protected boolean getBoolean(String name, boolean defaultValue) {
        String value = getConfigString(name);
        boolean result = defaultValue;
        if (value != null) {
            try {
                value = value.toLowerCase();
                result = "true".equals(value)
                    || "yes".equals(value)
                    || "ok".equals(value)
                    || "1".equals(value);
            } catch (Throwable t) {
            }
        }
        return result;
    }

    /**
     * Returns a boolean configuration value
     * 
     * @param key the key in the configuration objects
     * @param defaultValue the default value returned if there is no
     *        configuration values defined
     * @return
     */
    protected boolean getConfigBoolean(String key, boolean defaultValue) {
        String str = getConfigString(key);
        return str != null ? "true".equals(str)
            || "1".equals(str)
            || "ok".equals(str) : defaultValue;
    }

    /**
     * Returns the configuration value corresponding to the specified key.
     * 
     * @param key the key of the property to return
     * @param defaultValue the default value to return if the configuration does
     *        not have definition for the specified parameter
     * @return the configuration value corresponding to the specified key
     */
    protected int getConfigInteger(String key, int defaultValue) {
        String value = getConfigString(key);
        int result = defaultValue;
        try {
            result = Integer.parseInt(value);
        } catch (Throwable t) {
        }
        return result;
    }

    /**
     * @param key the key of the property
     * @param defaultValue the default value if the long is not defined
     * @return a long value from the configurations corresponding to the
     *         specified key
     */
    protected long getConfigLong(String key, int defaultValue) {
        String value = getConfigString(key);
        long result = defaultValue;
        try {
            result = Long.parseLong(value);
        } catch (Throwable t) {
        }
        return result;
    }

    /**
     * Returns the configuration value corresponding to the specified key.
     * 
     * @param key the key of configuration field
     * @return the value corresponding to the specified key
     */
    protected String getConfigString(String key) {
        return getConfigString(key, null);
    }

    /**
     * Returns the configuration value corresponding to the specified key. If no
     * values were found then this method returns the resolved default value.
     * 
     * @param key the key of configuration field
     * @param defaultValue the default value to use if the configuration does
     *        not have values for the specified key
     * @return the value corresponding to the specified key
     */
    protected String getConfigString(String key, String defaultValue) {
        String result = StringUtil.resolvePropertyByKey(key, fPropertyProvider);
        if (result == null && defaultValue != null) {
            result = StringUtil
                .resolveProperty(defaultValue, fPropertyProvider);
        }
        return result;
    }

    public IVariableProvider getPropertyProvider() {
        return fPropertyProvider;
    }
}
