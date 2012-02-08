/**
 * 
 */
package org.webreformatter.scrapper.app;

import java.io.File;

import junit.framework.TestCase;

import org.webreformatter.commons.adapters.CompositeAdapterFactory;
import org.webreformatter.commons.io.IOUtil;
import org.webreformatter.resources.IWrfResourceProvider;
import org.webreformatter.resources.impl.WrfRepositoryUtils;
import org.webreformatter.resources.impl.WrfResourceRepository;

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
