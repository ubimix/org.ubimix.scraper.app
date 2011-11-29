/*
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership. This file is licensed to you under the Apache
 * License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.webreformatter.server.xml.atom;

import java.io.IOException;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.webreformatter.server.xml.XmlException;
import org.webreformatter.server.xml.XmlWrapper;

public class AtomFeed extends AtomEntry {

    public static final String _NS_ATOM = "http://www.w3.org/2005/Atom";

    public static final String _NS_XHTML = "http://www.w3.org/1999/xhtml";

    public static final String _PREFIX_ATOM = "atom";

    public static final String _PREFIX_XHTML = "html";

    private static SimpleNamespaceContext ATOM_NAMESPACE_CONTEXT = new SimpleNamespaceContext(
        _PREFIX_ATOM,
        _NS_ATOM,
        _PREFIX_XHTML,
        _NS_XHTML);

    public static void checkAtomNamespaces(XmlContext context) {
        CompositeNamespaceContext namespaceContext = context
            .getNamespaceContext();
        namespaceContext.addContext(ATOM_NAMESPACE_CONTEXT);
    }

    private static XmlWrapper wrap(Document doc) throws XmlException {
        XmlContext context = XmlContext.builder().build();
        checkAtomNamespaces(context);
        XmlWrapper result;
        if (doc != null) {
            result = new XmlWrapper(doc, context);
            // result = wrapper.eval("//atom:feed");
        } else {
            doc = XmlWrapper.newDocument();
            result = context.wrap(doc);
            result.appendElement("atom:feed");
        }
        return result;
    }

    public AtomFeed() throws XmlException {
        this(XmlContext.builder().build());
    }

    public AtomFeed(Document doc) throws XmlException {
        this(wrap(doc));
    }

    public AtomFeed(Document doc, XmlContext context) throws XmlException {
        this(wrap(doc));
    }

    public AtomFeed(Node node, XmlContext context) {
        super(node, context);
        checkAtomNamespaces(context);
    }

    public AtomFeed(String doc) throws XmlException, IOException {
        this(XmlWrapper.readXML(doc));
    }

    public AtomFeed(XmlContext xmlContext) throws XmlException {
        super(XmlWrapper.newDocument(), xmlContext);
        checkAtomNamespaces(xmlContext);
        appendElement("atom:feed");
    }

    public AtomFeed(XmlWrapper wrapper) {
        super(wrapper);
        checkAtomNamespaces(getXmlContext());
    }

    public AtomEntry addEntry() throws XmlException {
        return addEntry(AtomEntry.class);
    }

    public <T extends AtomEntry> T addEntry(Class<T> type) throws XmlException {
        T entry = appendElement("atom:entry", type);
        return entry;
    }

    public List<AtomEntry> getEntries() throws XmlException {
        return getEntries(AtomEntry.class);
    }

    public <T extends XmlWrapper> List<T> getEntries(Class<T> type)
        throws XmlException {
        List<T> result = evalList("atom:entry", type);
        return result;
    }

    public String getSubtitle() throws XmlException {
        return evalStr("atom:subtitle");
    }

}