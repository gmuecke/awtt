/**
 * 
 */
package li.moskito.awtt.protocol.http;

import li.moskito.awtt.protocol.HeaderField;

/**
 * @author Gerald
 */
public class HttpResponse extends HttpMessage {

    /**
     * Creates a new Http Response
     * 
     * @param version
     */
    public HttpResponse(final HttpVersion version, final HttpStatusCodes status) {
        super(new HttpHeader(version, status));
    }

    /**
     * Creates a Response for HTTP/1.1
     * 
     * @param version
     */
    public HttpResponse(final HttpStatusCodes status) {
        super(new HttpHeader(status));
    }

    /**
     * Convenience method for getHeader()getStatus();
     * 
     * @return
     */
    public HttpStatusCodes getStatusCode() {
        return this.getHeader().getStatusCode();
    }

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder(128);
        buf.append(this.getHeader().getVersion()).append(' ').append(this.getStatusCode()).append(HTTP.CRLF);
        for (final HeaderField field : this.getHeader().getFields()) {
            buf.append(field).append(HTTP.CRLF);
        }
        buf.append(HTTP.CRLF);
        return buf.toString();
    }

}
