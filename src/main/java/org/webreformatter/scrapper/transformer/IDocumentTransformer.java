package org.webreformatter.scrapper.transformer;

import java.io.IOException;

import org.webreformatter.commons.uri.Uri;
import org.webreformatter.server.xml.XmlException;
import org.webreformatter.server.xml.XmlWrapper;
import org.webreformatter.server.xml.atom.AtomFeed;

/**
 * @author arkub
 * 
 */
public interface IDocumentTransformer {
    AtomFeed transformDocument(Uri url, XmlWrapper doc) throws XmlException,
            IOException;
}