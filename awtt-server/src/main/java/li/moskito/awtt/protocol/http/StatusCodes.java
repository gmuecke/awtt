/**
 * 
 */
package li.moskito.awtt.protocol.http;

/**
 * see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10">Status Code Definitions</a>
 * 
 * @author Gerald
 */
public enum StatusCodes {

    // Info 1xx
    INFO_100_CONTINUE(100, "Continue"),
    INFO_101_SWITCH_PROTOCOLS(101, "Switching Protocols"),

    // Sucessful 2xx
    SUCCESSFUL_200_OK(200, "OK"),
    SUCCESSFUL_201_CREATED(201, "Created"),
    SUCCESSFUL_202_ACCEPTED(202, "Accepted"),
    SUCCESSFUL_203_NON_AUTHORATIVE_INFORMATION(203, "Non-Authorative Information"),
    SUCCESSFUL_204_NO_CONTENT(204, "No Content"),
    SUCCESSFUL_205_RESET_CONTENT(205, "Reset Content"),
    SUCCESSFUL_206_PARTIAL_CONTENT(206, "Partial Content"),

    // Redirection 3xx
    REDIRECTION_300_MULTIPLE_CHOICES(300, "Multiple Choices"),
    REDIRECTION_301_MOVED_PERMANENTLY(301, "Moved Permanently"),
    REDIRECTION_302_FOUND(302, "Found"),
    REDIRECTION_303_SEE_OTHER(303, "See Other"),
    REDIRECTION_304_NOT_MODIFIED(304, "Not Modified"),
    REDIRECTION_305_USE_PROXY(305, "Use Proxy"),
    REDIRECTION_306_UNUSED(306, "(Unused)"),
    REDIRECTION_307_TEMPORARY_REDIRECT(307, "Temporary Redirect"),

    // Client Error 4xx
    CLIENT_ERR_400_BAD_REQUEST(400, "Bad Request"),
    CLIENT_ERR_401_UNAUTHORIZED(401, "Unauthorized"),
    CLIENT_ERR_402_PAYMENT_REQUIRED(402, "Payment Required"),
    CLIENT_ERR_403_FORBIDDEN(403, "Forbidden"),
    CLIENT_ERR_404_NOT_FOUND(404, "Not Found"),
    CLIENT_ERR_405_METHOD_NOT_ALLOWED(405, "Method Not Allowed"),
    CLIENT_ERR_406_NOT_ACCEPTABLE(406, "Not Acceptable"),
    CLIENT_ERR_407_PROXY_AUTHENTICATION_REQUIRED(407, "Proxy Authentication Required"),
    CLIENT_ERR_408_REQUEST_TIMEOUT(408, "Request Timeout"),
    CLIENT_ERR_409_CONFLICT(409, "Conflict"),
    CLIENT_ERR_410_GONE(410, "Gone"),
    CLIENT_ERR_411_LENGTH_REQUIRED(411, "Length Required"),
    CLIENT_ERR_412_PRECONDITION_FAILED(412, "Precondition Failed"),
    CLIENT_ERR_413_REQUEST_ENTITY_TOO_LARGE(413, "Request Entity Too Large"),
    CLIENT_ERR_414_REQUEST_URI_TOO_LONG(414, "Request-URI Too Long"),
    CLIENT_ERR_415_UNSUPPORTED_MEDIA_TYPE(415, "Unsupported Media Type"),
    CLIENT_ERR_416_REQUESTED_RANGE_NOT_SATISFIABLE(416, "Requested Range Not Satisfiable"),
    CLIENT_ERR_417_EXPECTATION_FAILED(417, "Expectation Failed"),

    // Server Error 5xx
    SERVER_ERR_500_INTERNAL_SERVER_ERROR(500, "Internal Server Error"),
    SERVER_ERR_501_NOT_IMPLEMENTED(501, "Not Implemented"),
    SERVER_ERR_502_BAD_GATEWAY(502, "Bad Gateway"),
    SERVER_ERR_503_SERVICE_UNAVAILABLE(503, "Service Unavailable"),
    SERVER_ERR_504_GATEWAY_TIMEOUT(504, "Gateway Timeout"),
    SERVER_ERR_505_HTTP_VERSION_NOT_SUPPORTED(505, "HTTP Version Not Supported"),

    ;
    private String reason;
    private int    code;

    /**
     * 
     */
    private StatusCodes(final int code, final String reason) {
        this.code = code;
        this.reason = reason;
    }

    public String getReason() {
        return this.reason;
    }

    public void setReason(final String reason) {
        this.reason = reason;
    }

    public int getCode() {
        return this.code;
    }

    public void setCode(final int code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return this.code + " " + this.reason;
    }

}
