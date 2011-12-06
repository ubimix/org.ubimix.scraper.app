package org.webreformatter.pageset;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.webreformatter.commons.uri.Path;
import org.webreformatter.commons.uri.Uri;
import org.webreformatter.commons.uri.path.PathManager;

/**
 * This class is used to translate absolute URIs to local paths vice versa -
 * local paths to absolute URIs.
 * 
 * @author kotelnikov
 */
public class UrlToPathMapper {

    /**
     * This object defines mapping of URLs to paths and vice versa for one site.
     * It keeps all rules to transform all URLs starting with the same URL
     * prefix to a corresponding set of local paths and vice versa - paths to
     * the corresponding absolute URLs.
     * 
     * @author kotelnikov
     */
    public static class SiteUrlMapper {

        /**
         * This class is used to transform a set of URLs from one site (all URLs
         * starting with the same prefix) to the corresponding set of local
         * paths and vice versa. The suffix part of the URL could be changed as
         * well using regular expressions registered in this class. The main
         * method of this class ( {@link RegexpUrlTransformer#transform(String)}
         * ) removes the common prefix of the given URI, searches and applies an
         * regexp transformation to the URI suffix, after that it prepends this
         * suffix with a new prefix and returns the result.
         */
        public static class RegexpUrlTransformer {

            /**
             * This class is used to keep one regexp transformation rule.
             */
            public static class TransformationRule {

                /**
                 * The regular expression defining transformation applied to an
                 * URL or to a path.
                 */
                private Pattern fRegexp;

                /**
                 * The replacement rules.
                 */
                private String fTo;

                /**
                 * This constructor initializes the internal fields - the
                 * original regular expression and the corresponding replacement
                 * rules.
                 * 
                 * @param from the regular expression to apply to the string
                 * @param to the replacement rules
                 */
                public TransformationRule(String from, String to) {
                    fRegexp = Pattern.compile(from);
                    fTo = to;
                }

                /**
                 * Replaces the internal regular expression to the given string,
                 * performs the corresponding replacements and returns the
                 * result of this operation.
                 * 
                 * @param str the string on which the transformation should be
                 *        applied
                 * @return the result of the transformation
                 */
                public String process(String str) {
                    Matcher matcher = fRegexp.matcher(str);
                    if (matcher.matches()) {
                        str = matcher.replaceAll(fTo);
                    } else {
                        str = null;
                    }
                    return str;
                }

                @Override
                public String toString() {
                    return "{" + fRegexp + " => " + fTo + "}";
                }
            }

            /**
             * A common prefix of site URLs or local paths to which replacement
             * rules should be applied.
             */
            private String fFromPrefix;

            /**
             * The resulting prefix prepended to all transformation results.
             */
            private String fToPrefix;

            /**
             * A list of transformation rules to apply for URL or path suffixes.
             */
            private List<TransformationRule> fTransformationRules = new ArrayList<TransformationRule>();

            /**
             * Default constructor defining the original and the resulting URL
             * or path prefixes.
             * 
             * @param fromPrefix the original prefix of URLs or paths
             * @param toPrefix the resulting prefix of URLs or paths
             */
            public RegexpUrlTransformer(String fromPrefix, String toPrefix) {
                fFromPrefix = fromPrefix;
                fToPrefix = toPrefix;
            }

            /**
             * Adds a new transformation rule to the internal list
             * 
             * @param from the original regular expression to apply for the URL
             *        or path suffix.
             * @param to the resulting replacement rules
             */
            public void addTranformation(String from, String to) {
                if (from != null) {
                    TransformationRule slot = new TransformationRule(from, to);
                    fTransformationRules.add(slot);
                }
            }

            /**
             * Returns the prefix of URLs or paths to which this transformation
             * set could be applied.
             * 
             * @return the prefix of URLs or paths to which this transformation
             *         set could be applied.
             */
            public String getFromPrefix() {
                return fFromPrefix;
            }

            /**
             * Returns the resulting prefix of URLs or paths to which this
             * transformation set could be applied.
             * 
             * @return the prefix of URLs or paths to which this transformation
             *         set could be applied.
             */
            public String getToPrefix() {
                return fToPrefix;
            }

            @Override
            public String toString() {
                return "{"
                    + fFromPrefix
                    + "=>"
                    + fToPrefix
                    + ":"
                    + fTransformationRules
                    + "}";
            }

