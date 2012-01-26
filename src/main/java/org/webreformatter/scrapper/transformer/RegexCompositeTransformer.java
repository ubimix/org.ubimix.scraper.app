package org.webreformatter.scrapper.transformer;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.webreformatter.commons.uri.Uri;
import org.webreformatter.commons.xml.XmlException;
import org.webreformatter.commons.xml.XmlWrapper;
import org.webreformatter.commons.xml.atom.AtomFeed;

/**
 * Dispatcher to external transformers. It uses regular expressions to delegate
 * document handling to specific transformers. Each regular expression is
 * applied in the natural order. On first match, the corresponding transformer
 * is used.
 * 
 * @see CompositeTransformer
 * 
 * @author arkub
 * 
 */

public class RegexCompositeTransformer implements IDocumentTransformer {

    public Map<Pattern, IDocumentTransformer> fMap = new LinkedHashMap<Pattern, IDocumentTransformer>();

    public void addTransformer(String urlRegexp, IDocumentTransformer normalizer) {
        Pattern regexp = Pattern.compile(urlRegexp);
        fMap.put(regexp, normalizer);
    }

    public void removeTransformer(String urlRegexp) {
        Pattern pattern = null;
        for (Pattern key : fMap.keySet()) {
            if (key.pattern().equals(urlRegexp)) {
                pattern = key;
            }
        }
        if (pattern != null) {
            fMap.remove(pattern);
        }
    }

    public AtomFeed transformDocument(Uri url, XmlWrapper doc)
            throws XmlException, IOException {
        AtomFeed result = null;
        String str = url.toString();
        for (Map.Entry<Pattern, IDocumentTransformer> entry : fMap.entrySet()) {
            Pattern pattern = entry.getKey();
            Matcher matcher = pattern.matcher(str);
            if (matcher.lookingAt()) {
                IDocumentTransformer normalizer = entry.getValue();
                result = normalizer.transformDocument(url, doc);
                if (result != null) {
                    break;
                }
            }
        }
        return result;
    }

}