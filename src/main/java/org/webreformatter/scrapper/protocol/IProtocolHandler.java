package org.webreformatter.scrapper.protocol;

import org.webreformatter.commons.uri.Uri;
import org.webreformatter.resources.IWrfResource;

public interface IProtocolHandler {

    HttpStatusCode handleRequest(
        Uri uri,
        String login,
        String password,
        IWrfResource resource);

}