package org.webreformatter.scrapper.context;

import org.webreformatter.commons.uri.Uri;

/**
 * @author kotelnikov
 */
public interface IUrlTransformer {

    IUrlTransformer EMPTY = new IUrlTransformer() {
        public Uri transform(Uri uri) {
            return uri;
        }
    };

    Uri transform(Uri uri);
}