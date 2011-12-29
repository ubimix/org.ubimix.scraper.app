package org.webreformatter.resources;

/**
 * @author kotelnikov
 */
public class WrfResourceAdapter implements IWrfResourceAdapter {

    /**
     * The internal resource. This adapter is used for this resource.
     */
    protected final IWrfResource fResource;

    public WrfResourceAdapter(IWrfResource resource) {
        fResource = resource;
    }

    public IWrfResource getResource() {
        return fResource;
    }

    public void handleEvent(Object event) {
    }

}
