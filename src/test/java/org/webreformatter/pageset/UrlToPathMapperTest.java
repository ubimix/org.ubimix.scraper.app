/**
 * 
 */
package org.webreformatter.pageset;

import junit.framework.TestCase;

import org.webreformatter.commons.uri.Uri;
import org.webreformatter.pageset.UrlToPathMapper.SiteUrlMapper;

/**
 * @author cognium
 */
// Get a SiteSetConfigBuilder. It creates a SiteSetConfig (formerly
// SiteConfigManager);
// SiteSetConfigBuilder gives access to SiteConfigBuilder.
// SiteConfigBuilder is used to fill all fields of the SiteConfig.
//

public class UrlToPathMapperTest extends TestCase {

    /**
     * @param name
     */
    public UrlToPathMapperTest(String name) {
        super(name);
    }

    private void testConvert(
        UrlToPathMapper transformer,
        String path,
        String uri) {
        testUriToPath(transformer, uri, path);
        testPathToUri(transformer, path, uri);
    }

    public void testDefaultUrl() throws Exception {
        UrlToPathMapper transformer = new UrlToPathMapper();
        transformer.addBase("http://www.foo.bar", "");
        // t.addPathToUri(from, to);
        // t.addUriToPath(from, to);

        testPathToUri(
            transformer,
            "x/README.txt",
            "http://www.foo.bar/x/README.txt");
        testPathToUri(transformer, "", "http://www.foo.bar");
        testPathToUri(transformer, "/", "http://www.foo.bar");

        testConvert(
            transformer,
            "/x/README.txt",
            "http://www.foo.bar/x/README.txt");
        testConvert(transformer, "", "http://www.foo.bar");
    }

    public void testDefaultUrlToLink() throws Exception {
        testDefaultUrlToLink(
            "http://localhost:8080/",
            "",
            "http://localhost:8080/MyPage.jsp",
            "MyPage.jsp");
        testDefaultUrlToLink(
            "http://en.wikipedia.org/wiki/",
            "wikipedia",
            "http://en.wikipedia.org/wiki/France",
            "wikipedia/France");

    }

    private void testDefaultUrlToLink(
        String baseUrl,
        String basePath,
        String url,
        String path) {
        UrlToPathMapper transformer = new UrlToPathMapper();
        transformer.addBase(baseUrl, basePath);

        Uri originalUrl = new Uri(url);
        Uri controlLink = new Uri(path);

        Uri testLink = transformer.uriToPath(originalUrl);
        assertNotNull(testLink);
        assertEquals(controlLink, testLink);

        Uri testUrl = transformer.pathToUri(testLink);
        assertNotNull(testUrl);
        assertEquals(originalUrl, testUrl);

    }

    public void testJspWikiMapping() throws Exception {
        UrlToPathMapper transformer = new UrlToPathMapper();
        SiteUrlMapper t = transformer.addBase("http://localhost:8080/", "abc");
        t.addPathToUri("^(.*)$", "Wiki.jsp?page=$1");
        t.addUriToPath("^.*page=([^&]*).*$", "$1");
        testUrlToLinkWithConverter(
            transformer,
            "http://localhost:8080/MyPage.jsp?hello=world&page=FirstPage&secondParam=x",
            "abc/FirstPage",
            "http://localhost:8080/Wiki.jsp?page=FirstPage");

        transformer = new UrlToPathMapper();
        t = transformer
            .addBase("http://localhost:8080/JSPWiki/", "wiki")
            .addUriToPath("^.*page=([^&]*).*$", "$1")
            .addPathToUri("^(.*)$", "Wiki.jsp?page=$1");

        String url = "http://localhost:8080/JSPWiki/Wiki.jsp?page=Hello";
        String path = "wiki/Hello";
        testUrlToLinkWithConverter(transformer, url, path, url);
    }

    public void testMultipleSiteConfiguration1() throws Exception {
        UrlToPathMapper transformer = new UrlToPathMapper();
        transformer
            .addBase("http://en.wikipedia.org/wiki/", "/wikipedia")
            .addUriToPath("^$", "index.html")
            .addUriToPath("^Main$", "index.html")
            .addUriToPath("^(.*)$", "$1.html")
            .addPathToUri("^$", "Main")
            .addPathToUri("^index\\.html", "Main")
            .addPathToUri("^(.+)\\.html$", "$1")
            .addPathToUri("^(.+)$", "$1");
        transformer.addBase(
            "http://upload.wikimedia.org/wikipedia/commons/thumb/",
            "/wikipedia/images");

        testPathToUri(
            transformer,
            "wikipedia/",
            "http://en.wikipedia.org/wiki/Main");
        testPathToUri(
            transformer,
            "wikipedia/index.html",
            "http://en.wikipedia.org/wiki/Main");
        testUriToPath(
            transformer,
            "http://en.wikipedia.org/wiki/Main",
            "wikipedia/index.html");
        testUriToPath(
            transformer,
            "http://en.wikipedia.org/wiki/",
            "wikipedia/index.html");
        testPathToUri(
            transformer,
            "wikipedia/Main.html",
            "http://en.wikipedia.org/wiki/Main");
        testPathToUri(
            transformer,
            "wikipedia/imagestoto.jpg",
            "http://en.wikipedia.org/wiki/imagestoto.jpg");

        testPathToUri(
            transformer,
            "wikipedia/images/MyImage.jpg",
            "http://upload.wikimedia.org/wikipedia/commons/thumb/MyImage.jpg");
        testUriToPath(
            transformer,
            "http://upload.wikimedia.org/wikipedia/commons/thumb/a/b/c/MyImage.jpg",
            "wikipedia/images/a/b/c/MyImage.jpg");
    }

