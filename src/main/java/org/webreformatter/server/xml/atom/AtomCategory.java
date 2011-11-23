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

import org.w3c.dom.Node;
import org.webreformatter.server.xml.XmlException;
import org.webreformatter.server.xml.XmlWrapper;

public class AtomCategory extends AtomItem {

    public AtomCategory(Node node, XmlContext context) {
        super(node, context);
    }

    public AtomCategory(XmlWrapper wrapper) {
        super(wrapper);
    }

    public String getLabel() throws XmlException {
        return evalStr("@label");
    }

    public String getScheme() throws XmlException {
        return evalStr("@scheme");
    }

    public String getTerm() throws XmlException {
        return evalStr("@term");
    }
}