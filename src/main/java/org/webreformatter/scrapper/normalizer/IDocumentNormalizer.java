package org.webreformatter.scrapper.normalizer;

import java.io.IOException;

import org.webreformatter.scrapper.context.RuntimeContext;
import org.webreformatter.server.xml.XmlException;
import org.webreformatter.server.xml.XmlWrapper;
import org.webreformatter.server.xml.atom.AtomFeed;

/**
 * @author kotelnikov
 */
public interface IDocumentNormalizer {

    AtomFeed getNormalizedContent(RuntimeContext context, XmlWrapper doc)
        throws XmlException,
        IOException;

    int getPriority();
}