/**
 * 
 */
package org.webreformatter.resources;

/**
 * @author kotelnikov
 */
public interface IWrfResourceAdapter {

    /**
     * Returns the adaptable resource
     * 
     * @return the adaptable resource
     */
    IWrfResource getResource();

    /**
     * This method is called by the {@link IWrfResource#notifyAdapters(Object)}
     * method to notify about adapter-wide events.
     * 
     * @param event the fired event
     * @see IWrfResource#notifyAdapters(Object)
     */
    void handleEvent(Object event);

}
