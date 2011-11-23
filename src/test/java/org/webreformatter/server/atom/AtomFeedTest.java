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

import java.util.List;

import junit.framework.TestCase;

import org.webreformatter.server.xml.atom.AtomCategory;
import org.webreformatter.server.xml.atom.AtomEntry;
import org.webreformatter.server.xml.atom.AtomFeed;
import org.webreformatter.server.xml.atom.AtomItem;
import org.webreformatter.server.xml.atom.AtomPerson;

/**
 * @author kotelnikov
 */
public class AtomFeedTest extends TestCase {

    /**
     * @param name
     */
    public AtomFeedTest(String name) {
        super(name);
    }

    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

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
            + "        <author>\n"
            + "             <name>J.Doe</name>\n"
            + "             <email>johndoe@example.com</email>\n"
            + "         </author>\n"
            + "         <contributor>\n"
            + "             <name>Foo Bar</name>\n"
            + "             <email>foobar@example.com</email>\n"
            + "         </contributor>\n"
            + "         <category term='webdev' label='Web Development' scheme='http://www.example.com/tags'/>\n"
            + "         <category term='standards' label='Web Standards' scheme='http://www.example.com/tags'/>\n"
            + "         <category term='test' />\n"
            + "    </entry>\n"
            + " \n"
            + "</feed>\n";
        AtomFeed feed = new AtomFeed(xml);
        assertNotNull(feed);
        assertEquals(
            "urn:uuid:60a76c80-d399-11d9-b91C-0003939e0af6",
            feed.getId());
        assertEquals("Example Feed", feed.getTitle());
        assertEquals("A subtitle.", feed.getSubtitle());
        assertEquals(
            AtomItem.parseDate("2003-12-13T18:30:02Z"),
            feed.getUpdated());
        assertEquals("http://example.org/feed/", feed.getSelfLink());
        assertEquals("http://example.org/", feed.getLink());

        List<AtomPerson> authors = feed.getAuthors();
        assertNotNull(authors);
        assertEquals(1, authors.size());
        AtomPerson author = authors.get(0);
        assertNotNull(author);
        assertEquals("John Doe", author.getName());
        assertEquals("johndoe@example.com", author.getEmail());

        List<AtomEntry> entries = feed.getEntries();
        assertNotNull(entries);
        assertEquals(1, entries.size());
        AtomEntry entry = entries.get(0);
        assertNotNull(entry);
        assertEquals(
            "urn:uuid:1225c695-cfb8-4ebb-aaaa-80da344efa6a",
            entry.getId());
        assertEquals(
            AtomItem.parseDate("2003-12-13T18:30:02Z"),
            entry.getUpdated());
        assertEquals("Atom-Powered Robots Run Amok", entry.getTitle());
        assertEquals("http://example.org/2003/12/13/atom03", entry.getLink());
        assertEquals("Some text.", entry.getSummary());
        assertEquals(null, entry.getSelfLink());
        authors = entry.getAuthors();
        assertNotNull(authors);
        assertEquals(1, authors.size());
        author = authors.get(0);
        assertNotNull(author);
        assertEquals("J.Doe", author.getName());
        assertEquals("johndoe@example.com", author.getEmail());
        List<AtomPerson> contributors = entry.getContributors();
        assertNotNull(contributors);
        assertEquals(1, contributors.size());
        AtomPerson contributor = contributors.get(0);
        assertNotNull(contributor);
        assertEquals("Foo Bar", contributor.getName());
        assertEquals("foobar@example.com", contributor.getEmail());

        List<AtomCategory> categories = entry.getCategories();
        assertNotNull(categories);
        assertEquals(3, categories.size());
        AtomCategory category = categories.get(0);
        assertNotNull(category);
        assertEquals("webdev", category.getTerm());
        assertEquals("Web Development", category.getLabel());
        assertEquals("http://www.example.com/tags", category.getScheme());

        category = categories.get(1);
        assertNotNull(category);
        assertEquals("standards", category.getTerm());
        assertEquals("Web Standards", category.getLabel());
        assertEquals("http://www.example.com/tags", category.getScheme());

        category = categories.get(2);
        assertNotNull(category);
        assertEquals("test", category.getTerm());
        assertEquals(null, category.getLabel());
        assertEquals(null, category.getScheme());
    }
}
