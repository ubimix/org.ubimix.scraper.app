package org.webreformatter.scrapper.core;

import org.webreformatter.commons.strings.StringUtil;
import org.webreformatter.commons.strings.StringUtil.IVariableProvider;
import org.webreformatter.resources.adapters.cache.DateUtil;

/**
 * @author kotelnikov
 */
public class AppConfig {

    private IVariableProvider fPropertyProvider;

    public AppConfig(IVariableProvider propertyProvider) {
        fPropertyProvider = propertyProvider;
    }

    public boolean downloadExistingResources() {
        boolean result = getBoolean("downloadExisting", true);
        return result;
    }

    protected boolean getBoolean(String name, boolean defaultValue) {
        String value = getString(name);
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
        return StringUtil.resolvePropertyByKey(name, fPropertyProvider);
    }

    public boolean resetRepository() {
        return getBoolean("resetRepository", false);
    }

}