/**
 * 
 */
package org.webreformatter.resources.impl;

import org.webreformatter.resources.adapters.cache.CachedResourceAdapter;
import org.webreformatter.resources.adapters.encoding.EncodingAdapter;
import org.webreformatter.resources.adapters.html.HTMLAdapter;
import org.webreformatter.resources.adapters.images.ImageAdapter;
import org.webreformatter.resources.adapters.mime.MimeTypeAdapter;
import org.webreformatter.resources.adapters.string.StringAdapter;
import org.webreformatter.resources.adapters.xml.XmlAdapter;

/**
 * @author kotelnikov
 */
public class WrfRepositoryUtils {

    public static void registerAdapters(WrfResourceRepository repository) {
        repository.registerResourceAdapter(CachedResourceAdapter.class);
        repository.registerResourceAdapter(XmlAdapter.class);
        repository.registerResourceAdapter(EncodingAdapter.class);
        repository.registerResourceAdapter(HTMLAdapter.class);
        repository.registerResourceAdapter(MimeTypeAdapter.class);
        repository.registerResourceAdapter(StringAdapter.class);
        repository.registerResourceAdapter(ImageAdapter.class);
    }

    /**
     * 
     */
    public WrfRepositoryUtils() {
    }

}
