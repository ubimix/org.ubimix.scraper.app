/**
 * 
 */
package org.webreformatter.resources.impl;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.webreformatter.commons.adapters.AdaptableObject;
import org.webreformatter.commons.adapters.AdapterFactoryUtils;
import org.webreformatter.commons.adapters.IAdapterFactory;
import org.webreformatter.commons.adapters.IAdapterRegistry;
import org.webreformatter.resources.IContentAdapter;
import org.webreformatter.resources.IPropertyAdapter;
import org.webreformatter.resources.IWrfRepository;
import org.webreformatter.resources.IWrfResource;
import org.webreformatter.resources.IWrfResourceProvider;

/**
 * @author kotelnikov
 */
public class WrfResourceRepository extends AdaptableObject
    implements
    IWrfRepository {

    private IAdapterRegistry fAdapterRegistry;

    private Map<String, IWrfResourceProvider> fCache = new HashMap<String, IWrfResourceProvider>();

    private File fRoot;

    public WrfResourceRepository(
        IAdapterRegistry adapterRegistry,
        IAdapterFactory adapterFactory,
        File root) {
        super(adapterFactory);
        fAdapterRegistry = adapterRegistry;
        fRoot = root;
        initDefaultAdapters();
    }

    public <T extends IAdapterRegistry & IAdapterFactory> WrfResourceRepository(
        T adapters,
        File root) {
        this(adapters, adapters, root);
    }

    public IWrfResourceProvider getResourceProvider(String key, boolean create) {
        synchronized (fCache) {
            IWrfResourceProvider provider = fCache.get(key);
            if (provider == null) {
                File dir = getResourceProviderDir(key);
                if (dir.exists() || create) {
                    IAdapterFactory adapterFactory = getAdapterFactory();
                    provider = new WrfResourceProvider(
                        dir,
                        fAdapterRegistry,
                        adapterFactory);
                    fCache.put(key, provider);
                }
            }
            return provider;
        }
    }

    private File getResourceProviderDir(String key) {
        return new File(fRoot, key);
    }

    protected void initDefaultAdapters() {
        registerResourceAdapter(IContentAdapter.class, ContentAdapter.class);
        registerResourceAdapter(IPropertyAdapter.class, PropertyAdapter.class);
    }

    public void registerResourceAdapter(Class<?> adapterType) {
        registerResourceAdapter(adapterType, adapterType);
    }

    public void registerResourceAdapter(
        Class<?> adapterInterface,
        Class<?> adapterImpl) {
        AdapterFactoryUtils.registerAdapter(
            fAdapterRegistry,
            IWrfResource.class,
            adapterInterface,
            adapterImpl);
    }

    public void registerResourceAdapter(
        Class<?> adapterType,
        IAdapterFactory adapterFactory) {
        fAdapterRegistry.registerAdapterFactory(
            adapterFactory,
            IWrfResource.class);
    }

}
