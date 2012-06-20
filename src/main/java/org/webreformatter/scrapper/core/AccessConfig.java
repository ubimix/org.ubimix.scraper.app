package org.webreformatter.scrapper.core;

import java.util.ArrayList;
import java.util.List;

import org.webreformatter.commons.json.JsonArray;
import org.webreformatter.commons.json.JsonObject;
import org.webreformatter.commons.uri.Uri;

/**
 * @author kotelnikov
 */
public class AccessConfig extends JsonArray implements IAccessConfig {

    public static class Credentials extends JsonObject implements ICredentials {

        public static IJsonValueFactory<AccessConfig.Credentials> FACTORY = new IJsonValueFactory<AccessConfig.Credentials>() {
            @Override
            public AccessConfig.Credentials newValue(Object object) {
                return new Credentials().setJsonObject(object);
            }
        };

        @Override
        public Uri getBaseDomain() {
            String url = getString("domain");
            return new Uri(url);
        }

        @Override
        public String getLogin() {
            return getString("login");
        }

        @Override
        public String getPassword() {
            return getString("password");
        }
    }

    public static IJsonValueFactory<AccessConfig> FACTORY = new IJsonValueFactory<AccessConfig>() {
        @Override
        public AccessConfig newValue(Object object) {
            return new AccessConfig().setJsonObject(object);
        }
    };

    @Override
    public List<ICredentials> getCredentials() {
        List<ICredentials> result = new ArrayList<ICredentials>();
        int len = getSize();
        for (int i = 0; i < len; i++) {
            AccessConfig.Credentials credentials = getObject(
                i,
                Credentials.FACTORY);
            result.add(credentials);
        }
        return result;
    }
}