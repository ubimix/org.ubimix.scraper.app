/**
 * 
 */
package org.webreformatter.scrapper.protocol;

import java.util.HashMap;
import java.util.Map;

/**
 * @author kotelnikov
 */
public class HttpStatusCode {

    public enum ResponseClass {

        // 1xx Informational
        // ActionRequest received, continuing process.
        // This class of status code indicates a provisional response,
        // consisting only of the Status-Line and optional headers, and is
        // terminated by an empty line. Since HTTP/1.0 did not define any 1xx
        // status codes, servers must not send a 1xx response to an HTTP/1.0
        // client except under experimental conditions.
        CLASS_1xx(1, "Informational"),

        // 2xx Success
        // This class of status codes indicates the action requested by the
        // client was received, understood, accepted and processed successfully.
        CLASS_2xx(2, "Success"),

        // 3xx Redirection
        // The client must take additional action to complete the request.
        // This class of status code indicates that further action needs to be
        // taken by the user agent in order to fulfil the request. The action
        // required may be carried out by the user agent without interaction
        // with the user if and only if the method used in the second request is
        // GET or HEAD. A user agent should not automatically redirect a request
        // more than five times, since such redirections usually indicate an
        // infinite loop.
        CLASS_3xx(3, "Redirection"),

        // 4xx Client Error
        // The 4xx class of status code is intended for cases in which the
        // client seems to have erred. Except when responding to a HEAD request,
        // the server should include an entity containing an explanation of the
        // error situation, and whether it is a temporary or permanent
        // condition. These status codes are applicable to any request method.
        // User agents should display any included entity to the user. These are
        // typically the most common error codes encountered while online.
        CLASS_4xx(4, "Client Error"),

        // 5xx Server Error
        // The server failed to fulfill an apparently valid request.
        // ActionResponse status codes beginning with the digit "5" indicate
        // cases in
        // which the server is aware that it has encountered an error or is
        // otherwise incapable of performing the request. Except when responding
        // to a HEAD request, the server should include an entity containing an
        // explanation of the error situation, and indicate whether it is a
        // temporary or permanent condition. Likewise, user agents should
        // display any included entity to the user. These response codes are
        // applicable to any request method.
        CLASS_5xx(5, "Server Error");

        public static ResponseClass getGroup(int code) {
            int key = code / 100;
            ResponseClass result = null;
            switch (key) {
                case 1:
                    result = CLASS_1xx;
                    break;
                case 2:
                    result = CLASS_2xx;
                    break;
                case 3:
                    result = CLASS_3xx;
                    break;
                case 4:
                    result = CLASS_4xx;
                    break;
                case 5:
                    result = CLASS_5xx;
                    break;
            }
            return result;
        }

        private final int fCode;

        private String fDescription;

        ResponseClass(int code, String description) {
            fCode = code;
            fDescription = description;
        }

        @Override
        public String toString() {
            return fCode + "xx - " + fDescription;
        }

    }

    public final static Map<Integer, HttpStatusCode> fCodeMap = new HashMap<Integer, HttpStatusCode>();

    public final static HttpStatusCode STATUS_100 = addCode(100, "Continue");

    public final static HttpStatusCode STATUS_101 = addCode(
        101,
        "Switching Protocols");

    public final static HttpStatusCode STATUS_102 = addCode(
        102,
        "Processing (WebDAV) (RFC 2518)");

    public final static HttpStatusCode STATUS_200 = addCode(200, "OK");

    public final static HttpStatusCode STATUS_201 = addCode(201, "Created");

    public final static HttpStatusCode STATUS_202 = addCode(202, "Accepted");

    public final static HttpStatusCode STATUS_203 = addCode(
        203,
        "Non-Authoritative Information (since HTTP/1.1)");

    public final static HttpStatusCode STATUS_204 = addCode(204, "No Content");

    public final static HttpStatusCode STATUS_205 = addCode(
        205,
        "Reset Content");

    public final static HttpStatusCode STATUS_206 = addCode(
        206,
        "Partial Content");

    public final static HttpStatusCode STATUS_207 = addCode(
        207,
        "Multi-Status (WebDAV) (RFC 4918)");

    public final static HttpStatusCode STATUS_300 = addCode(
        300,
        "Multiple Choices");

    public final static HttpStatusCode STATUS_301 = addCode(
        301,
        "Moved Permanently");

    public final static HttpStatusCode STATUS_302 = addCode(302, "Found");

    public final static HttpStatusCode STATUS_303 = addCode(
        303,
        "See Other (since HTTP/1.1)");

    public final static HttpStatusCode STATUS_304 = addCode(304, "Not Modified");

    public final static HttpStatusCode STATUS_305 = addCode(
        305,
        "Use Proxy (since HTTP/1.1)");

    public final static HttpStatusCode STATUS_306 = addCode(306, "Switch Proxy");

    public final static HttpStatusCode STATUS_307 = addCode(
        307,
        "Temporary Redirect (since HTTP/1.1)");

    public final static HttpStatusCode STATUS_400 = addCode(
        400,
        "Bad ActionRequest");

    public final static HttpStatusCode STATUS_401 = addCode(401, "Unauthorized");

    public final static HttpStatusCode STATUS_402 = addCode(
        402,
        "Payment Required");

    public final static HttpStatusCode STATUS_403 = addCode(403, "Forbidden");

