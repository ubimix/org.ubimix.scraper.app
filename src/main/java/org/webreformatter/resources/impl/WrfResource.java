/**
 * 
 */
package org.webreformatter.resources.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.webreformatter.commons.adapters.AdaptableObject;
import org.webreformatter.commons.io.IOUtil;
import org.webreformatter.commons.uri.Path;
import org.webreformatter.resources.IContentAdapter;
import org.webreformatter.resources.IPropertyAdapter;
import org.webreformatter.resources.IWrfResource;
import org.webreformatter.resources.IWrfResourceAdapter;
import org.webreformatter.resources.IWrfResourceProvider;

/**
 * @author kotelnikov
 */
public class WrfResource extends AdaptableObject implements IWrfResource {

    private Path fPath;

    private WrfResourceProvider fProvider;

    public WrfResource(WrfResourceProvider provider, Path link) {
        super(provider.getAdapterFactory());
        fProvider = provider;
        fPath = link;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof WrfResource)) {
            return false;
        }
        WrfResource o = (WrfResource) obj;
        return fProvider.equals(o.fProvider) && fPath.equals(o.fPath);
    }

    @Override
    public <T> T getAdapter(Class<T> type) {
        return super.getAdapter(type);
    }

    /**
     * @return
     */
    public Iterator<IWrfResource> getChildren() {
        File dir = getResourceDirectory();
        List<IWrfResource> result = new ArrayList<IWrfResource>();
        if (dir.exists()) {
            Path resourcePath = getPath();
            File[] list = dir.listFiles();
            if (list != null) {
                for (File child : list) {
                    if (!child.isDirectory()) {
                        continue;
                    }
                    String name = child.getName();
                    Path p = resourcePath.getBuilder().appendPath(name).build();
                    IWrfResource resource = fProvider.getResource(p, false);
                    result.add(resource);
                }
            }
        }
        return result.iterator();
    }

    /**
     * @see org.webreformatter.resources.IWrfResource#getPath()
     */
    public synchronized Path getPath() {
        return fPath;
    }

    /**
     * @see org.webreformatter.resources.IWrfResource#getProvider()
     */
    public IWrfResourceProvider getProvider() {
        return fProvider;
    }

    protected File getResourceDirectory() {
        return fProvider.getResourceDirectory(fPath);
    }

    public File getResourceFile(String fileName) {
        File dir = getResourceDirectory();
        File file = new File(dir, fileName);
        return file;
    }

    @Override
    public int hashCode() {
        return fPath.hashCode();
    }

    public void notifyAdapters(Object event) {
        Map<Class<?>, Object> adapters = getAdapters();
        for (Map.Entry<Class<?>, Object> entry : adapters.entrySet()) {
            Object adapter = entry.getValue();
            if (adapter instanceof IWrfResourceAdapter) {
                IWrfResourceAdapter resourceAdapter = (IWrfResourceAdapter) adapter;
                resourceAdapter.handleEvent(event);
            }
        }
    }

    public synchronized void remove() {
        ContentAdapter contentAdapter = (ContentAdapter) getAdapter(IContentAdapter.class);
        File dir = getResourceDirectory();
        contentAdapter.remove();
        PropertyAdapter propertyAdapter = (PropertyAdapter) getAdapter(IPropertyAdapter.class);
        propertyAdapter.remove();
        IOUtil.delete(dir);
        fProvider.removeFromCache(fPath);
    }

    @Override
    public synchronized String toString() {
        return "[" + fProvider + "]" + fPath.toString();
    }
}
