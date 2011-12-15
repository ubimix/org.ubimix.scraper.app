package org.webreformatter.scrapper.events;

import org.webreformatter.resources.IWrfResource;
import org.webreformatter.resources.adapters.cache.CachedResourceAdapter;
import org.webreformatter.resources.adapters.html.HTMLAdapter;
import org.webreformatter.resources.adapters.xml.XmlAdapter;
import org.webreformatter.scrapper.events.ProcessResource.ActionRequest;
import org.webreformatter.scrapper.events.ProcessResource.ActionResponse;
import org.webreformatter.scrapper.normalizer.IDocumentNormalizer;
import org.webreformatter.scrapper.protocol.HttpStatusCode;
import org.webreformatter.server.xml.XmlWrapper;
import org.webreformatter.server.xml.atom.AtomFeed;

/**
 * @author kotelnikov
 */
public class GetAtomFeedHandler extends ProcessResourceHandler<GetAtomFeed> {

    private IDocumentNormalizer fNormalizer;

    public GetAtomFeedHandler(IDocumentNormalizer documentNormalizer) {
        fNormalizer = documentNormalizer;
    }

    @Override
    public String[] getActionNames() {
        return array("atom");
    }

    @Override
    public String[] getMimeTypes() {
        return array("text/html");
    }

    @Override
    protected void handleRequest(GetAtomFeed event) {
        ActionResponse response = new ActionResponse();
        try {
            ActionRequest request = event.getRequest();
            IWrfResource htmlResource = request.getInitialResource();
            IWrfResource xmlResource = setResponseResource(response, request);
            HttpStatusCode status = HttpStatusCode.STATUS_200;
            if (request.isExpired(xmlResource)) {
                status = HttpStatusCode.STATUS_500;
                HTMLAdapter htmlAdapter = htmlResource
                    .getAdapter(HTMLAdapter.class);
                XmlWrapper doc = htmlAdapter.getWrapper();
                if (doc != null) {
                    AtomFeed feed = fNormalizer.getNormalizedContent(
                        request,
                        doc);
                    XmlAdapter xmlAdapter = xmlResource
                        .getAdapter(XmlAdapter.class);
                    xmlAdapter = xmlResource.getAdapter(XmlAdapter.class);
                    xmlAdapter.setDocument(feed);
                    CachedResourceAdapter adapter = xmlResource
                        .getAdapter(CachedResourceAdapter.class);
                    adapter.updateMetadataFrom(htmlResource);
                    status = HttpStatusCode.STATUS_200;
                }
            }
            response.setResultResource(xmlResource);
            response.setResultStatus(status);
        } catch (Throwable t) {
            handleError("Can not transform the requested resource to Atom.", t);
        } finally {
            event.setResponse(response);
        }
    }

}