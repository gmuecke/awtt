/**
 * 
 */
package li.moskito.awtt.protocol.http;

/**
 * see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec5.html#sec5.3">HTTP Request Header Fields</a>
 * 
 * @author Gerald
 */
public enum ResponseHeaderFieldDefinitions implements HeaderFieldDefinition {

    ACCEPT_RANGES("Accept-Ranges"),
    AGE("Age"),
    ALLOW("Allow"),
    AUTHORIZATION("Authorization"),
    CACHE_CONTROL("Cache-Control"),
    CONNECTION("Connection"),
    CONTENT_ENCODING("Content-Encoding"),
    CONTENT_LANGUAGE("Content-Language"),
    CONTENT_LENGTH("Content-Length"),
    CONTENT_LOCATION("Content-Location"),
    CONTENT_MD5("Content-MD5"),
    // CONTENT_DISPOSITION("Content-Disposition"), //considered harmful (wikipedia)
    CONTENT_RANGE("Content-Range"),
    // CONTENT_SECURITY_POLICY("Content-Security-Polica"), //not part of http11 standard
    CONTENT_TYPE("Content-Type"),
    DATE("Date"),
    ETAG("ETag"),
    EXPIRES("Expires"),
    LAST_MODIFIED("Last-Modified"),
    // LINK("Link"),//not part of http11 standard
    LOCATION("Location"),
    PRAGMA("Pragma"),
    PROXY_AUTHENTICATE("Proxy-Authenticate"),
    REFRESH("Refresh"),
    RETRY_AFTER("Retry-After"),
    SERVER("Server"),
    // SET_COOKIE("Set-Cookie"), //not part of http11 standard
    TRAILER("Trailer"),
    TRANSFER_ENCODING("Transfer-Encoding"),
    VARY("Vary"),
    VIA("Via"),
    WARNING("Warning"),
    WWW_AUTHENTICATE("WWW-Authenticate"), ;

    private String headerFieldName;

    // TODO add regex for value validation
    private ResponseHeaderFieldDefinitions(final String headerFieldName) {
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
    public static ResponseHeaderFieldDefinitions fromString(final String string) {
        for (final ResponseHeaderFieldDefinitions field : values()) {
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
