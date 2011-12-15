package org.webreformatter.scrapper.events;

import java.io.InputStream;
import java.io.OutputStream;

import org.webreformatter.commons.io.IOUtil;
import org.webreformatter.resources.IContentAdapter;
import org.webreformatter.resources.IWrfResource;
import org.webreformatter.scrapper.events.ProcessResource.ActionRequest;
import org.webreformatter.scrapper.events.ProcessResource.ActionResponse;
import org.webreformatter.scrapper.protocol.HttpStatusCode;

/**
 * @author kotelnikov
 */
public class CallImageHandler extends ProcessResourceHandler<CallImage> {
    @Override
    public String[] getActionNames() {
        return array("");
    }

    @Override
    public String[] getMimeTypes() {
        return array("image");
    }

    @Override
    protected void handleRequest(CallImage event) {
        ActionResponse response = new ActionResponse();
        try {
            ActionRequest request = event.getRequest();
            IWrfResource from = request.getInitialResource();
            IWrfResource to = setResponseResource(response, request);

            IContentAdapter fromContent = from
                .getAdapter(IContentAdapter.class);
            IContentAdapter toContent = to.getAdapter(IContentAdapter.class);
            InputStream input = fromContent.getContentInput();
            try {
                OutputStream output = toContent.getContentOutput();
                IOUtil.copy(input, output);
            } finally {
                input.close();
            }
            response.setResultStatus(HttpStatusCode.STATUS_200);
        } catch (Throwable t) {
            handleError("Can not copy resource", t);
            event.onError(t);
        } finally {
            event.setResponse(response);
        }
    }
}