/**
 * 
 */
package li.moskito.awtt.protocol.http;

import li.moskito.awtt.protocol.HeaderFieldDefinition;

/**
 * see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec5.html#sec5.3">HTTP Request Header Fields</a>
 * 
 * @author Gerald
 */
public enum RequestHeaders implements HeaderFieldDefinition {

    ACCEPT("Accept"),
    ACCEPT_CHARSET("Accept-Charset"),
    ACCEPT_ENCODING("Accept-Encoding"),
    ACCEPT_LANGUAGE("Accept-Language"),
    AUTHORIZATION("Authorization"),
    CACHE_CONTROL("Cache-Control"),
    CONNECTION("Connection"),
    // COOKIE("Cookie"), //not part of standard
    CONTENT_LENGTH("Content-Length"),
    CONTENT_MD5("Content-MD5"),
    CONTENT_TYPE("Content-Type"),
    DATE("Date"),
    EXPECT("Expect"),
    FROM("From"),
    HOST("Host"),
    IF_MATCH("If-Match"),
    IF_MODIFIED_SINCE("If-Modified-Since"),
    IF_NONE_MATCH("If-None-Match"),
    IF_RANGE("If-Range"),
    IF_UNMODIFIED_SINCE("If-Unmodified-Since"),
    MAX_FORWARDS("Max-Forwards"),
    PRAGMA("Pragma"),
    PROXY_AUTHORIZATION("Proxy-Authorization"),
    RANGE("Range"),
    REFERER("Referer"),
    TE("TE"),
    TRANSFER_ENCODING("Transfer-Encoding"),
    UPGRADE("Upgrade"),
    USER_AGENT("User-Agent"),
    VIA("Via"),
    WARNING("Warning"), ;

    private String headerFieldName;

    // TODO add value validation regex
    private RequestHeaders(final String headerFieldName) {
        this.headerFieldName = headerFieldName;
    }

    @Override
    public String toString() {
        return this.headerFieldName;
    }

    /**
     * @param group
     * @return
     */
    public static RequestHeaders fromString(final String string) {
        for (final RequestHeaders field : values()) {
            if (field.headerFieldName.equals(string)) {
                return field;
            }
        }
        return valueOf(string);
    }

    @Override
    public String getName() {
        return this.headerFieldName;
    }
}
