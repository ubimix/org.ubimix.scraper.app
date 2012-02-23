/**
 * 
 */
package org.webreformatter.scrapper.core;

import junit.framework.TestCase;

/**
 * @author kotelnikov
 */
public class AppContextTest extends TestCase {

    /**
     * @param name
     */
    public AppContextTest(String name) {
        super(name);
    }

    public void test() throws Exception {
        AppContext appContext = new AppContext();
        DownloadAdapter downloadAdapter = appContext
            .getAdapter(DownloadAdapter.class);
        assertNotNull(downloadAdapter);
    }

}
