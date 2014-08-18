/**
 * 
 */
package li.moskito.awtt.protocol.http;

import java.net.URI;
import java.util.List;

import li.moskito.awtt.protocol.Header;
import li.moskito.awtt.protocol.HeaderField;
import li.moskito.awtt.protocol.HeaderFieldDefinition;

/**
 * A HTTP header that can be used either for requests or responses, depending on the constructor used.
 * 
 * @author Gerald
 */
public class HttpHeader extends Header {

    private final HttpCommands command;
    private final URI resource;
    private final HttpStatusCodes statusCode;

    /**
     * Creates a HTTP request header for the specified HTTP version.
     * 
     * @param version
     */
    public HttpHeader(final HttpVersion version, final HttpCommands command, final URI resource) {
        super(version);
        this.command = command;
        this.resource = resource;
        this.statusCode = null;

    }

    /**
     * Creates a HTTP request header for HTTP/1.1.
     * 
     * @param version
     */
    public HttpHeader(final HttpCommands command, final URI resource) {
        this(HttpVersion.HTTP_1_1, command, resource);
    }

    /**
     * Creates a HTTP response header for the specified HTTP version.
     * 
     * @param version
     */
    public HttpHeader(final HttpVersion version, final HttpStatusCodes statusCode) {
        super(version);
        this.statusCode = statusCode;
        this.command = null;
        this.resource = null;

    }

    /**
     * Creates a HTTP response header for HTTP/1.1
     * 
     * @param version
     */
    public HttpHeader(final HttpStatusCodes statusCode) {
        this(HttpVersion.HTTP_1_1, statusCode);
    }

    /**
     * Retrieves the command of this request header
     * 
     * @return the command or <code>null</code> if it is a response
     */
    public HttpCommands getCommand() {
        return this.command;
    }

    /**
     * Retrieves the target resource of this request header
     * 
     * @return the resource or <code>null</code> if it is a response
     */
    public URI getResource() {
        return this.resource;
    }

    /**
     * Retrieves the status code of this response header
     * 
     * @return the status code or <code>null</code> if it is a request
     */
    public HttpStatusCodes getStatusCode() {
        return this.statusCode;
    }

    @Override
    public HttpVersion getVersion() {
        return (HttpVersion) super.getVersion();
    }

    /**
     * Checks if the header denotes a request
     * 
     * @return <code>true</code> if it is a request
     */
    public boolean isRequest() {
        return this.command != null && this.resource != null;
    }

    /**
     * Checks if the header denotes a response
     * 
     * @return <code>true</code> if it is a response
     */
    public boolean isResponse() {
        return this.statusCode != null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends HeaderFieldDefinition> HttpHeaderField<T> getField(final T headerFieldDefinition) {
        return (HttpHeaderField<T>) super.getField(headerFieldDefinition);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<HttpHeaderField<? extends HeaderFieldDefinition>> getFields() {
        return super.getFields();
    }

    /**
     * @param fields
     */
    @SuppressWarnings("unchecked")
    public <T extends HeaderFieldDefinition> void addHttpHeaderFields(final List<HttpHeaderField<?>> fields) {
        final List<?> typeErasedFields = fields;
        super.addFields((List<HeaderField<T, ?>>) typeErasedFields);

    }

}
