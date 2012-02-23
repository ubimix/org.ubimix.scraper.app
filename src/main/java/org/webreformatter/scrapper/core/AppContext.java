/**
 * 
 */
package org.webreformatter.scrapper.core;

import java.io.File;

import org.webreformatter.commons.adapters.AdaptableObject;
import org.webreformatter.commons.adapters.AdapterFactoryUtils;
import org.webreformatter.commons.adapters.CompositeAdapterFactory;
import org.webreformatter.commons.digests.Sha1Digest;
import org.webreformatter.commons.uri.Path;
import org.webreformatter.commons.uri.Uri;
import org.webreformatter.commons.uri.UriToPath;
import org.webreformatter.resources.IWrfRepository;
import org.webreformatter.resources.IWrfResource;
import org.webreformatter.resources.IWrfResourceProvider;
import org.webreformatter.resources.impl.WrfResourceRepository;

/**
 * This class is the parent for all application-specific configurators.
 * 
 * @author kotelnikov
 */
public class AppContext extends AdaptableObject {

    private IWrfRepository fResourceRepository;

    public AppContext() {
        this("./data", false);
    }

    public AppContext(
        CompositeAdapterFactory adapters,
        IWrfRepository repository) {
        super(adapters);
        initAdapters();
        fResourceRepository = repository;
    }

    public AppContext(String repositoryPath, boolean reset) {
        this(new CompositeAdapterFactory(), WrfResourceRepository
            .newRepository(new File(repositoryPath), reset));
    }

    protected <T extends AppContextAdapter> AppContext addAdapter(
        Class<? extends AppContextAdapter> type) {
        CompositeAdapterFactory registry = getAdapterFactory();
        AdapterFactoryUtils.registerAdapter(registry, AppContext.class, type);
        return this;
    }

    @Override
    public CompositeAdapterFactory getAdapterFactory() {
        return (CompositeAdapterFactory) super.getAdapterFactory();
    }

    public IWrfResource getResource(String storeName, Uri url) {
        return getResource(storeName, url, null);
    }

    public IWrfResource getResource(String storeName, Uri url, String suffix) {
        IWrfResourceProvider store = fResourceRepository.getResourceProvider(
            storeName,
            true);
        int maxLen = 150;
        String str = url.toString();
        if (str.length() > maxLen) {
            Sha1Digest digest = Sha1Digest.builder().update(str).build();
            str = str.substring(0, maxLen) + "/---/" + digest;
            url = new Uri(str);
        }
        Path path = UriToPath.getPath(url);
        Path.Builder builder = path.getBuilder();
        if (suffix != null) {
            builder.appendPath("$").appendPath(suffix);
        }
        Path targetResultPath = builder.build();
        IWrfResource targetResource = store.getResource(targetResultPath, true);
        return targetResource;
    }

    protected void initAdapters() {
        addAdapter(DownloadAdapter.class);
        addAdapter(DocAdapter.class);
        addAdapter(MapAdapter.class);
    }

}