            /**
             * Performs the full transformation to the given URL or path. This
             * transformation is applied only for URLs or paths starting with
             * the internal prefix (see the {@link #getFromPrefix()} method).
             * First of all this method removes this common prefix, after that
             * it searches and applies a transformation rule to the rest of the
             * URL (to the suffix), and finally it prepends a new prefix to the
             * resulting string (see the {@#getToPrefix()})
             * method). The result of these operations is returned to the
             * caller.
             * 
             * @param str the original URL or path
             * @return the result of all transformations
             */
            public String transform(String str) {
                String result = null;
                if (str.startsWith(fFromPrefix)) {
                    str = str.substring(fFromPrefix.length());
                    String resultStr = null;
                    for (TransformationRule slot : fTransformationRules) {
                        resultStr = slot.process(str);
                        if (resultStr != null) {
                            break;
                        }
                    }
                    if (resultStr == null) {
                        resultStr = str;
                    }
                    String toPrefix = fToPrefix.toString();
                    if (toPrefix.length() > 0
                        && resultStr.length() > 0
                        && !resultStr.startsWith("/")
                        && !resultStr.startsWith("?")) {
                        resultStr = "/" + resultStr;
                    }
                    result = toPrefix + resultStr;
                    // Uri.Builder builder = new Uri.Builder(resultStr);
                    // builder
                    // .setScheme(fToPrefix.getScheme())
                    // .setUserInfo(fToPrefix.getUserInfo())
                    // .setHost(fToPrefix.getHost())
                    // .setPort(fToPrefix.getPort());
                    // Path.Builder pathBuilder = builder.getPathBuilder();
                    // pathBuilder.appendPath(fToPrefix.getPath(), true);
                    // result = builder.toString();
                }
                return result;
            }

        }

        /**
         * Transforms the given path to a string and returns the result
         * 
         * @param path the path to transform to a string
         * @param appendTrailingSeparator
         * @return a string corresponding to the original path
         */
        protected static String getPathKey(
            Uri path,
            boolean appendTrailingSeparator) {
            Uri.Builder builder = path.getBuilder();
            builder.getPathBuilder().makeRelativePath();
            Path.Builder pathBuilder = builder.getPathBuilder();
            if (pathBuilder.getPathSegmentCount() == 0) {
                pathBuilder.removeTrailingSeparator();
            } else if (appendTrailingSeparator) {
                pathBuilder.appendTrailingSeparator();
            }
            return builder.toString();
        }

        /**
         * Transforms the given URI to a string and returns its representation
         * 
         * @param baseUri the URI to transform to a string
         * @return a string representation of the given URI
         */
        protected static String getUriKey(Uri baseUri) {
            Uri.Builder builder = baseUri
                .getBuilder()
                .setFragment(null)
                .setQuery("");
            return builder.toString();
        }

        /**
         * This rule keeps transformations of paths to URLs.
         * RegexpUrlTransformer
         * 
         * @see #fUriToPath
         */
        private RegexpUrlTransformer fPathToUri;

        /**
         * This rule contains transformations of paths to absolute URLs
         * 
         * @see #fPathToUri
         */
        private RegexpUrlTransformer fUriToPath;

        /**
         * Initializes the internal fields {@link #fPathToUri} and
         * {@link #fUriToPath}.
         * 
         * @param uriStr the common URL prefix defining one site
         * @param pathStr the corresponding path prefix used to export site
         *        resources
         */
        public SiteUrlMapper(String uriStr, String pathStr) {
            fPathToUri = newRegexpUrlTransformer(pathStr, uriStr);
            fUriToPath = newRegexpUrlTransformer(uriStr, pathStr);
        }

        /**
         * Adds a new regexp transformation rule defining how to transform path
         * suffixes to resulting URL suffixes.
         * 
         * @param from the regexp applied to path suffixes
         * @param to the resulting rules defining how paths suffix should be
         *        transformed to the resulting URL
         * @return reference to this object
         */
        public SiteUrlMapper addPathToUri(String from, String to) {
            fPathToUri.addTranformation(from, to);
            return this;
        }

        /**
         * Adds a new regexp transformation rule defining how to transform URL
         * suffixes to resulting path suffixes.
         * 
         * @param from the regexp applied to URL suffixes
         * @param to the resulting rules defining how URL suffix should be
         *        transformed to the resulting path
         * @return reference to this object
         */
        public SiteUrlMapper addUriToPath(String from, String to) {
            fUriToPath.addTranformation(from, to);
            return this;
        }

        /**
         * Returns the common path prefix.
         * 
         * @return the common path prefix.
         */
        public String getPathPrefix() {
            return fPathToUri.getFromPrefix();
        }

        /**
         * Returns a path to URL transformer.
         * 
         * @return a path to URL transformer
         */
        public RegexpUrlTransformer getPathToUri() {
            return fPathToUri;
        }

        /**
         * Returns the common URL prefix.
         * 
         * @return the common URL prefix
         */
        public String getUriPrefix() {
            return fUriToPath.getFromPrefix();
        }

        /**
         * Returns an URL to path transformer.
         * 
         * @return an URL to path transformer
         */
        public RegexpUrlTransformer getUriToPath() {
            return fUriToPath;
        }