    public void testMultipleSiteConfiguration2() throws Exception {
        UrlToPathMapper transformer = new UrlToPathMapper();
        transformer.addBase("http://localhost:8080/site/", "");
        transformer.addBase("http://localhost:8080/docs/", "docs");
        transformer.addBase("http://code.google.com/p/webreformatter/", "wrf/");
        transformer
            .addBase("http://en.wikipedia.org/wiki/", "/wikipedia")
            .addUriToPath("^$", "index.html")
            .addUriToPath("^Main$", "index.html")
            .addUriToPath("^(.*)$", "$1.html")
            .addPathToUri("^$", "Main")
            .addPathToUri("^index\\.html", "Main")
            .addPathToUri("^(.+)\\.html$", "$1");
        testUriToPath(
            transformer,
            "http://en.wikipedia.org/wiki/France",
            "wikipedia/France.html");
        testPathToUri(
            transformer,
            "wikipedia/France",
            "http://en.wikipedia.org/wiki/France");
        testPathToUri(
            transformer,
            "wikipedia/France.html",
            "http://en.wikipedia.org/wiki/France");

        testConvert(
            transformer,
            "wrf/toto/aa",
            "http://code.google.com/p/webreformatter/toto/aa");
        testConvert(
            transformer,
            "wrf/toto/aa/",
            "http://code.google.com/p/webreformatter/toto/aa/");
        testConvert(
            transformer,
            "about.html",
            "http://localhost:8080/site/about.html");
        testConvert(
            transformer,
            "docs/gettingstarted.html",
            "http://localhost:8080/docs/gettingstarted.html");
    }

    private void testPathToUri(
        UrlToPathMapper transformer,
        String path,
        String uri) {
        Uri control = new Uri(uri);
        Uri test = transformer.pathToUri(path);
        assertEquals(control, test);
    }

    private void testUriToPath(
        UrlToPathMapper transformer,
        String uri,
        String path) {
        Uri control = new Uri(path);
        Uri test = transformer.uriToPath(uri);
        assertEquals(control, test);
    }

    private void testUrlToLinkWithConverter(
        UrlToPathMapper transformer,
        String url,
        String controlLink,
        String controlUrl) {
        Uri cUrl = new Uri(controlUrl);
        Uri cLink = new Uri(controlLink);

        Uri originalUrl = new Uri(url);
        Uri testLink = transformer.uriToPath(originalUrl);
        assertNotNull(testLink);
        assertEquals(cLink, testLink);
        assertEquals(controlLink, testLink.toString());

        Uri testUrl = transformer.pathToUri(testLink);
        assertNotNull(testUrl);
        assertEquals(cUrl, testUrl);
        assertEquals(controlUrl, testUrl.toString());
    }

    public void testUrlToLinkWithFilters() throws Exception {
        {
            UrlToPathMapper transformer = new UrlToPathMapper();
            SiteUrlMapper t = transformer
                .addBase("http://localhost:8080/", "abc")
                .addUriToPath("^.*page=([^&]*).*$", "$1")
                .addPathToUri("^(.*)$", "MyPage.jsp?page=$1&param=value");

            testUrlToLinkWithConverter(
                transformer,
                "http://localhost:8080/MyPage.jsp?hello=world&page=FirstPage&secondParam=x",
                "abc/FirstPage",
                "http://localhost:8080/MyPage.jsp?page=FirstPage&param=value");
            testUrlToLinkWithConverter(
                transformer,
                "http://localhost:8080/MyPage.jsp?x=Z&page=TOTO&secondParam=x",
                "abc/TOTO",
                "http://localhost:8080/MyPage.jsp?page=TOTO&param=value");
        }
        {
            UrlToPathMapper transformer = new UrlToPathMapper();
            transformer
                .addBase("http://localhost:8080/test", "profile")
                .addUriToPath(
                    "^.*user=([^&]*)&group=([^&]*)&type=([^&]*).*$",
                    "$2/$1/$3")
                .addPathToUri(
                    "^(.*)/(.*)/(.*)$",
                    "?group=$1&user=$2&type=$3&x=Y");
            testUrlToLinkWithConverter(
                transformer,
                "http://localhost:8080/test?user=123&group=admin&type=contacts&x=Y",
                "profile/admin/123/contacts",
                "http://localhost:8080/test?group=admin&user=123&type=contacts&x=Y");
        }
    }

}
