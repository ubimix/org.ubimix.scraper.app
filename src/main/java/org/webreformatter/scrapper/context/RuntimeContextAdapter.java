/**
 * 
 */
package org.webreformatter.scrapper.context;


/**
 * @author kotelnikov
 */
public class RuntimeContextAdapter {

    protected static final String RESOURCE_DOWNLOAD = "download";

    protected RuntimeContext fRuntimeContext;

    /**
     * 
     */
    public RuntimeContextAdapter(RuntimeContext context) {
        fRuntimeContext = context;
    }

}