        /**
         * Creates and returns a newly created {@link RegexpUrlTransformer}
         * instance.
         * 
         * @param uriPrefix the original URL prefix
         * @param pathPrefix the resulting path prefix
         * @return a newly created {@link RegexpUrlTransformer} instance
         */
        protected RegexpUrlTransformer newRegexpUrlTransformer(
            String uriPrefix,
            String pathPrefix) {
            return new RegexpUrlTransformer(uriPrefix, pathPrefix);
        }

        /**
         * Transforms the given URL to the resulting path and returns the result
         * of this operation.
         * 
         * @param path the path to transform
         * @return the result of transformation of the original path to an
         *         absolute URL
         */
        public String pathToUri(String path) {
            String str = fPathToUri.transform(path);
            return str;
        }

        @Override
        public String toString() {
            return "PathToUri:"
                + fPathToUri
                + "\n"
                + "UriToPath: "
                + fUriToPath;
        }

        /**
         * Transforms the given URL to a local path.
         * 
         * @param uri the URL to transform to a local path
         * @return the result of transformation of URLs to the corresponding
         *         paths
         */
        public String uriToPath(String uri) {
            return fUriToPath.transform(uri);
        }

    }

    /**
     * Defines mapping of URL prefixes to the corresponding
     * {@link SiteUrlMapper} instances.
     */
    private PathManager<SiteUrlMapper> fPathToUrlTranslators = new PathManager<SiteUrlMapper>();

    /**
     * Defines mapping of URL prefixes to the corresponding
     * {@link SiteUrlMapper} instances.
     */
    private PathManager<SiteUrlMapper> fUrlToPathTranslators = new PathManager<SiteUrlMapper>();

    /**
     * Defines a new URL-to-path mapping for one site.
     * 
     * @param baseUri the base URL
     * @param basePath the corresponding path prefix
     * @return a newly created {@link SiteUrlMapper} instance
     */
    public SiteUrlMapper addBase(String baseUri, String basePath) {
        Uri uri = new Uri(baseUri);
        Uri path = new Uri(basePath);
        return addBase(uri, path);
    }

    /**
     * Defines a new URL-to-path mapping for one site.
     * 
     * @param baseUri the base URL
     * @param basePath the corresponding path prefix
     * @return a newly created {@link SiteUrlMapper} instance
     */
    public SiteUrlMapper addBase(Uri baseUri, Uri basePath) {
        String uriStr = SiteUrlMapper.getUriKey(baseUri);
        String pathStr = SiteUrlMapper.getPathKey(basePath, true);
        SiteUrlMapper translator = new SiteUrlMapper(uriStr, pathStr);
        fPathToUrlTranslators.add(pathStr, translator);
        fUrlToPathTranslators.add(uriStr, translator);
        return translator;
    }

    /**
     * Transforms the given path to an absolute URL
     * 
     * @param path the path to transform to an URL
     * @return an URL corresponding to the given path
     */
    public Uri pathToUri(String path) {
        Uri p = new Uri(path);
        return pathToUri(p);
    }

    /**
     * Transforms the given path to an absolute URL
     * 
     * @param path the path to transform to an URL
     * @return an URL corresponding to the given path
     */
    public Uri pathToUri(Uri path) {
        String key = SiteUrlMapper.getPathKey(path, false);
        SiteUrlMapper tranlator = fPathToUrlTranslators.getNearestValue(key);
        Uri result = null;
        if (tranlator != null) {
            String str = tranlator.pathToUri(key);
            result = toUri(str);
        }
        return result;
    }

    @Override
    public String toString() {
        return ""
            + "PathToUrl:"
            + fPathToUrlTranslators
            + "\n"
            + "UrlToPath:"
            + fUrlToPathTranslators
            + "";
    }

    /**
     * Transforms the given string to an absolute URL.
     * 
     * @param str the string to transform to an URL
     * @return a newly created URI instance
     */
    private Uri toUri(String str) {
        return str != null ? new Uri(str) : null;
    }

    /**
     * Transforms the given absolute URL to the corresponding local path
     * 
     * @param uri the absolute URL to transformate to path
     * @return an path corresponding to the given URL
     */
    public Uri uriToPath(String uri) {
        Uri u = new Uri(uri);
        return uriToPath(u);
    }

    /**
     * Transforms the given absolute URL to the corresponding local path
     * 
     * @param uri the absolute URL to transformate to path
     * @return an path corresponding to the given URL
     */
    public Uri uriToPath(Uri uri) {
        String key = SiteUrlMapper.getUriKey(uri);
        SiteUrlMapper tranlator = fUrlToPathTranslators.getNearestValue(key);
        Uri result = null;
        if (tranlator != null) {
            String str = tranlator.uriToPath(uri.toString());
            result = toUri(str);
        }
        return result;
    }

}