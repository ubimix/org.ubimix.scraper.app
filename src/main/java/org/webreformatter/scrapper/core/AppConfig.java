package org.webreformatter.scrapper.core;

import org.webreformatter.commons.strings.StringUtil.IVariableProvider;
import org.webreformatter.resources.adapters.cache.DateUtil;
import org.webreformatter.scrapper.app.AbstractConfig;

/**
 * @author kotelnikov
 */
public class AppConfig extends AbstractConfig {

    public AppConfig(IVariableProvider propertyProvider) {
        super(propertyProvider);
    }

    public boolean downloadExistingResources() {
        boolean result = getBoolean("downloadExisting", true);
        return result;
    }

    public long getDownloadExpirationTimeout() {
        return getConfigLong("downloadExpirationTimeout", DateUtil.MIN * 2);
    }

    public long getDownloadRefreshTimeout() {
        return getConfigLong("downloadExpirationTimeout", DateUtil.MIN * 2);
    }

    public String getRepositoryPath() {
        return getConfigString("repositoryPath");
    }

    public boolean resetRepository() {
        return getBoolean("resetRepository", false);
    }

}