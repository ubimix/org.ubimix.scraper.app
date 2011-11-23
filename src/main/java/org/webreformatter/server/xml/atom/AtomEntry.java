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
import java.util.Date;
import java.util.List;

import org.w3c.dom.Node;
import org.webreformatter.server.xml.XmlException;
import org.webreformatter.server.xml.XmlWrapper;

public class AtomEntry extends AtomItem {

    public AtomEntry(Node node, XmlContext context) {
        super(node, context);
    }

    public AtomEntry(XmlWrapper wrapper) {
        super(wrapper);
    }

    public List<AtomPerson> getAuthors() throws XmlException {
        return getPersonList("atom:author");
    }

    public List<AtomCategory> getCategories() throws XmlException {
        List<AtomCategory> result = evalList(
            "atom:category",
            AtomCategory.class);
        return result;
    }

    public String getContent() throws XmlException {
        return evalStr("atom:content");
    }

    public String getContentType() throws XmlException {
        String contentType = evalStr("atom:content/@type");
        return contentType != null ? contentType : "text/plain";
    }

    public List<AtomPerson> getContributors() throws XmlException {
        return getPersonList("atom:contributor");
    }

    public String getLink() throws XmlException {
        return evalStr("atom:link[not(@rel)]/@href");
    }

    protected List<AtomPerson> getPersonList(String path) throws XmlException {
        List<AtomPerson> result = evalList(path, AtomPerson.class);
        return result;
    }

    public String getSelfLink() throws XmlException {
        return evalStr("atom:link[@rel='self']/@href");
    }

    public String getSummary() throws XmlException {
        return evalStr("atom:summary");
    }

    public String getTitle() throws XmlException {
        return evalStr("atom:title");
    }

    public Date getUpdated() throws XmlException {
        return evalDate("atom:updated");
    }

    public XmlWrapper setContent(String content) throws XmlException {
        XmlWrapper tag = appendElement("atom:content");
        tag.appendText(content);
        return tag;
    }

    public AtomEntry setContent(XmlWrapper wrapper) throws XmlException {
        return setElementContent(wrapper, "atom:content");
    }

    public XmlWrapper setContentAsXml(String content)
        throws XmlException,
        IOException {
        XmlWrapper contentTag = appendElement("atom:content");
        XmlContext context = contentTag.getXmlContext();
        XmlWrapper child = context.readXML(content);
        contentTag.append(child);
        return contentTag;
    }

    protected AtomEntry setElementContent(XmlWrapper wrapper, String name)
        throws XmlException {
        remove(name);
        XmlWrapper parent = appendElement(name);
        parent.appendCopy(wrapper);
        return this;
    }

    public XmlWrapper setTitle(String title) throws XmlException {
        XmlWrapper tag = appendElement("atom:title");
        tag.appendText(title);
        return tag;
    }
}