    public final static HttpStatusCode STATUS_404 = addCode(404, "Not Found");

    public final static HttpStatusCode STATUS_405 = addCode(
        405,
        "Method Not Allowed");

    public final static HttpStatusCode STATUS_406 = addCode(
        406,
        "Not Acceptable");

    public final static HttpStatusCode STATUS_407 = addCode(
        407,
        "Proxy Authentication Required");

    public final static HttpStatusCode STATUS_408 = addCode(
        408,
        "ActionRequest Timeout");

    public final static HttpStatusCode STATUS_409 = addCode(409, "Conflict");

    public final static HttpStatusCode STATUS_410 = addCode(410, "Gone");

    public final static HttpStatusCode STATUS_411 = addCode(
        411,
        "Length Required");

    public final static HttpStatusCode STATUS_412 = addCode(
        412,
        "Precondition Failed");

    public final static HttpStatusCode STATUS_413 = addCode(
        413,
        "ActionRequest Entity Too Large");

    public final static HttpStatusCode STATUS_414 = addCode(
        414,
        "ActionRequest-URI Too Long");

    public final static HttpStatusCode STATUS_415 = addCode(
        415,
        "Unsupported Media Type");

    public final static HttpStatusCode STATUS_416 = addCode(
        416,
        "Requested Range Not Satisfiable");

    public final static HttpStatusCode STATUS_417 = addCode(
        417,
        "Expectation Failed");

    public final static HttpStatusCode STATUS_418 = addCode(418, "I'm a teapot");

    public final static HttpStatusCode STATUS_421 = addCode(
        421,
        "There are too many connections from your internet address");

    public final static HttpStatusCode STATUS_422 = addCode(
        422,
        "Unprocessable Entity (WebDAV) (RFC 4918)");

    public final static HttpStatusCode STATUS_423 = addCode(
        423,
        "Locked (WebDAV) (RFC 4918)");

    public final static HttpStatusCode STATUS_424 = addCode(
        424,
        "Failed Dependency (WebDAV) (RFC 4918)");

    public final static HttpStatusCode STATUS_425 = addCode(
        425,
        "Unordered Collection (RFC 3648)");

    public final static HttpStatusCode STATUS_426 = addCode(
        426,
        "Upgrade Required (RFC 2817)");

    public final static HttpStatusCode STATUS_449 = addCode(449, "Retry With");

    public final static HttpStatusCode STATUS_450 = addCode(
        450,
        "Blocked by Windows Parental Controls");

    public final static HttpStatusCode STATUS_500 = addCode(
        500,
        "Internal Server Error");

    public final static HttpStatusCode STATUS_501 = addCode(
        501,
        "Not Implemented");

    public final static HttpStatusCode STATUS_502 = addCode(502, "Bad Gateway");

    public final static HttpStatusCode STATUS_503 = addCode(
        503,
        "Service Unavailable");

    public final static HttpStatusCode STATUS_504 = addCode(
        504,
        "Gateway Timeout");

    public final static HttpStatusCode STATUS_505 = addCode(
        505,
        "HTTP Version Not Supported");

    public final static HttpStatusCode STATUS_506 = addCode(
        506,
        "Variant Also Negotiates (RFC 2295)");

    public final static HttpStatusCode STATUS_507 = addCode(
        507,
        "Insufficient Storage (WebDAV) (RFC 4918)[4]");

    public final static HttpStatusCode STATUS_509 = addCode(
        509,
        "Bandwidth Limit Exceeded (Apache bw/limited extension)");

    public final static HttpStatusCode STATUS_510 = addCode(
        510,
        "Not Extended (RFC 2774)");

    public final static HttpStatusCode STATUS_530 = addCode(
        530,
        "User access denied");

    public static void addCode(HttpStatusCode statusCode) {
        fCodeMap.put(statusCode.getStatusCode(), statusCode);
    }

    public static HttpStatusCode addCode(int code, String msg) {
        HttpStatusCode c = new HttpStatusCode(code, msg);
        addCode(c);
        return c;
    }

    public static HttpStatusCode getStatusCode(int code) {
        return fCodeMap.get(code);
    }

    private final int fCode;

    private ResponseClass fGroup;

    private final String fMessage;

    public HttpStatusCode(int code, String msg) {
        fCode = code;
        fMessage = msg;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof HttpStatusCode)) {
            return false;
        }
        HttpStatusCode o = (HttpStatusCode) obj;
        return toString().equals(o.toString());
    }

    public ResponseClass getGroup() {
        if (fGroup == null) {
            fGroup = ResponseClass.getGroup(fCode);
        }
        return fGroup;
    }

    public String getMessage() {
        return fMessage;
    }

    public int getStatusCode() {
        return fCode;
    }

    @Override
    public int hashCode() {
        return fCode;
    }

    public boolean isClientError() {
        return ResponseClass.CLASS_4xx.equals(getGroup());
    }

    public boolean isError() {
        ResponseClass group = getGroup();
        return ResponseClass.CLASS_4xx.equals(group)
            || ResponseClass.CLASS_5xx.equals(group);
    }

    public boolean isOk() {
        return ResponseClass.CLASS_2xx.equals(getGroup());
    }

    public boolean isServerError() {
        return ResponseClass.CLASS_5xx.equals(getGroup());
    }

    @Override
    public String toString() {
        return fCode + " - " + fMessage;
    }

}
