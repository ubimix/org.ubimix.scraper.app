package org.webreformatter.scrapper.transformer;

import java.io.IOException;

import org.webreformatter.commons.uri.Path;
import org.webreformatter.commons.uri.Uri;
import org.webreformatter.commons.uri.UriToPath;
import org.webreformatter.commons.uri.path.PathManager;
import org.webreformatter.server.xml.XmlException;
import org.webreformatter.server.xml.XmlWrapper;
import org.webreformatter.server.xml.atom.AtomFeed;

/**
 * Dispatcher to external transformers. The transformations are based on a map
 * of URLs and other transformers. When a given URL is passed, the closest
 * prefix is used (the longest common prefix) for identifying the transformer to
 * be used.
 * 
 * 
 * @author kotelnikov
 * 
 */
public class CompositeTransformer implements IDocumentTransformer {

    private IDocumentTransformer fDefaultTransformer;

    private PathManager<IDocumentTransformer> fUrlMapping = new PathManager<IDocumentTransformer>();

    public CompositeTransformer() {
        this(null);
    }

    public CompositeTransformer(IDocumentTransformer defaultTransformer) {
        setDefaultTransformer(defaultTransformer);
    }

    public void addTransformer(Uri baseUrl, IDocumentTransformer normalizer) {
        Path path = UriToPath.getPath(baseUrl);
        String str = path.toString();
        fUrlMapping.add(str, normalizer);
    }

    public IDocumentTransformer getDefaultTransformer() {
        return fDefaultTransformer;
    }

    public void removeNormalizer(Uri baseUrl) {
        Path path = UriToPath.getPath(baseUrl);
        String str = path.toString();
        fUrlMapping.remove(str);
    }

    public void setDefaultTransformer(IDocumentTransformer defaultTransformer) {
        fDefaultTransformer = defaultTransformer;
    }

    public AtomFeed transformDocument(Uri url, XmlWrapper doc)
            throws XmlException, IOException {
        Path path = UriToPath.getPath(url);
        String str = path.toString();
        AtomFeed result = null;
        IDocumentTransformer normalizer = fUrlMapping.getNearestValue(str);
        if (normalizer == null) {
            normalizer = fDefaultTransformer;
        }
        if (normalizer != null) {
            result = normalizer.transformDocument(url, doc);
        }
        return result;
    }

}