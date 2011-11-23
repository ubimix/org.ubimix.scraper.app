/**
 * 
 */
package org.webreformatter.scrapper.events;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.webreformatter.commons.events.calls.CallListener;
import org.webreformatter.resources.IWrfResource;
import org.webreformatter.resources.adapters.cache.CachedResourceAdapter;
import org.webreformatter.resources.adapters.mime.MimeTypeAdapter;
import org.webreformatter.scrapper.events.ProcessResource.ActionRequest;
import org.webreformatter.scrapper.events.ProcessResource.ActionResponse;

/**
 * @author kotelnikov
 */
public abstract class ProcessResourceHandler<E extends ProcessResource>
    extends
    CallListener<E> {

    private final static Logger log = Logger
        .getLogger(ProcessResourceHandler.class.getName());

    protected static String[] array(String... array) {
        return array;
    }

    public abstract String[] getActionNames();

    public abstract String[] getMimeTypes();

    protected IOException handleError(String msg, Throwable e) {
        log.log(Level.FINE, msg, e);
        if (e instanceof IOException) {
            return (IOException) e;
        }
        return new IOException(msg, e);
    }

    protected IWrfResource setResponseResource(
        ActionResponse response,
        ActionRequest request) throws IOException {
        IWrfResource from = request.getInitialResource();
        MimeTypeAdapter mimeTypeAdapter = from
            .getAdapter(MimeTypeAdapter.class);
        String mimeType = mimeTypeAdapter.getMimeType();
        IWrfResource to = request.newTargetResource(mimeType);
        response.setResultResource(to);
        CachedResourceAdapter adapter = to
            .getAdapter(CachedResourceAdapter.class);
        adapter.copyPropertiesFrom(request.getInitialResource());
        return to;
    }
}
