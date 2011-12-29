/**
 * 
 */
package org.webreformatter.resources.adapters;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.webreformatter.commons.adapters.IAdapterFactory;
import org.webreformatter.commons.uri.Path;
import org.webreformatter.resources.IContentAdapter;
import org.webreformatter.resources.IPropertyAdapter;
import org.webreformatter.resources.IWrfResource;
import org.webreformatter.resources.IWrfResourceProvider;
import org.webreformatter.resources.WrfResourceAdapter;
import org.webreformatter.resources.adapters.cache.DateUtil;

/**
 * This adapter is used to copy the content of a local file in the adapted
 * resource and vice versa - from the current resource to a file.
 * 
 * @author kotelnikov
 */
public class FileAdapter extends WrfResourceAdapter {

    /**
     * Returns a new adapter factory.
     * 
     * @return a new adapter factory
     */
    public static IAdapterFactory getAdapterFactory() {
        return new IAdapterFactory() {
            @SuppressWarnings("unchecked")
            public <T> T getAdapter(Object instance, Class<T> type) {
                if (type != FileAdapter.class) {
                    return null;
                }
                return (T) new FileAdapter((IWrfResource) instance);
            }
        };
    }

    /**
     * The default constructor initializing the internal fields.
     * 
     * @param instance the resource instance to set
     */
    public FileAdapter(IWrfResource instance) {
        super(instance);
    }

    public void copy(InputStream input, OutputStream out) throws IOException {
        byte[] buf = new byte[1024 * 10];
        int len;
        while ((len = input.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
    }

    /**
     * Recursively copies all children of the specified directory in
     * sub-resources of the underlying resource.
     * 
     * @param dir the directory to copy.
     * @throws IOException
     */
    protected void copyChildren(File dir) throws IOException {
        File[] array = dir.listFiles();
        if (array != null) {
            IWrfResourceProvider provider = fResource.getProvider();
            Path path = fResource.getPath();
            for (File child : array) {
                Path childPath = path
                    .getBuilder()
                    .appendPath(child.getName())
                    .build();
                IWrfResource childResource = provider.getResource(
                    childPath,
                    true);
                FileAdapter childAdapter = childResource
                    .getAdapter(FileAdapter.class);
                childAdapter.copyFrom(child, true);
            }
        }
    }

    /**
     * This method copies from the specified file in this resource. This method
     * can copy recursively the content of directories if the specified
     * <code>recursive</code> flag is <code>true</code>.
     * 
     * @param file the file to copy
     * @param recursive if this flag is true
     * @return <code>true</code> if the content of the given file was
     *         successfully copied in the underlying resource
     * @throws IOException
     */
    public synchronized boolean copyFrom(File file, boolean recursive)
        throws IOException {
        if (!file.exists()) {
            return false;
        }
        if (file.isDirectory()) {
            if (recursive) {
                copyChildren(file);
            }
        } else {
            FileInputStream input = new FileInputStream(file);
            copyFrom(input);
        }
        setMetadata(file);
        return true;
    }

    /**
     * This method copies from the specified InputStream in this resource.
     * 
     * @param file the file to copy
     * @param recursive if this flag is true
     * @return <code>true</code> if the content of the given file was
     *         successfully copied in the underlying resource
     * @throws IOException
     */
    public synchronized boolean copyFrom(InputStream input) throws IOException {
        IContentAdapter contentAdapter = fResource
            .getAdapter(IContentAdapter.class);
        try {
            OutputStream out = contentAdapter.getContentOutput();
            try {
                copy(input, out);
            } finally {
                out.close();
            }
        } finally {
            input.close();
        }
        return true;
    }

    /**
     * Copies the content of the underlying resource (see
     * {@link IContentAdapter}) to the specified file.
     * 
     * @param file the destination file for the content of this resource
     * @return <code>true</code> if the content was successfully copied
     * @throws IOException
     */
    public synchronized boolean copyTo(File file) throws IOException {
        IContentAdapter contentAdapter = fResource
            .getAdapter(IContentAdapter.class);
        if (!contentAdapter.exists()) {
            return false;
        }
        file.getParentFile().mkdirs();
        FileOutputStream out = new FileOutputStream(file);
        try {
            InputStream input = contentAdapter.getContentInput();
            try {
                copy(input, out);
            } finally {
                input.close();
            }
        } finally {
            out.close();
        }
        return true;
    }

    /**
     * Copies the metadata about the specified file in the properties of this
     * resource.
     * 
     * @param file the file used as a source of information.
     * @throws IOException
     */
    private void setMetadata(File file) throws IOException {
        IPropertyAdapter properties = fResource
            .getAdapter(IPropertyAdapter.class);
        long date = file.lastModified();
        String formattedDate = DateUtil.formatDate(date);
        properties.setProperty("Last-Modified", formattedDate);
    }

}
