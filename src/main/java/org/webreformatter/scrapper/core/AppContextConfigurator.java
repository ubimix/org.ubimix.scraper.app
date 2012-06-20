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
import org.webreformatter.scrapper.core.DownloadAdapter.IUrlTransformer;
import org.webreformatter.scrapper.core.IAccessConfig.ICredentials;

/**
 * @author kotelnikov
 */
public class AppContextConfigurator {

    public static AppContext createAppContext(IVariableProvider properyProvider)
        throws IOException {
        AppConfig appConfig = readConfig(
            properyProvider,
            "app.config.file",
            "./configurations/ebook/app.json",
            AppConfig.FACTORY);
        IAccessConfig accessConfig = readConfig(
            properyProvider,
            "app.access.file",
            "./configurations/ebook/access.json",
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
        downloadAdapter.setNoDownload(fConfig.noDownload());
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
        downloadAdapter.setDownloadUrlTransformer(new IUrlTransformer() {

            protected Uri convertInternalUrl(Uri uri) {
                Uri.Builder builder = uri.getBuilder();
                // Add basicauth parameter to use simple authentication
                builder.addParam("basicauth", "1");
                uri = builder.build();
                return uri;
            }

            protected boolean isInternalUrl(Uri uri) {
                boolean result = false;
                String str = uri.toString();
                for (Uri baseUrl : internalUrls) {
                    String base = baseUrl.toString();
                    result = str.startsWith(base);
                    if (result) {
                        break;
                    }
                }
                return result;
            }

            @Override
            public Uri transform(Uri uri) {
                if (isInternalUrl(uri)) {
                    uri = convertInternalUrl(uri);
                }
                return uri;
            }
        });

    }

}