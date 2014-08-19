/**
 * 
 */
package li.moskito.awtt.protocol.http;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import li.moskito.awtt.protocol.Header;
import li.moskito.awtt.protocol.HeaderField;
import li.moskito.awtt.protocol.MessageChannel;
import li.moskito.awtt.protocol.Protocol;
import li.moskito.awtt.protocol.ProtocolException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gerald
 */
public class HttpChannel extends MessageChannel {

    /**
     * SLF4J Logger for this class
     */
    private static final Logger LOG = LoggerFactory.getLogger(HttpChannel.class);

    private final HTTP protocol;

    /**
     * @param protocol
     */
    public HttpChannel(final HTTP protocol) {
        super();
        this.protocol = protocol;
    }

    @Override
    public Protocol getProtocol() {
        return this.protocol;
    }

    @Override
    protected HttpMessage parseMessage(final ByteBuffer src) throws ProtocolException, IOException {
        return this.parseMessage(HTTP.CHARSET.decode(src));
    }

    /**
     * Parses an entire request from the given CharBuffer. The first line of the string must be a HTTP Request Line
     * 
     * @param charBuffer
     *            the character buffer to read from
     * @return
     * @throws IOException
     * @throws HttpProtocolException
     */
    private HttpRequest parseMessage(final CharBuffer charBuffer) throws HttpProtocolException, IOException {
        // skip to end of line
        final String requestLine = this.readLine(charBuffer);
        if (requestLine == null) {
            return null;
        }
        final HttpRequest result = this.parseRequestLine(requestLine);

        final List<HttpHeaderField> fields = new ArrayList<>();

        String fieldLine = this.readLine(charBuffer);
        while (fieldLine != null) {
            if (fieldLine.trim().isEmpty()) {
                break;
            }
            fields.add(this.parseRequestHeaderField(fieldLine));
            fieldLine = this.readLine(charBuffer);
        }

        result.getHeader().addHttpHeaderFields(fields);
        return result;
    }

    /**
     * Reads a line from the character buffer. After the operation, the position of the buffer will be at the beginning
     * of the next line.
     * 
     * @param charBuffer
     *            the character buffer to read from
     * @return the current line or <code>null</code> if the line was empty
     * @throws HttpProtocolException
     */
    private String readLine(final CharBuffer charBuffer) throws HttpProtocolException {
        final StringBuilder buf = new StringBuilder(8);

        boolean eol = false;
        while (charBuffer.hasRemaining() && !eol) {
            final char c = charBuffer.get();
            switch (c) {
                case '\r':
                    continue;
                case '\n':
                    eol = true;
                    break;
                default:
                    buf.append(c);
                    break;
            }
        }

        if (buf.length() > 0) {
            return buf.toString();
        }
        return null;
    }

    /**
     * Parses the a request line ( see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec5.html#sec5.1">HTTP
     * Request Line</a>) to an HttpRequest object. the HttpRequest object will not contain any request header fields.
     * 
     * @param requestLine
     *            a single line according to HTTP standard
     * @return a HttpRequest object with no fields
     * @throws URISyntaxException
     */
    private HttpRequest parseRequestLine(final String requestLine) throws HttpProtocolException {
        final Matcher matcher = HTTP.HTTP_REQUEST_LINE_PATTERN.matcher(requestLine);

        final HttpRequest httpRequest;
        if (matcher.matches() && matcher.groupCount() >= 3) {

            try {
                httpRequest = new HttpRequest(HttpCommands.valueOf(matcher.group(1)), new URI(matcher.group(2)),
                        HttpVersion.fromString(matcher.group(3)));
                httpRequest.setCharset(HTTP.CHARSET);
            } catch (final URISyntaxException e) {
                throw new HttpProtocolException("Resource identifier in " + e.getInput() + " was invalid at position "
                        + e.getIndex(), requestLine, e);
            }

        } else {
            throw new HttpProtocolException("RequestLine " + requestLine + " does not conform to http standard");
        }

        return httpRequest;
    }

    /**
     * see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html">Header Field Definitions</a>
     * 
     * @param fieldLine
     *            the string containing a line describing a field and its value
     * @return the request header field
     */
    private HttpHeaderField parseRequestHeaderField(final String fieldLine) throws HttpProtocolException {
        final Matcher matcher = HTTP.HTTP_REQUEST_FIELD_PATTERN.matcher(fieldLine);

        final HttpHeaderField field;

        if (matcher.matches() && matcher.groupCount() >= 2) {

            field = new HttpHeaderField(RequestHeaders.fromString(matcher.group(1)), matcher.group(2));

        } else {
            throw new HttpProtocolException("Field '" + fieldLine + "' does not conform to http standard");
        }

        return field;

    }

    @Override
    protected CharBuffer serializeHeader(final Header header) {
        return this.serializeHeader((HttpHeader) header);
    }

    /**
     * Serializes a response into a CharBuffer.
     * 
     * @param httpResponse
     *            the response to be serialized
     * @return a CharBuffer containing the response in character representation
     */
    private CharBuffer serializeHeader(final HttpHeader header) {
        final StringBuilder buf = new StringBuilder(128);
        buf.append(header.getVersion()).append(' ').append(header.getStatusCode()).append(HTTP.CRLF);
        for (final HeaderField field : header.getFields()) {
            buf.append(field).append(HTTP.CRLF);
        }
        buf.append(HTTP.CRLF);
        final char[] serializedResponse = new char[buf.length()];
        buf.getChars(0, buf.length(), serializedResponse, 0);
        LOG.debug("Serialized Response: \n{}", buf);
        return CharBuffer.wrap(serializedResponse);
    }

}
