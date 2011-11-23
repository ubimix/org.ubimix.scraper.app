package org.webreformatter.scrapper.events;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.webreformatter.commons.strings.StringUtil;
import org.webreformatter.commons.uri.Uri;
import org.webreformatter.pageset.AccessManager;
import org.webreformatter.pageset.IUrlMapper;
import org.webreformatter.pageset.IUrlTransformer;
import org.webreformatter.pageset.PageSetConfig;
import org.webreformatter.resources.IWrfResource;
import org.webreformatter.resources.adapters.cache.CachedResourceAdapter;
import org.webreformatter.resources.adapters.string.StringAdapter;
import org.webreformatter.scrapper.context.AtomProcessing;
import org.webreformatter.scrapper.context.CoreAdapter;
import org.webreformatter.scrapper.context.ApplicationContext;
import org.webreformatter.scrapper.context.HttpStatusCode;
import org.webreformatter.scrapper.events.ProcessResource.ActionRequest;
import org.webreformatter.scrapper.events.ProcessResource.ActionResponse;
import org.webreformatter.scrapper.normalizer.XslUtils;
import org.webreformatter.server.xml.atom.AtomEntry;
import org.webreformatter.server.xml.atom.AtomFeed;

/**
 * @author kotelnikov
 */
public class FormatHtmlHandler extends ProcessResourceHandler<FormatHtmlAction> {

    @Override
    public String[] getActionNames() {
        return array("");
    }

    @Override
    public String[] getMimeTypes() {
        return array("text/html");
    }

    @Override
    protected void handleRequest(FormatHtmlAction event) {
        ActionResponse response = new ActionResponse();
        try {
            // Prepare the resulting resource
            ActionRequest request = event.getRequest();
            IWrfResource to = setResponseResource(response, request);

            //
            Uri url = request.getUrl();
            ApplicationContext applicationContext = request.getApplicationContext();
            AtomProcessing atomProcessingAdapter = applicationContext
                .getAdapter(AtomProcessing.class);
            AtomFeed feed = atomProcessingAdapter
                .getResourceAsAtomFeed(request);
            String prefix = XslUtils
                .getNamespacePrefix(
                    feed.getXmlContext().getNamespaceContext(),
                    "http://www.w3.org/1999/xhtml",
                    "xhtml");
            if (prefix != null && !"".equals(prefix)) {
                prefix += ":";
            }
            // Localize references

            PageSetConfig pageSetConfig = request.getPageSetConfig();
            final IUrlTransformer transformer = pageSetConfig
                .getLocalizeUrlTransformer();
            final Uri docLocalUri = transformer.transform(url);
            IUrlTransformer t = new IUrlTransformer() {
                public Uri transform(Uri uri) {
                    Uri localUri = transformer.transform(uri);
                    Uri result = docLocalUri.getRelative(localUri);
                    return result;
                }
            };
            XslUtils.transformLinks(feed, t, url, prefix + "a", "href");
            XslUtils.transformLinks(feed, t, url, prefix + "img", "src");

            // Get template
            IUrlMapper mapper = pageSetConfig.getUriMapper();
            Uri templateUrl = mapper.getUrl("template", url);

            HttpStatusCode status;
            if (templateUrl == null) {
                List<AtomEntry> entries = feed.getEntries();
                AtomEntry entry = entries.get(0);
                String str = entry.getContent();
                StringAdapter resultStringAdapter = response
                    .getResultResource()
                    .getAdapter(StringAdapter.class);
                resultStringAdapter.setContentAsString(str);
                status = HttpStatusCode.STATUS_200;
            } else {
                CoreAdapter coreAdapter = applicationContext
                    .getAdapter(CoreAdapter.class);
                IWrfResource templateResource = coreAdapter.getResource(
                    templateUrl,
                    "download",
                    true);
                CachedResourceAdapter templateResourceCache = templateResource
                    .getAdapter(CachedResourceAdapter.class);
                status = HttpStatusCode.STATUS_404;
                if (!templateResourceCache.isExpired()) {
                    status = HttpStatusCode.STATUS_200;
                } else {
                    CoreAdapter adapter = applicationContext
                        .getAdapter(CoreAdapter.class);
                    AccessManager accessManager = pageSetConfig
                        .getAccessManager();
                    HttpStatusCode templateDownloadStatus = adapter.download(
                        accessManager,
                        templateUrl,
                        templateResource);
                    if (!templateDownloadStatus.isError()) {
                        status = templateDownloadStatus;
                    }
                }
                if (!status.isError()) {
                    // Apply template
                    List<AtomEntry> entries = feed.getEntries();
                    AtomEntry entry = entries.get(0);
                    final Map<String, String> map = new HashMap<String, String>();
                    String str = entry.getContent();
                    map.put("content", str);
                    str = entry.getTitle();
                    map.put("title", str);
                    String template = templateResource.getAdapter(
                        StringAdapter.class).getContentAsString();
                    String content = StringUtil.resolveProperty(
                        template,
                        new StringUtil.IVariableProvider() {
                            public String getValue(String name) {
                                return map.get(name);
                            }
                        });

                    // Copy the result to the output
                    StringAdapter resultStringAdapter = to
                        .getAdapter(StringAdapter.class);
                    resultStringAdapter.setContentAsString(content);
                    status = HttpStatusCode.STATUS_200;
                }
            }
            response.setResultStatus(status);
        } catch (Throwable t) {
            handleError("Can not format the current resource", t);
        } finally {
            event.setResponse(response);
        }
    }

}