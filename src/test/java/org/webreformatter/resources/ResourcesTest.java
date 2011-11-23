package org.webreformatter.resources;

import java.io.OutputStream;

import org.webreformatter.commons.io.IOUtil;
import org.webreformatter.commons.uri.Path;

public class ResourcesTest extends AbstractResourceTest {

    public ResourcesTest(String name) {
        super(name);
    }

    public void test() throws Exception {
        Path path = new Path("/abc.txt");
        IWrfResource resource = fResourceProvider.getResource(path, false);
        assertNull(resource);
        resource = fResourceProvider.getResource(path, true);
        assertNotNull(resource);
        IContentAdapter content = resource.getAdapter(IContentAdapter.class);
        assertNotNull(content);
        assertFalse(content.exists());
        OutputStream out = content.getContentOutput();
        try {
            IOUtil.writeString(out, "Hello, there");
        } finally {
            out.close();
        }
        assertTrue(content.exists());
    }
}
