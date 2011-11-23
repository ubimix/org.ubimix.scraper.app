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
package org.webreformatter.server.atom;

import java.io.StringReader;

import junit.framework.TestCase;

import org.w3c.dom.Document;
import org.webreformatter.server.xml.XmlWrapper;
import org.webreformatter.server.xml.atom.AtomFeed;

public class JSonFeedSerializerTest extends TestCase {

    public void test() throws Exception {
        String xml = ""
            + "<?xml version='1.0' encoding='UTF-8'?>\n"
            + "<feed xmlns='http://www.w3.org/2005/Atom'>\n"
            + "    <title>Example Feed</title>\n"
            + "    <subtitle>A subtitle.</subtitle>\n"
            + "    <link href='http://example.org/feed/' rel='self' />\n"
            + "    <link href='http://example.org/' />\n"
            + "    <id>urn:uuid:60a76c80-d399-11d9-b91C-0003939e0af6</id>\n"
            + "    <updated>2003-12-13T18:30:02Z</updated>\n"
            + "    <author>\n"
            + "        <name>John Doe</name>\n"
            + "        <email>johndoe@example.com</email>\n"
            + "    </author>\n"
            + "    <entry>\n"
            + "        <title>Atom-Powered Robots Run Amok</title>\n"
            + "        <link href='http://example.org/2003/12/13/atom03' />\n"
            + "        <id>urn:uuid:1225c695-cfb8-4ebb-aaaa-80da344efa6a</id>\n"
            + "        <updated>2003-12-13T18:30:02Z</updated>\n"
            + "        <summary>Some text.</summary>\n"
            + "    </entry>\n"
            + "</feed>\n";
        StringReader reader = new StringReader(xml);
        Document doc = XmlWrapper.readXML(reader);
        AtomFeed feed = new AtomFeed(doc);
    }
}
