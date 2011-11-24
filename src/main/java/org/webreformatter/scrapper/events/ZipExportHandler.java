package org.webreformatter.scrapper.events;

import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.webreformatter.commons.events.IEventManager;
import org.webreformatter.commons.events.calls.CallListener;
import org.webreformatter.commons.events.server.CallBarrier;
import org.webreformatter.commons.uri.Path;
import org.webreformatter.commons.uri.Uri;
import org.webreformatter.pageset.IUrlTransformer;
import org.webreformatter.resources.IContentAdapter;
import org.webreformatter.resources.IPropertyAdapter;
import org.webreformatter.resources.IWrfResource;
import org.webreformatter.scrapper.context.ApplicationContext;
import org.webreformatter.scrapper.context.AtomProcessing;
import org.webreformatter.scrapper.context.HttpStatusCode;
import org.webreformatter.scrapper.events.ProcessResource.ActionRequest;
import org.webreformatter.scrapper.events.ProcessResource.ActionResponse;
import org.webreformatter.scrapper.normalizer.XslUtils;
import org.webreformatter.server.xml.XmlWrapper;
import org.webreformatter.server.xml.atom.AtomFeed;

public class ZipExportHandler extends ProcessResourceHandler<ZipExportAction> {

    private void extractReferences(
        AtomFeed feed,
        Uri baseUri,
        IUrlTransformer siteConfig,
        Map<Uri, Uri> pathToUrlMap,
        String tagName,
        String attrName) {
        String prefix = XslUtils.getNamespacePrefix(feed
            .getXmlContext()
            .getNamespaceContext(), "http://www.w3.org/1999/xhtml", "xhtml");
        if (prefix != null && !"".equals(prefix)) {
            prefix += ":";
        }
        List<XmlWrapper> list = XslUtils.getElementList(feed, prefix + tagName);
        for (XmlWrapper tag : list) {
            String str = tag.getAttribute(attrName);
            if (str != null) {
                Uri.Builder urlBuilder = new Uri.Builder(str).setFragment(null);
                Uri url = urlBuilder.build();
                Uri path = siteConfig.transform(url);
                if (path != null) {
                    pathToUrlMap.put(path, url);
                }
            }
        }
    }

    @Override
    public String[] getActionNames() {
        return array("zip");
    }

    @Override
    public String[] getMimeTypes() {
        return array("text/html");
    }

    @Override
    protected void handleRequest(ZipExportAction event) {
        ActionResponse response = new ActionResponse();
        try {
            ActionRequest request = event.getRequest();
            Uri docUrl = request.getUrl();
            ApplicationContext applicationContext = request
                .getApplicationContext();
            AtomProcessing atomProcessingAdapter = applicationContext
                .getAdapter(AtomProcessing.class);
            AtomFeed feed = atomProcessingAdapter
                .getResourceAsAtomFeed(request);

            Map<Uri, Uri> pathToUrlMap = new TreeMap<Uri, Uri>(
                new Comparator<Uri>() {
                    public int compare(Uri o1, Uri o2) {
                        return o1.toString().compareTo(o2.toString());
                    }
                });

            IUrlTransformer uriTransformer = request
                .getDownloadUrlTransformer();
            Uri docPath = uriTransformer != null ? uriTransformer
                .transform(docUrl) : null;
            pathToUrlMap.put(docPath, docUrl);

            extractReferences(
                feed,
                docUrl,
                uriTransformer,
                pathToUrlMap,
                "a",
                "href");
            extractReferences(
                feed,
                docUrl,
                uriTransformer,
                pathToUrlMap,
                "img",
                "src");

            IEventManager eventManager = applicationContext.getEventManager();

            final Map<Uri, IWrfResource> resourceMap = new HashMap<Uri, IWrfResource>();
            Map<String, String> params = new HashMap<String, String>();
            final CallBarrier callBarrier = new CallBarrier();
            for (Uri url : pathToUrlMap.values()) {
                ActionRequest newRequest = ActionRequest
                    .builder(request)
                    .setUrl(url)
                    .setParams(params)
                    .build();
                eventManager.fireEvent(
                    new ApplyAction(newRequest),
                    callBarrier.add(new CallListener<ProcessResource>() {
                        @Override
                        protected void handleResponse(ProcessResource event) {
                            if (!event.hasErrors()) {
                                ActionResponse actionResponse = event
                                    .getResponse();
                                HttpStatusCode status = actionResponse
                                    .getResultStatus();
                                if (!status.isError()) {
                                    IWrfResource resource = actionResponse
                                        .getResultResource();
                                    Uri url = event.getRequest().getUrl();
                                    resourceMap.put(url, resource);
                                }
                            }
                        }
                    }));
            }
            callBarrier.await();

            // Copy all loaded resources to the ZIP
            IWrfResource resultResource = setResponseResource(response, request);
            IContentAdapter content = resultResource
                .getAdapter(IContentAdapter.class);
            OutputStream output = content.getContentOutput();
            try {
                int BUFFER_SIZE = 1024 * 10;
                byte[] buf = new byte[BUFFER_SIZE];
                ZipOutputStream out = new ZipOutputStream(
                    new BufferedOutputStream(output, BUFFER_SIZE));
                for (Map.Entry<Uri, Uri> entry : pathToUrlMap.entrySet()) {
                    Uri path = entry.getKey();
                    Uri url = entry.getValue();
                    IWrfResource resource = resourceMap.get(url);
                    if (resource == null) {
                        // FIXME: notify that the resource does not exist..
                        continue;
                    }
                    IContentAdapter resourceContentAdapter = resource
                        .getAdapter(IContentAdapter.class);
                    ZipEntry zipEntry = new ZipEntry(path.toString());
                    out.putNextEntry(zipEntry);
                    InputStream input = resourceContentAdapter
                        .getContentInput();
                    try {
                        int len;
                        while ((len = input.read(buf)) > 0) {
                            out.write(buf, 0, len);
                        }
                    } finally {
                        input.close();
                    }
                    out.closeEntry();
                    System.out.println("     > " + url + "\t-\t" + path);
                }
                out.close();
            } finally {
                output.close();
            }

            IPropertyAdapter propertiesAdapter = resultResource
                .getAdapter(IPropertyAdapter.class);
            Path.Builder builder = docPath.getPath().getBuilder();
            if (builder.getFileName() == null) {
                builder.setFileName("unnamed");
            }
            builder.setFileExtension("zip");
            String fileName = builder.getFileName();
            Map<String, String> properties = new HashMap<String, String>();
            properties.put("Content-Type", "application/zip");
            properties.put("Content-disposition", "attachment; filename="
                + fileName);
            propertiesAdapter.setProperties(properties);
            response.setResultStatus(HttpStatusCode.STATUS_200);
        } catch (Throwable t) {
            handleError("Can not zip resources", t);
        } finally {
            event.setResponse(response);
        }
    }
}