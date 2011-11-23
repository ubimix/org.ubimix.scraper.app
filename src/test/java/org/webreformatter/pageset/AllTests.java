package org.webreformatter.pageset;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

    public static Test suite() {
        TestSuite suite = new TestSuite("Test for org.webreformatter.pageset");
        // $JUnit-BEGIN$
        suite.addTestSuite(UrlToPathMapperTest.class);
        suite.addTestSuite(UrlMapperTest.class);
        suite.addTestSuite(XmlUrlToPathMapperLoaderTest.class);
        // $JUnit-END$
        return suite;
    }

}
