package org.webreformatter.scrapper.core;

import java.util.List;

import org.webreformatter.commons.uri.Uri;

/**
 * @author kotelnikov
 */
public interface IAccessConfig {

    public static interface ICredentials {

        Uri getBaseDomain();

        String getLogin();

        String getPassword();
    }

    List<IAccessConfig.ICredentials> getCredentials();

}