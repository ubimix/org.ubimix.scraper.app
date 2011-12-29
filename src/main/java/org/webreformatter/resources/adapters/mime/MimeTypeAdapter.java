/**
 * 
 */
package org.webreformatter.resources.adapters.mime;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.webreformatter.commons.adapters.IAdapterFactory;
import org.webreformatter.commons.uri.Path;
import org.webreformatter.resources.IContentAdapter;
import org.webreformatter.resources.IContentAdapter.ContentChangeEvent;
import org.webreformatter.resources.IPropertyAdapter;
import org.webreformatter.resources.IPropertyAdapter.PropertiesChangeEvent;
import org.webreformatter.resources.IWrfResource;
import org.webreformatter.resources.WrfResourceAdapter;
import org.webreformatter.server.mime.ExtendedMimeTypeDetector;
import org.webreformatter.server.mime.IMimeTypeDetector;

/**
 * @author kotelnikov
 */
public class MimeTypeAdapter extends WrfResourceAdapter {

    public static IAdapterFactory getAdapterFactory() {
        final IMimeTypeDetector detector = new ExtendedMimeTypeDetector();
        return new IAdapterFactory() {
            @SuppressWarnings("unchecked")
            public <T> T getAdapter(Object instance, Class<T> type) {
                if (!(instance instanceof IWrfResource)
                    || type != MimeTypeAdapter.class) {
                    return null;
                }
                return (T) new MimeTypeAdapter(
                    detector,
                    (IWrfResource) instance);
            }
        };
    }

    private IMimeTypeDetector fDetector;

    private String fMimeType;

    protected MimeTypeAdapter(IMimeTypeDetector detector, IWrfResource instance) {
        super(instance);
        fDetector = detector;
    }

    public synchronized String getMimeType() throws IOException {
        if (fMimeType == null) {
            IContentAdapter content = fResource
                .getAdapter(IContentAdapter.class);
            if (content.exists()) {
                fMimeType = getMimeTypeFromProperties();
                if (fMimeType == null) {
                    fMimeType = getMimeTypeByFileName();
                    if (fMimeType == null) {
                        fMimeType = getMimeTypeByContent();
                    }
                }
            }
        }
        return fMimeType;
    }

    protected String getMimeTypeByContent() throws IOException {
        IContentAdapter content = fResource.getAdapter(IContentAdapter.class);
        InputStream input = content.getContentInput();
        try {
            if (!input.markSupported()) {
                input = new BufferedInputStream(input);
            }
            String mimeType = fDetector.getMimeType(input);
            if (mimeType != null) {
                int idx = mimeType.lastIndexOf(';');
                if (idx > 0) {
                    mimeType = mimeType.substring(0, idx);
                }
            }
            return mimeType;
        } finally {
            input.close();
        }
    }

    protected String getMimeTypeByFileName() {
        String mimeType = null;
        Path path = fResource.getPath();
        String extension = path.getFileExtension();
        if (extension != null) {
            String fileName = path.getFileName();
            mimeType = fDetector.getMimeTypeByExtension(fileName);
        }
        return mimeType;
    }

    protected String getMimeTypeFromProperties() throws IOException {
        IPropertyAdapter propertyAdapter = fResource
            .getAdapter(IPropertyAdapter.class);
        String mime = null;
        if (propertyAdapter != null) {
            mime = propertyAdapter.getProperty("Content-Type");
            if (mime != null) {
                int idx = mime.indexOf(';');
                if (idx > 0) {
                    mime = mime.substring(0, idx);
                    mime = mime.trim();
                }
            }
        }
        return mime;
    }

    @Override
    public void handleEvent(Object event) {
        if (event instanceof PropertiesChangeEvent
            || event instanceof ContentChangeEvent) {
            fMimeType = null;
        }
    }
}
