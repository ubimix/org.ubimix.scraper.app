package org.webreformatter.scrapper.transformer;

import java.io.IOException;

import org.webreformatter.commons.uri.Uri;
import org.webreformatter.commons.xml.XmlException;
import org.webreformatter.commons.xml.XmlWrapper;
import org.webreformatter.commons.xml.atom.AtomFeed;

/**
 * @author arkub
 * 
 */
public interface IDocumentTransformer {
    AtomFeed transformDocument(Uri url, XmlWrapper doc) throws XmlException,
            IOException;
}