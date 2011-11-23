/**
 * 
 */
package org.webreformatter.pageset;

/**
 * This configuration class contains the following information:
 * <ul>
 * <li> {@link AccessManager} - defines credentials used to access site resources
 * </li>
 * <li> {@link IUrlTransformer} - there are two instances of this type used to
 * translate URIs mentioned in documents into absolute resource URLs (used to
 * download resources) and to localized paths (used to export these resources):
 * <ul>
 * <li>DownloadUrlTransformer - defines how URIs mentioned in documents should
 * be resolved to full URLs used to retrieve resources</li>
 * <li>LocalizeUrlTransformer - defines how document URIs in documents should be
 * translated to local paths used to export resources</li>
 * </ul>
 * </li>
 * <li> {@link IUrlMapper} - defines additional URLs associated with external
 * sites; for example it could be an URL of an XSL transformation used to
 * cleanup site pages or an URL of templates applied for pages.</li>
 * </ul>
 * 
 * @author kotelnikov
 */
public class PageSetConfig {

    public static class Builder extends PageSetConfig {

        public Builder() {
            super(null);
        }

        public PageSetConfig build() {
            return new PageSetConfig(this);
        }

        @Override
        protected void checkFields() {
        }

        public PageSetConfig.Builder setAccessManager(
            AccessManager accessManager) {
            fAccessManager = accessManager;
            return this;
        }

        public PageSetConfig.Builder setDownloadUrlTransformer(
            IUrlTransformer downloadUrlTransformer) {
            fDownloadUrlTransformer = downloadUrlTransformer;
            return this;
        }

        public PageSetConfig.Builder setLocalizeUrlTransformer(
            IUrlTransformer localizeUrlTransformer) {
            fLocalizeUrlTransformer = localizeUrlTransformer;
            return this;
        }

        public PageSetConfig.Builder setUriMapper(IUrlMapper uriMapper) {
            fUriMapper = uriMapper;
            return this;
        }

        public PageSetConfig.Builder setUrlToPathMapper(
            UrlToPathMapper urlToPathMapper) {
            fUrlToPathMapper = urlToPathMapper;
            return this;
        }

    }

    public static PageSetConfig.Builder builder() {
        return new PageSetConfig.Builder();
    }

    protected AccessManager fAccessManager;

    protected IUrlTransformer fDownloadUrlTransformer;

    protected IUrlTransformer fLocalizeUrlTransformer;

    protected IUrlMapper fUriMapper;

    protected UrlToPathMapper fUrlToPathMapper;

    /**
     * 
     */
    public PageSetConfig(PageSetConfig config) {
        if (config != null) {
            fAccessManager = config.fAccessManager;
            fDownloadUrlTransformer = config.fDownloadUrlTransformer;
            fLocalizeUrlTransformer = config.fLocalizeUrlTransformer;
            fUriMapper = config.fUriMapper;
            fUrlToPathMapper = config.fUrlToPathMapper;
        }
        checkFields();
    }

    protected void assertTrue(String msg, boolean condition) {
        if (!condition) {
            throw new IllegalArgumentException();
        }
    }

    protected void checkFields() {
        assertTrue("Access manager can not be null", fAccessManager != null);
        assertTrue(
            "Download URL transformer can not be null",
            fDownloadUrlTransformer != null);
        assertTrue(
            "Localize URL transformer can not be null",
            fLocalizeUrlTransformer != null);
        // assertTrue("URI mapper can not be null", fUriMapper != null);
        // assertTrue("UrlToPath mapper can not be null", fUrlToPathMapper !=
        // null);
    }

    public AccessManager getAccessManager() {
        return fAccessManager;
    }

    public IUrlTransformer getDownloadUrlTransformer() {
        return fDownloadUrlTransformer;
    }

    public IUrlTransformer getLocalizeUrlTransformer() {
        return fLocalizeUrlTransformer;
    }

    public IUrlMapper getUriMapper() {
        return fUriMapper;
    }

    public UrlToPathMapper getUrlToPathMapper() {
        return fUrlToPathMapper;
    }

}
