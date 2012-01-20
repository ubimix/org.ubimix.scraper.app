/**
 * 
 */
package org.webreformatter.scrapper.protocol;

import java.io.IOException;


/**
 * @author kotelnikov
 */
public class ProtocolHandlerUtils {

    public static void registerDefaultProtocols(CompositeProtocolHandler handler)
        throws IOException {
        HttpProtocolHandler httpProtocolHandler = new HttpProtocolHandler();
        handler.setProtocolHandler("http", httpProtocolHandler);
        handler.setProtocolHandler("https", httpProtocolHandler);
        handler.setProtocolHandler("file", new FileProtocolHandler());
        handler.setProtocolHandler("classpath", new ClasspathProtocolHandler());

        IProtocolHandler urlBasedHandler = new UrlBasedProtocolHandler();
        handler.setDefaultProtocolHandler(urlBasedHandler);
    }

}
