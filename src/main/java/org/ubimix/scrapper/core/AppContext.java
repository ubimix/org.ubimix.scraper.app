/**
 * 
 */
package org.ubimix.scrapper.core;

import java.io.File;

import org.ubimix.commons.adapters.AdaptableObject;
import org.ubimix.commons.adapters.AdapterFactoryUtils;
import org.ubimix.commons.adapters.CompositeAdapterFactory;
import org.ubimix.commons.uri.Path;
import org.ubimix.commons.uri.Uri;
import org.ubimix.commons.uri.UriToPath;
import org.ubimix.resources.IWrfRepository;
import org.ubimix.resources.IWrfResource;
import org.ubimix.resources.IWrfResourceProvider;
import org.ubimix.resources.impl.WrfResourceRepository;

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

    public IWrfResource getResource(String storeName, Path path) {
        IWrfResourceProvider store = fResourceRepository.getResourceProvider(
            storeName,
            true);
        IWrfResource resource = store.getResource(path, true);
        return resource;
    }

    public IWrfResource getResource(String storeName, Uri url) {
        return getResource(storeName, url, null);
    }

    public IWrfResource getResource(String storeName, Uri url, String suffix) {
        IWrfResourceProvider store = fResourceRepository.getResourceProvider(
            storeName,
            true);
        // Transform the full URL into a path
        Path targetResultPath = UriToPath.getPath(url);
        if (suffix != null) {
            Path.Builder pathBuilder = targetResultPath.getBuilder();
            pathBuilder.appendPath("$").appendPath(suffix);
            targetResultPath = pathBuilder.build();
        }
        IWrfResource targetResource = store.getResource(targetResultPath, true);
        return targetResource;
    }

    public IWrfRepository getResourceRepository() {
        return fResourceRepository;
    }

    protected void initAdapters() {
        addAdapter(DownloadAdapter.class);
        addAdapter(DocAdapter.class);
        addAdapter(MapAdapter.class);
    }

}
