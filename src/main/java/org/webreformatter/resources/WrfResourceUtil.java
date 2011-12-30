/**
 * 
 */
package org.webreformatter.resources;

import org.webreformatter.commons.uri.Path;

/**
 * @author kotelnikov
 */
public class WrfResourceUtil {

    public static IWrfResource getResource(IWrfResource resource, String name) {
        Path path = resource.getPath();
        return resource
            .getProvider()
            .getRepository()
            .getResourceProvider(name, true)
            .getResource(path, true);
    }

}
