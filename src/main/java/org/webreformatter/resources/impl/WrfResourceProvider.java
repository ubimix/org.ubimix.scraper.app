/**
 * 
 */
package org.webreformatter.resources.impl;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import org.webreformatter.commons.adapters.AdaptableObject;
import org.webreformatter.commons.uri.Path;
import org.webreformatter.commons.uri.UriUtil;
import org.webreformatter.resources.IWrfRepository;
import org.webreformatter.resources.IWrfResource;
import org.webreformatter.resources.IWrfResourceProvider;

/**
 * @author kotelnikov
 */
public class WrfResourceProvider extends AdaptableObject
    implements
    IWrfResourceProvider {

    private Map<Path, WeakReference<IWrfResource>> fCache = new HashMap<Path, WeakReference<IWrfResource>>();

    private WrfResourceRepository fRepository;

    private File fRoot;

    /**
     * @param root
     * @param conf
     */
    public WrfResourceProvider(WrfResourceRepository repository, File root) {
        super(repository.getAdapterFactory());
        fRepository = repository;
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

    public IWrfRepository getRepository() {
        return fRepository;
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
        if (result == null) {
            if (!create) {
                File dir = getResourceDirectory(link);
                create = dir.exists();
            }
            if (create) {
                result = new WrfResource(WrfResourceProvider.this, link);
                ref = new WeakReference<IWrfResource>(result);
                fCache.put(link, ref);
            }
        }
        return result;
    }

    public File getResourceDirectory(Path path) {
        String escapedLink = UriUtil.toPath(path.toString());
        File rootDir = getRoot();
        File file = new File(rootDir, escapedLink);
        return file;
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
