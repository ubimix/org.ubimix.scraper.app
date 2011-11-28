/**
 * 
 */
package org.webreformatter.pageset;

import java.util.HashMap;
import java.util.Map;

import org.webreformatter.commons.uri.Path;
import org.webreformatter.commons.uri.Uri;
import org.webreformatter.commons.uri.UriToPath;
import org.webreformatter.commons.uri.path.PathManager;

/**
 * @author kotelnikov
 */
public class UrlMapper implements IUrlMapper {

    private Map<String, PathManager<Uri>> fPathManagers = new HashMap<String, PathManager<Uri>>();

    public UrlMapper() {
    }

    protected String getKey(Uri baseUrl) {
        Path path = UriToPath.getPath(baseUrl);
        String key = path.toString();
        return key;
    }

    /**
     * @see org.webreformatter.pageset.IUrlMapper#getUrl(java.lang.String,
     *      org.webreformatter.commons.uri.Uri)
     */
    public Uri getUrl(String type, Uri baseUrl) {
        PathManager<Uri> pathManager = fPathManagers.get(type);
        Uri result = null;
        if (pathManager != null) {
            String key = getKey(baseUrl);
            result = pathManager.getNearestValue(key);
        }
        return result;
    }

    /**
     * @param type
     * @param baseUrl
     * @param uri
     */
    public UrlMapper setUrl(String type, Uri baseUrl, Uri uri) {
        PathManager<Uri> pathManager = fPathManagers.get(type);
        if (pathManager == null) {
            pathManager = new PathManager<Uri>();
            fPathManagers.put(type, pathManager);
        }
        String key = getKey(baseUrl);
        pathManager.add(key, uri);
        return this;
    }

}