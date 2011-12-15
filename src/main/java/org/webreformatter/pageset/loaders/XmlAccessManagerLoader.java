package org.webreformatter.pageset.loaders;

import java.util.List;

import org.webreformatter.commons.uri.Uri;
import org.webreformatter.scrapper.protocol.AccessManager;
import org.webreformatter.scrapper.protocol.AccessManager.CredentialInfo;
import org.webreformatter.server.xml.XmlException;
import org.webreformatter.server.xml.XmlWrapper;

/**
 * @author kotelnikov
 */
public class XmlAccessManagerLoader extends AbstractXmlLoader {

    public void configure(
        Uri configUri,
        AccessManager accessManager,
        XmlWrapper xml) throws XmlException {
        List<XmlWrapper> list = xml.evalList("//credentials");
        for (XmlWrapper item : list) {
            String str = getValue(item, "baseUrl");
            Uri baseUrl = getUrl(configUri, str);
            String login = getValue(item, "login");
            String password = getValue(item, "password");
            CredentialInfo credentials = new CredentialInfo(login, password);
            accessManager.setCredentials(baseUrl, credentials);
        }
    }

}
