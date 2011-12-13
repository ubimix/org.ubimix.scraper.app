/**
 * 
 */
package org.webreformatter.resources.adapters.zip;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.webreformatter.commons.uri.Path;
import org.webreformatter.resources.IContentAdapter;
import org.webreformatter.resources.IWrfResource;
import org.webreformatter.resources.IWrfResourceProvider;
import org.webreformatter.resources.WrfResourceAdapter;

/**
 * @author kotelnikov
 */
public class ZipAdapter extends WrfResourceAdapter {

    private IWrfResource fResource;

    public ZipAdapter(IWrfResource instance) {
        fResource = instance;
    }

    private ZipEntry writeZipEntry(
        ZipOutputStream out,
        Path path,
        InputStream input) throws IOException {
        ZipEntry zipEntry = new ZipEntry(path.toString());
        out.putNextEntry(zipEntry);
        try {
            byte[] buf = new byte[1024 * 10];
            int len;
            while ((len = input.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } finally {
            input.close();
        }
        out.closeEntry();
        return zipEntry;
    }

    /**
     * This method zip all resources referenced by the specified in this
     * resource. Keys of the given map contains logical paths of entries in the
     * resulting zip, and the corresponding values represents paths of resources
     * to compress.
     * 
     * @param resourceMapping
     * @throws IOException
     */
    public void zip(Map<Path, Path> resourceMapping) throws IOException {
        IWrfResourceProvider provider = fResource.getProvider();
        Map<Path, IWrfResource> resources = new HashMap<Path, IWrfResource>();
        for (Map.Entry<Path, Path> entry : resourceMapping.entrySet()) {
            Path key = entry.getKey();
            Path resourcePath = entry.getValue();
            IWrfResource resource = provider.getResource(resourcePath, false);
            if (resource != null) {
                resources.put(key, resource);
            }
        }
        zipResources(resources.entrySet());
    }

    /**
     * This method compress all resources given in the specified map in this
     * resource. Keys of the given map contains logical paths of entries to
     * compress, and the corresponding values represents compressed resources.
     * 
     * @param resourceMapping
     * @throws IOException
     */
    public void zipResources(Iterable<Map.Entry<Path, IWrfResource>> resources)
        throws IOException {
        IContentAdapter content = fResource.getAdapter(IContentAdapter.class);
        OutputStream output = content.getContentOutput();
        try {
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(
                output,
                1024 * 10));
            for (Map.Entry<Path, IWrfResource> entry : resources) {
                Path path = entry.getKey();
                IWrfResource resource = entry.getValue();
                if (resource != null) {
                    IContentAdapter resourceContentAdapter = resource
                        .getAdapter(IContentAdapter.class);
                    InputStream input = resourceContentAdapter.exists()
                        ? resourceContentAdapter.getContentInput()
                        : new ByteArrayInputStream(new byte[0]);
                    writeZipEntry(out, path, input);
                }
            }
            out.close();
        } finally {
            output.close();
        }
    }

}
