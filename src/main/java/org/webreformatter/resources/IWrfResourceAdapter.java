/**
 * 
 */
package org.webreformatter.resources;

/**
 * @author kotelnikov
 */
public interface IWrfResourceAdapter {

    /**
     * This method is called by the
     * {@link IWrfResource#notifyAdapters(Object)} method to notify about
     * adapter-wide events.
     * 
     * @param event the fired event
     */
    void handleEvent(Object event);

}
