/**
 * 
 */
package org.webreformatter.resources.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.webreformatter.commons.uri.Path;
import org.webreformatter.resources.IHierarchyAdapter;
import org.webreformatter.resources.WrfResourceAdapter;

/**
 * @author kotelnikov
 */
public class HierarchyAdapter extends WrfResourceAdapter
    implements
    IHierarchyAdapter {

    public HierarchyAdapter(WrfResource instance) {
        super(instance);
    }

    /**
     * @return
     */
    public Iterator<Path> getChildren() {
        File dir = getResource().getResourceDirectory();
        List<Path> result = new ArrayList<Path>();
        if (dir.exists()) {
            Path resourcePath = fResource.getPath();
            File[] list = dir.listFiles();
            if (list != null) {
                for (File child : list) {
                    if (!child.isDirectory()) {
                        continue;
                    }
                    String name = child.getName();
                    Path p = resourcePath.getBuilder().appendPath(name).build();
                    result.add(p);
                }
            }
        }
        return result.iterator();
    }

    @Override
    public WrfResource getResource() {
        return (WrfResource) super.getResource();
    }
}
