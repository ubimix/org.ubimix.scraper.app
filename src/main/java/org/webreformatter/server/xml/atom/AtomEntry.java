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

    protected XmlWrapper appendChildren(
        String name,
        XmlWrapper tag,
        boolean clear) throws XmlException {
        XmlWrapper contentTag = getOrCreateElement(name);
        if (clear) {
            contentTag.removeChildren();
        }
        if (tag != null) {
            XmlWrapper.appendChildren(contentTag, tag, true);
        }
        return contentTag;
    }

    public XmlWrapper appendContent(String content) throws XmlException {
        return appendContent("atom:content", content, false);
    }

    protected XmlWrapper appendContent(
        String name,
        String content,
        boolean clear) throws XmlException {
        XmlWrapper contentTag = getOrCreateElement(name);
        if (clear) {
            contentTag.removeChildren();
        }
        contentTag.appendText(content);
        return contentTag;
    }

    public XmlWrapper appendContent(XmlWrapper wrapper) throws XmlException {
        return appendChildren("atom:content", wrapper, false);
    }

    protected XmlWrapper appendTag(String name, XmlWrapper tag, boolean clear)
        throws XmlException {
        XmlWrapper contentTag = getOrCreateElement(name);
        if (clear) {
            contentTag.removeChildren();
        }
        if (tag != null) {
            contentTag.append(tag);
        }
        return contentTag;
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

    public XmlWrapper getContentXml() throws XmlException {
        return getContentXml(XmlWrapper.class);
    }

    public <T extends XmlWrapper> T getContentXml(Class<T> type)
        throws XmlException {
        return eval("atom:content", type);
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

    public XmlWrapper getTitleXml() throws XmlException {
        return getTitleXml(XmlWrapper.class);
    }

    public <T extends XmlWrapper> T getTitleXml(Class<T> type)
        throws XmlException {
        return eval("atom:title", type);
    }

    public Date getUpdated() throws XmlException {
        return evalDate("atom:updated");
    }

    public XmlWrapper setContent(String content) throws XmlException {
        return appendContent("atom:content", content, true);
    }

    public XmlWrapper setContent(XmlWrapper wrapper) throws XmlException {
        return appendChildren("atom:content", wrapper, true);
    }

    public XmlWrapper setContentAsXml(String content)
        throws XmlException,
        IOException {
        XmlWrapper tag = getXmlContext().readXML(content);
        return appendTag("atom:content", tag, true);
    }

    public XmlWrapper setTitle(String title) throws XmlException {
        return appendContent("atom:title", title, true);
    }

    public XmlWrapper setTitle(XmlWrapper titleContent) throws XmlException {
        return appendChildren("atom:title", titleContent, true);
    }

}