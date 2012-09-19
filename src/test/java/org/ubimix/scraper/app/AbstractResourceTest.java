/**
 * 
 */
package org.ubimix.scraper.app;

import java.io.File;

import junit.framework.TestCase;

import org.ubimix.commons.adapters.CompositeAdapterFactory;
import org.ubimix.commons.io.IOUtil;
import org.ubimix.resources.IWrfResourceProvider;
import org.ubimix.resources.impl.WrfRepositoryUtils;
import org.ubimix.resources.impl.WrfResourceRepository;

/**
 * @author kotelnikov
 */
public abstract class AbstractResourceTest extends TestCase {

    protected CompositeAdapterFactory fAdapters = new CompositeAdapterFactory();

    protected IWrfResourceProvider fResourceProvider;

    protected WrfResourceRepository fResourceRepository;

    public AbstractResourceTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        File root = new File("./tmp");
        IOUtil.delete(root);
        fResourceRepository = new WrfResourceRepository(fAdapters, root);
        WrfRepositoryUtils.registerDefaultResourceAdapters(fAdapters);
        fResourceProvider = fResourceRepository.getResourceProvider(
            "test",
            true);
    }
}
