package org.webreformatter.resources.adapters.cache;

import java.io.IOException;
import java.util.Map;

import org.webreformatter.resources.IContentAdapter;
import org.webreformatter.resources.IPropertyAdapter;
import org.webreformatter.resources.IWrfResource;
import org.webreformatter.resources.WrfResourceAdapter;
import org.webreformatter.scrapper.context.HttpStatusCode;

/**
 * @author kotelnikov
 */
public class CachedResourceAdapter extends WrfResourceAdapter {

    private static final String PROPERTY_LAST_MODIFIED = "Last-Modified";

    private static final String PROPERTY_STATUS_CODE = "StatusCode";

    private IWrfResource fResource;

    public CachedResourceAdapter(IWrfResource resource) {
        super();
        fResource = resource;
    }

    public void copyPropertiesFrom(IPropertyAdapter from) throws IOException {
        IPropertyAdapter to = fResource.getAdapter(IPropertyAdapter.class);
        Map<String, String> properties = from.getProperties();
        to.setProperties(properties);
    }

    public void copyPropertiesFrom(IWrfResource resource) throws IOException {
        IPropertyAdapter from = resource.getAdapter(IPropertyAdapter.class);
        copyPropertiesFrom(from);
    }

    protected long getDownloadDelta() {
        // Don't download if the resource was already downloaded
        // less than one day ago.
        return DateUtil.DAY * 1;
    }

    public long getLastModified() throws IOException {
        IPropertyAdapter properties = fResource
            .getAdapter(IPropertyAdapter.class);
        long lastModified = getTime(properties, PROPERTY_LAST_MODIFIED);
        return lastModified;
    }

    protected long getLastModifiedDelta() {
        return (long) DateUtil.DAY * 30;
        // return DateUtil.HOUR; // DateUtil.MIN; // DateUtil.DAY * 30;
    }

    public HttpStatusCode getStatus() throws IOException {
        IPropertyAdapter properties = fResource
            .getAdapter(IPropertyAdapter.class);
        String statusCode = properties.getProperty(PROPERTY_STATUS_CODE);
        int code = 500;
        try {
            code = Integer.parseInt(statusCode.trim());
        } catch (Exception e) {
        }
        HttpStatusCode result = HttpStatusCode.getStatusCode(code);
        return result;
    }

    private long getTime(IPropertyAdapter resource, String string)
        throws IOException {
        String str = resource.getProperty(string);
        return str != null ? DateUtil.parseDate(str) : -1;
    }

    public synchronized boolean isExpired() throws IOException {
        IContentAdapter content = fResource.getAdapter(IContentAdapter.class);
        boolean expired = !content.exists();
        if (expired) {
            return true;
        }
        expired = true;
        long lastModified = getLastModified();
        if (lastModified > 0) {
            long delta = System.currentTimeMillis() - lastModified;
            long maxDelta = getLastModifiedDelta();
            expired = (delta >= maxDelta);
        }
        return expired;
    }

    public void setLastModified(long now) throws IOException {
        IPropertyAdapter propertyAdapter = fResource
            .getAdapter(IPropertyAdapter.class);
        propertyAdapter.setProperty(
            PROPERTY_LAST_MODIFIED,
            DateUtil.formatDate(now));
    }

    public void setStatus(HttpStatusCode status) throws IOException {
        IPropertyAdapter properties = fResource
            .getAdapter(IPropertyAdapter.class);
        properties.setProperty(PROPERTY_STATUS_CODE, status.getStatusCode()
            + "");
    }

    public void touch() throws IOException {
        long now = System.currentTimeMillis();
        setLastModified(now);
    }

    public void updateMetadataFrom(CachedResourceAdapter adapter)
        throws IOException {
        long lastModified = adapter.getLastModified();
        setLastModified(lastModified);
    }

    public void updateMetadataFrom(IWrfResource resource) throws IOException {
        updateMetadataFrom(resource.getAdapter(CachedResourceAdapter.class));
    }
}