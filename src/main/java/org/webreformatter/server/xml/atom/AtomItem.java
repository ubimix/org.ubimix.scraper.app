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

import java.util.Date;

import org.w3c.dom.Node;
import org.webreformatter.server.xml.XmlException;
import org.webreformatter.server.xml.XmlWrapper;

public abstract class AtomItem extends XmlWrapper {

    public static String formatDate(Date date) {
        return DateUtil.formatDate(date.getTime());
    }

    public static Date parseDate(String date) {
        if (date == null) {
            return null;
        }
        long time = DateUtil.parseDate(date);
        return new Date(time);
    }

    public AtomItem(Node node, XmlContext context) {
        super(node, context);
    }

    public AtomItem(XmlWrapper wrapper) {
        super(wrapper.getRootNode(), wrapper.getXmlContext());
    }

    public Date evalDate(String xpath) throws XmlException {
        String str = evalStr(xpath);
        Date date = str != null ? parseDate(str) : null;
        return date;
    }

    public String getId() throws XmlException {
        return evalStr("atom:id");
    }

}