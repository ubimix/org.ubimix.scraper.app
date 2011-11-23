package org.webreformatter.resources;

import java.util.Iterator;

import org.webreformatter.commons.adapters.IAdaptableObject;
import org.webreformatter.commons.uri.Path;

/**
 * The common interface for all resources.
 * 
 * @author kotelnikov
 */
public interface IWrfResource extends IAdaptableObject {

    /**
     * Returns an iterator over child resources. Each child resource has the
     * path starting with the parent path.
     * 
     * @return an iterator over child resource
     */
    Iterator<IWrfResource> getChildren();

    /**
     * Returns the path of this resource
     * 
     * @return the path of this resource
     */
    Path getPath();

    /**
     * Returns the provider managing this resource node
     * 
     * @return the provider managing this resource node
     */
    IWrfResourceProvider getProvider();

    /**
     * This method is used to notify all registered/loaded adapters of this
     * resource.
     * 
     * @param event the event used to notify adapters.
     */
    void notifyAdapters(Object event);

    /**
     * Removes this resource from the storage.
     */
    void remove();

}