/**
 * 
 */
package org.webreformatter.server.mime;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu.medsea.mimeutil.MimeType;
import eu.medsea.mimeutil.MimeUtil2;
import eu.medsea.mimeutil.TextMimeDetector;
import eu.medsea.mimeutil.TextMimeType;
import eu.medsea.mimeutil.handler.TextMimeHandler;

/**
 * @author kotelnikov
 */
public class ExtendedMimeTypeDetector implements IMimeTypeDetector {

    private static final MimeType DEFAULT_MIME_TYPE = new MimeType(
        "application/octet-stream");

    private MimeUtil2 mimeUtil = new MimeUtil2();

    /**
     * 
     */
    public ExtendedMimeTypeDetector() {
        registerMimeDetectors(
            "eu.medsea.mimeutil.detector.MagicMimeMimeDetector",
            "eu.medsea.mimeutil.detector.ExtensionMimeDetector",
            "eu.medsea.mimeutil.detector.OpendesktopMimeDetector");
        TextMimeDetector.registerTextMimeHandler(new TextMimeHandler() {
            private int _OPTIONS = Pattern.DOTALL
                | Pattern.CASE_INSENSITIVE
                | Pattern.MULTILINE;

            public boolean handle(TextMimeType mimeType, String content) {
                boolean result = false;
                if (content.indexOf("<html") >= 0
                    || content.indexOf("<HTML") >= 0) {
                    mimeType.setSubType("html");

                    Pattern REGEXP_CHARSET = Pattern.compile(
                        "^.*charset\\s*=\\s*([^'\"]*).*$",
                        _OPTIONS);

                    Pattern REGEXP_META = Pattern.compile(
                        "^.*<meta.+http\\-equiv(.*)>.*$",
                        _OPTIONS);
                    Matcher matcher = REGEXP_META.matcher(content);
                    if (matcher.matches()) {
                        String str = matcher.group(1);
                        matcher = REGEXP_CHARSET.matcher(str);
                        if (matcher.matches()) {
                            str = matcher.group(1);
                            str = str.trim();
                            if (!"".equals(str)) {
                                mimeType.setEncoding(str);
                            }
                        }
                        mimeType.setSubType("html");
                        result = true;
                    }

                    result = true;
                }
                return result;
            }
        });
    }

    /**
     * @see org.webreformatter.server.mime.IMimeTypeDetector#getMimeType(java.io.InputStream)
     */
    @SuppressWarnings("unchecked")
    public String getMimeType(InputStream input) throws IOException {
        Collection<MimeType> types = mimeUtil.getMimeTypes(input);
        return getPreferredMimeType(types);
    }

    /**
     * @see org.webreformatter.server.mime.IMimeTypeDetector#getMimeTypeByExtension(java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public String getMimeTypeByExtension(String fileName) {
        Collection<MimeType> types = mimeUtil.getMimeTypes(fileName);
        return getPreferredMimeType(types);
    }

    private String getPreferredMimeType(Collection<MimeType> types) {
        if (types == null) {
            return null;
        }
        MimeType type = types.iterator().next();
        return type != null ? type.toString() : null;
    }

    private void registerMimeDetectors(String... classNames) {
        for (String className : classNames) {
            mimeUtil.registerMimeDetector(className);
        }
    }

}
