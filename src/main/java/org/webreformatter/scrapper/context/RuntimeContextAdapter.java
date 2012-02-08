/**
 * 
 */
package org.webreformatter.scrapper.context;

/**
 * @author kotelnikov
 */
public class RuntimeContextAdapter
    extends
    AbstractContext.ContextAdapter<RuntimeContext> {

    protected static final String RESOURCE_DOWNLOAD = "download";

    /**
     * 
     */
    public RuntimeContextAdapter(RuntimeContext context) {
        super(context);
    }

}
