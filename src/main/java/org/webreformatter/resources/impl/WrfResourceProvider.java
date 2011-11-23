/**
 * 
 */
package org.webreformatter.resources.impl;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import org.webreformatter.commons.adapters.AdaptableObject;
import org.webreformatter.commons.adapters.IAdapterFactory;
import org.webreformatter.commons.adapters.IAdapterRegistry;
import org.webreformatter.commons.uri.Path;
import org.webreformatter.resources.IWrfResource;
import org.webreformatter.resources.IWrfResourceProvider;

/**
 * @author kotelnikov
 */
public class WrfResourceProvider extends AdaptableObject
    implements
    IWrfResourceProvider {

    private Map<Path, WeakReference<IWrfResource>> fCache = new HashMap<Path, WeakReference<IWrfResource>>();

    private File fRoot;

    /**
     * @param root
     * @param conf
     */
    public WrfResourceProvider(
        File root,
        IAdapterRegistry adapterRegistry,
        IAdapterFactory adapterFactory) {
        super(adapterFactory);
        fRoot = root;
        fRoot.mkdirs();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof WrfResourceProvider)) {
            return false;
        }
        WrfResourceProvider o = (WrfResourceProvider) obj;
        return fRoot.equals(o.fRoot);
    }

    public synchronized IWrfResource getResource(Path link, boolean create) {
        IWrfResource result = null;
        WeakReference<IWrfResource> ref = fCache.get(link);
        if (ref != null) {
            result = ref.get();
            if (result == null) {
                fCache.remove(link);
            }
        }
        if (result == null && create) {
            result = new WrfResource(WrfResourceProvider.this, link);
            ref = new WeakReference<IWrfResource>(result);
            fCache.put(link, ref);
        }
        return result;
    }

    public File getRoot() {
        return fRoot;
    }

    @Override
    public int hashCode() {
        return fRoot.hashCode();
    }

    void removeFromCache(Path path) {
        fCache.remove(path);
    }

    @Override
    public String toString() {
        return fRoot.toString();
    }

}