/**
 * 
 */
package org.webreformatter.resources.adapters.encoding;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.webreformatter.commons.adapters.IAdapterFactory;
import org.webreformatter.resources.IContentAdapter;
import org.webreformatter.resources.IContentAdapter.ContentChangeEvent;
import org.webreformatter.resources.IPropertyAdapter;
import org.webreformatter.resources.IPropertyAdapter.PropertiesChangeEvent;
import org.webreformatter.resources.IWrfResource;
import org.webreformatter.resources.WrfResourceAdapter;
import org.webreformatter.server.encoding.EncodingDetector;
import org.webreformatter.server.encoding.IEncodingDetector;

/**
 * @author kotelnikov
 */
public class EncodingAdapter extends WrfResourceAdapter {

    private static Pattern CHARSET_REGEX = Pattern.compile(
        "^.*charset\\s*=\\s*([\\w\\d-]+).*$",
        Pattern.CASE_INSENSITIVE);

    public static IAdapterFactory getAdapterFactory() {
        final IEncodingDetector detector = new EncodingDetector();
        return new IAdapterFactory() {
            @SuppressWarnings("unchecked")
            public <T> T getAdapter(Object instance, Class<T> type) {
                if (!(instance instanceof IWrfResource)
                    || type != EncodingAdapter.class) {
                    return null;
                }
                return (T) new EncodingAdapter(
                    detector,
                    (IWrfResource) instance);
            }
        };
    }

    private IEncodingDetector fDetector;

    private String fEncoding;

    private IWrfResource fResource;

    protected EncodingAdapter(IEncodingDetector detector, IWrfResource instance) {
        fDetector = detector;
        fResource = instance;
    }

    public synchronized String getEncoding() throws IOException {
        if (fEncoding == null) {
            IPropertyAdapter propertiesAdapter = fResource
                .getAdapter(IPropertyAdapter.class);
            String contentType = propertiesAdapter.getProperty("Content-Type");
            if (contentType != null) {
                Matcher matcher = CHARSET_REGEX.matcher(contentType);
                if (matcher.matches()) {
                    fEncoding = matcher.group(1);
                }
            }
            if (fEncoding == null) {
                IContentAdapter content = fResource
                    .getAdapter(IContentAdapter.class);
                InputStream input = content.getContentInput();
                try {
                    if (!input.markSupported()) {
                        input = new BufferedInputStream(input);
                    }
                    fEncoding = fDetector.getEncoding(input);
                } finally {
                    input.close();
                }
            }
        }
        return fEncoding;
    }

    @Override
    public void handleEvent(Object event) {
        if (event instanceof PropertiesChangeEvent
            || event instanceof ContentChangeEvent) {
            fEncoding = null;
        }
    }

}
