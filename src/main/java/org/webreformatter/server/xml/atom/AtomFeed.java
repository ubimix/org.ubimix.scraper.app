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

    public static final String _PREFIX_ATOM = "atom";

    private static SimpleNamespaceContext ATOM_NAMESPACE_CONTEXT = new SimpleNamespaceContext(
        _PREFIX_ATOM,
        _NS_ATOM);

    private static void checkAtomNamespace(XmlContext context) {
        CompositeNamespaceContext namespaceContext = context
            .getNamespaceContext();
        namespaceContext.addContext(ATOM_NAMESPACE_CONTEXT);
    }

    private static XmlWrapper wrap(Document doc) throws XmlException {
        XmlContext context = XmlContext.builder().build();
        checkAtomNamespace(context);
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
        checkAtomNamespace(context);
    }

    public AtomFeed(String doc) throws XmlException, IOException {
        this(XmlWrapper.readXML(doc));
    }

    public AtomFeed(XmlContext xmlContext) throws XmlException {
        super(XmlWrapper.newDocument(), xmlContext);
        checkAtomNamespace(xmlContext);
        appendElement("atom:feed");
    }

    public AtomFeed(XmlWrapper wrapper) {
        super(wrapper);
        checkAtomNamespace(getXmlContext());
    }

    public AtomEntry addEntry() throws XmlException {
        AtomEntry entry = appendElement("atom:entry", AtomEntry.class);
        return entry;
    }

    public List<AtomEntry> getEntries() throws XmlException {
        List<AtomEntry> result = evalList("atom:entry", AtomEntry.class);
        return result;
    }

    public String getSubtitle() throws XmlException {
        return evalStr("atom:subtitle");
    }

}