package org.webreformatter.scrapper.events;

import org.webreformatter.resources.IWrfResource;
import org.webreformatter.resources.adapters.cache.CachedResourceAdapter;
import org.webreformatter.scrapper.events.ProcessResource.ActionRequest;
import org.webreformatter.scrapper.events.ProcessResource.ActionResponse;
import org.webreformatter.scrapper.protocol.HttpStatusCode;

/**
 * @author kotelnikov
 */
public class CopyResourceHandler extends ProcessResourceHandler<CopyResourceAction> {

    @Override
    public String[] getActionNames() {
        return array("");
    }

    @Override
    public String[] getMimeTypes() {
        return array("");
    }

    @Override
    protected void handleRequest(CopyResourceAction event) {
        ActionResponse response = new ActionResponse();
        try {
            ActionRequest request = event.getRequest();
            IWrfResource from = request.getInitialResource();
            response.setResultResource(from);
            CachedResourceAdapter cache = from
                .getAdapter(CachedResourceAdapter.class);
            HttpStatusCode status = cache.getStatus();
            response.setResultStatus(status);

            // IWrfResource to = setResponseResource(response, request);
            //
            // IContentAdapter fromContent = from
            // .getAdapter(IContentAdapter.class);
            // IContentAdapter toContent = to.getAdapter(IContentAdapter.class);
            // InputStream input = fromContent.getContentInput();
            // try {
            // OutputStream output = toContent.getContentOutput();
            // IOUtil.copy(input, output);
            // } finally {
            // input.close();
            // }
            // response.setResultStatus(HttpStatusCode.STATUS_200);
        } catch (Throwable t) {
            handleError("Can not copy resource", t);
            event.onError(t);
        } finally {
            event.setResponse(response);
        }
    }

}