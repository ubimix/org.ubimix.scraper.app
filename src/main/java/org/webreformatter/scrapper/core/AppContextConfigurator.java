package org.webreformatter.scrapper.core;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.webreformatter.commons.io.IOUtil;
import org.webreformatter.commons.json.JsonValue.IJsonValueFactory;
import org.webreformatter.commons.strings.StringUtil;
import org.webreformatter.commons.strings.StringUtil.IVariableProvider;
import org.webreformatter.commons.uri.Uri;
import org.webreformatter.resources.adapters.cache.CachedResourceAdapter;
import org.webreformatter.scrapper.core.IAccessConfig.ICredentials;

/**
 * @author kotelnikov
 */
public class AppContextConfigurator {

    public static AppContext createAppContext() throws IOException {
        return createAppContext(new IVariableProvider() {
            @Override
            public String getValue(String name) {
                return System.getProperty(name);
            }
        });
    }

    public static AppContext createAppContext(IVariableProvider propertyProvider)
        throws IOException {
        AppConfig appConfig = new AppConfig(propertyProvider);
        IAccessConfig accessConfig = readConfig(
            propertyProvider,
            "accessConfig",
            "./config/access.json",
            AccessConfig.FACTORY);
        AppContextConfigurator configurator = new AppContextConfigurator(
            appConfig);
        AppContext appContext = configurator.getAppContext(accessConfig);
        return appContext;
    }

    private static String getProperty(
        IVariableProvider properyProvider,
        String key,
        String defaultValue) {
        String result = StringUtil.resolvePropertyByKey(key, properyProvider);
        if (result == null && defaultValue != null) {
            result = StringUtil.resolveProperty(defaultValue, properyProvider);
        }
        return result;
    }

    private static <T> T readConfig(
        IVariableProvider propertyProvider,
        String key,
        String defaultValue,
        IJsonValueFactory<T> factory) throws IOException {
        String configFileName = getProperty(propertyProvider, key, defaultValue);
        File configFile = new File(configFileName);
        String serializedConfig = IOUtil.readString(configFile);
        T result = factory.newValue(serializedConfig);
        return result;
    }

    private AppConfig fConfig;

    public AppContextConfigurator(AppConfig config) {
        fConfig = config;
    }

    public AppContext getAppContext(IAccessConfig accessConfig) {
        String path = fConfig.getRepositoryPath();
        boolean resetRepository = fConfig.resetRepository();
        AppContext appContext = new AppContext(path, resetRepository);
        CachedResourceAdapter.setRefreshTimeout(fConfig
            .getDownloadRefreshTimeout());
        CachedResourceAdapter.setExpirationTimeout(fConfig
            .getDownloadRefreshTimeout());
        DownloadAdapter downloadAdapter = appContext
            .getAdapter(DownloadAdapter.class);
        downloadAdapter.downloadExistingResources(fConfig
            .downloadExistingResources());
        initDownloadAdapter(downloadAdapter, accessConfig);
        return appContext;
    }

    protected void initDownloadAdapter(
        DownloadAdapter downloadAdapter,
        IAccessConfig accessConfig) {
        final Set<Uri> internalUrls = new HashSet<Uri>();
        List<ICredentials> credentials = accessConfig.getCredentials();
        for (ICredentials credential : credentials) {
            Uri url = credential.getBaseDomain();
            internalUrls.add(url);
            downloadAdapter.addCredentials(
                url,
                credential.getLogin(),
                credential.getPassword());
        }
    }

}