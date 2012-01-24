/**
 * 
 */
package org.webreformatter.scrapper.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.auth.params.AuthPNames;
import org.apache.http.client.params.AuthPolicy;
import org.apache.http.client.protocol.RequestAcceptEncoding;
import org.apache.http.client.protocol.ResponseContentEncoding;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.BrowserCompatHostnameVerifier;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.RequestUserAgent;
import org.webreformatter.commons.uri.Uri;

/**
 * @author kotelnikov
 */
public class HttpConnection {

    private final static Logger log = Logger
        .getLogger(HttpProtocolHandler.class.getName());

    private SchemeRegistry fSchemeRegistry = new SchemeRegistry();

    /**
     * @throws IOException
     */
    public HttpConnection() throws IOException {
        initSSL();
    }

    public HttpResponse get(Uri url, String login, String password)
        throws IOException {
        Uri.Builder pathBuilder = new Uri.Builder();
        pathBuilder.setFullPath(url.getPath());
        pathBuilder.setQuery(url.getQuery());
        String path = pathBuilder.getUri(true, true);
        HttpRequest httpRequest = new BasicHttpRequest(
            "GET",
            path,
            HttpVersion.HTTP_1_1);
        String hostName = url.getBuilder().setUserInfo(null).getHost();
        int port = url.getPort();
        if (port <= 0) {
            port = -1;
        }
        String scheme = url.getScheme();
        HttpHost host = new HttpHost(hostName, port, scheme);

        httpRequest.setHeader(
            "Accept-Charset",
            "UTF-8,ISO-8859-1;q=0.7,*;q=0.3");

        DefaultHttpClient httpClient = getClient();
        try {
            if (login != null) {
                httpClient.getCredentialsProvider().setCredentials(
                    new AuthScope(host.getHostName(), host.getPort()),
                    new UsernamePasswordCredentials(login, password));
            }

            HttpResponse httpResponse = httpClient.execute(host, httpRequest);
            return httpResponse;
        } finally {
            releaseClient(httpClient);
        }

    }

    private synchronized DefaultHttpClient getClient() throws IOException {
        DefaultHttpClient client = newClient();
        return client;
    }

    protected int getMaxClientNumber() {
        return 10;
    }

    private IOException handleError(String msg, Throwable e) {
        if (e instanceof IOException) {
            return (IOException) e;
        }
        log.log(Level.FINE, msg, e);
        return new IOException(msg, e);
    }

    private void initSSL() throws IOException {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            X509TrustManager trustManager = new X509TrustManager() {

                public void checkClientTrusted(
                    X509Certificate[] chain,
                    String authType) throws CertificateException {
                    System.out.println("checkClientTrusted");
                }

                public void checkServerTrusted(
                    X509Certificate[] chain,
                    String authType) throws CertificateException {
                    System.out.println("checkServerTrusted");
                }

                public X509Certificate[] getAcceptedIssuers() {
                    System.out.println("getAcceptedIssuers");
                    return null;
                }
            };
            sslContext.init(null, new TrustManager[] { trustManager }, null);
            X509HostnameVerifier hostnameVerifier = new AllowAllHostnameVerifier();
            hostnameVerifier = new BrowserCompatHostnameVerifier();
            SSLSocketFactory ssf = new SSLSocketFactory(
                sslContext,
                hostnameVerifier);
            Scheme http = new Scheme(
                "http",
                80,
                PlainSocketFactory.getSocketFactory());
            fSchemeRegistry.register(http);
            Scheme https = new Scheme("https", 443, ssf);
            fSchemeRegistry.register(https);
        } catch (Throwable t) {
            throw handleError("Can not initialize SSL", t);
        }
    }

    protected DefaultHttpClient newClient() throws IOException {
        SingleClientConnManager connectionManager = new SingleClientConnManager(
            fSchemeRegistry);
        // SingleClientConnManager connectionManager = new
        // SingleClientConnManager();
        DefaultHttpClient httpclient = new DefaultHttpClient(connectionManager);
        httpclient.removeRequestInterceptorByClass(RequestUserAgent.class);
        httpclient.addRequestInterceptor(new HttpRequestInterceptor() {
            public void process(HttpRequest request, HttpContext context)
                throws HttpException,
                IOException {
                request.setHeader(
                    HTTP.USER_AGENT,
                    "Mozilla/5.0 (compatible; WrfBot/1.0)");
            }
        });
        httpclient.addRequestInterceptor(new RequestAcceptEncoding());
        httpclient.addResponseInterceptor(new ResponseContentEncoding());
        List<String> authpref = new ArrayList<String>();
        authpref.add(AuthPolicy.BASIC);
        authpref.add(AuthPolicy.DIGEST);
        HttpParams params = httpclient.getParams();
        params.setParameter(AuthPNames.PROXY_AUTH_PREF, authpref);
        params.setParameter(AuthPNames.TARGET_AUTH_PREF, authpref);
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        return httpclient;
    }

    public InputStream openStream(HttpResponse response) throws IOException {
        HttpEntity entity = response.getEntity();
        InputStream input = entity.getContent();
        Header encoding = entity.getContentEncoding();
        if (encoding != null && "gzip".equalsIgnoreCase(encoding.getValue())) {
            input = new GZIPInputStream(input);
        }
        return input;
    }

    private synchronized void releaseClient(DefaultHttpClient httpClient) {
    }
}
