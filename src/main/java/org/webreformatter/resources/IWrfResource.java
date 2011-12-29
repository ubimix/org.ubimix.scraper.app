package org.webreformatter.resources;

import org.webreformatter.commons.adapters.IAdaptableObject;
import org.webreformatter.commons.uri.Path;

/**
 * The common interface for all resources.
 * 
 * @author kotelnikov
 */
public interface IWrfResource extends IAdaptableObject {

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
     * @see IWrfResourceAdapter#handleEvent(Object)
     */
    void notifyAdapters(Object event);

    /**
     * Removes this resource from the storage.
     */
    void remove();

}