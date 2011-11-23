package org.webreformatter.resources;

import org.webreformatter.commons.adapters.IAdaptableObject;
import org.webreformatter.commons.uri.Path;

/**
 * @author kotelnikov
 */
public interface IWrfResourceProvider extends IAdaptableObject {

    IWrfResource getResource(Path path, boolean create);

}