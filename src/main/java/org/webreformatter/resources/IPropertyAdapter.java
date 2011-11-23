/**
 * 
 */
package org.webreformatter.resources;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * @author kotelnikov
 */
public interface IPropertyAdapter extends IWrfResourceAdapter {

    /**
     * This event is fired when one or more resource properties were changed.
     * 
     * @author kotelnikov
     */
    public class PropertiesChangeEvent extends WrfResourceEvent {
    }

    Map<String, String> getProperties() throws IOException;

    String getProperty(String key) throws IOException;

    Set<String> getPropertyKeys() throws IOException;

    void setProperties(Map<String, String> properties) throws IOException;

    void setProperty(String key, String value) throws IOException;

}
