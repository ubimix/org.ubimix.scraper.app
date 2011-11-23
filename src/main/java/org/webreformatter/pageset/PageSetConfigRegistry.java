/**
 * 
 */
package org.webreformatter.pageset;

import java.util.HashMap;
import java.util.Map;

/**
 * This is a registry of {@link PageSetConfig} used to register/retrieve page
 * set configurations by their unique keys.
 * 
 * @author kotelnikov
 */
public class PageSetConfigRegistry {

    private Map<String, PageSetConfig> fRegistry = new HashMap<String, PageSetConfig>();

    /**
     * 
     */
    public PageSetConfigRegistry() {
    }

    /**
     * Returns a registered {@link PageSetConfig} instance corresponding to the
     * specified key or <code>null</code> if no configurations were found for
     * the given key.
     * 
     * @param key the key of the page set configuration
     * @return a {@link PageSetConfig} instance corresponding to the specified
     *         key
     */
    public PageSetConfig getPageSetConfig(String key) {
        synchronized (fRegistry) {
            return fRegistry.get(key);
        }
    }

    /**
     * Registers a new page set configuration corresponding to the specified
     * key.
     * 
     * @param key the key of the configuration
     * @param config the configuration object to register
     */
    public void registerPageSetConfig(String key, PageSetConfig config) {
        synchronized (fRegistry) {
            fRegistry.put(key, config);
        }
    }

    /**
     * Removes a configuration corresponding to the specified key and returns
     * it.
     * 
     * @param key the key of the configuration to remove
     * @return a removed {@link PageSetConfig} instance corresponding to the
     *         specified key
     */
    public PageSetConfig unregisterPageSetConfig(String key) {
        synchronized (fRegistry) {
            return fRegistry.remove(key);
        }
    }

}
