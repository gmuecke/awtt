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

    /**
     * Info: 100 Continue
     */
    CONTINUE(100, "Continue"),
    /**
     * Info: 101 Switch Protocols
     */
    SWITCH_PROTOCOLS(101, "Switching Protocols"),

    /**
     * Successful: 200 OK
     */
    OK(200, "OK"),
    /**
     * Successful: 201 Created
     */
    CREATED(201, "Created"),
    /**
     * Successful: 202 Accepted
     */
    ACCEPTED(202, "Accepted"),
    /**
     * Successful: 203 Non-Authoritavie Information
     */
    NON_AUTHORATIVE_INFORMATION_203(203, "Non-Authorative Information"),
    /**
     * Successful: 204 No Content
     */
    NO_CONTENT(204, "No Content"),
    /**
     * Successful: 205 Reset Content
     */
    RESET_CONTENT(205, "Reset Content"),
    /**
     * Successful: 206 Partial Content
     */
    PARTIAL_CONTENT(206, "Partial Content"),

    /**
     * Redirection: 300 Multiple Choices
     */
    MULTIPLE_CHOICES(300, "Multiple Choices"),
    /**
     * Redirection: 301 Moved Permanently
     */
    MOVED_PERMANENTLY(301, "Moved Permanently"),
    /**
     * Redirection: 302 Found
     */
    FOUND(302, "Found"),
    /**
     * Redirection: 303 See Other
     */
    SEE_OTHER(303, "See Other"),
    /**
     * Redirection: 304 Not Modified
     */
    NOT_MODIFIED(304, "Not Modified"),
    /**
     * Redirection: 305 Use Proxy
     */
    USE_PROXY(305, "Use Proxy"),
    /**
     * Redirection: 306 Unused
     */
    UNUSED(306, "(Unused)"),
    /**
     * Redirection: 307 Temporary Redirect
     */
    TEMPORARY_REDIRECT(307, "Temporary Redirect"),

    /**
     * Client Error: 400 Bad Request
     */
    BAD_REQUEST(400, "Bad Request"),
    /**
     * Client Error: 401 Unauthorized
     */
    UNAUTHORIZED(401, "Unauthorized"),
    /**
     * Client Error: 402 Payment Required
     */
    PAYMENT_REQUIRED(402, "Payment Required"),
    /**
     * Client Error: 403 Forbidden
     */
    FORBIDDEN(403, "Forbidden"),
    /**
     * Client Error: 404 Not Found
     */
    NOT_FOUND(404, "Not Found"),
    /**
     * Client Error: 405 Method Not Allowed
     */
    METHOD_NOT_ALLOWED(405, "Method Not Allowed"),
    /**
     * Client Error: 406 Not Acceptable
     */
    NOT_ACCEPTABLE(406, "Not Acceptable"),
    /**
     * Client Error: 407 Proxy Authentication Required
     */
    PROXY_AUTHENTICATION_REQUIRED(407, "Proxy Authentication Required"),
    /**
     * Client Error: 408 Request Timeout
     */
    REQUEST_TIMEOUT(408, "Request Timeout"),
    /**
     * Client Error: 409 Conflict
     */
    CONFLICT(409, "Conflict"),
    /**
     * Client Error: 410 Gone
     */
    GONE(410, "Gone"),
    /**
     * Client Error: 411 Length Required
     */
    LENGTH_REQUIRED(411, "Length Required"),
    /**
     * Client Error: 412 Precondition Failed
     */
    PRECONDITION_FAILED(412, "Precondition Failed"),
    /**
     * Client Error: 413 Request Entity Too Large
     */
    REQUEST_ENTITY_TOO_LARGE(413, "Request Entity Too Large"),
    /**
     * Client Error: 414 Request-URI Too Long
     */
    REQUEST_URI_TOO_LONG(414, "Request-URI Too Long"),
    /**
     * Client Error: 415 Unsupported Media Type
     */
    UNSUPPORTED_MEDIA_TYPE(415, "Unsupported Media Type"),
    /**
     * Client Error: 416 Request Range Not Satisfiable
     */
    REQUESTED_RANGE_NOT_SATISFIABLE(416, "Requested Range Not Satisfiable"),
    /**
     * Client Error: 417 Expectation Failed
     */
    EXPECTATION_FAILED(417, "Expectation Failed"),

    /**
     * Server Error: 500 Internal Server Error
     */
    INTERNAL_SERVER_ERROR(500, "Internal Server Error"),
    /**
     * Server Error: 501 Not Implemented
     */
    NOT_IMPLEMENTED(501, "Not Implemented"),
    /**
     * Server Error: 502 Bad Gateway
     */
    BAD_GATEWAY(502, "Bad Gateway"),
    /**
     * Server Error: 503 Service Unavailable
     */
    SERVICE_UNAVAILABLE(503, "Service Unavailable"),
    /**
     * Server Error: 504 Gateway Timeout
     */
    GATEWAY_TIMEOUT(504, "Gateway Timeout"),
    /**
     * Server Error: 505 HTTP Version Not Supported
     */
    HTTP_VERSION_NOT_SUPPORTED(505, "HTTP Version Not Supported"),

    ;
    private String reason;
    private int code;

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
