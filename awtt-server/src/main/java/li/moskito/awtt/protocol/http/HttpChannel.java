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
import java.util.Set;
import java.util.regex.Matcher;

import li.moskito.awtt.protocol.ChannelEventListener;
import li.moskito.awtt.protocol.CustomHeaderFieldDefinition;
import li.moskito.awtt.protocol.Event;
import li.moskito.awtt.protocol.Header;
import li.moskito.awtt.protocol.HeaderField;
import li.moskito.awtt.protocol.Message;
import li.moskito.awtt.protocol.MessageChannel;
import li.moskito.awtt.protocol.MessageChannelOption;
import li.moskito.awtt.protocol.Protocol;
import li.moskito.awtt.protocol.ProtocolException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.emory.mathcs.backport.java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Gerald
 */
public class HttpChannel extends MessageChannel {

    /**
     * SLF4J Logger for this class
     */
    private static final Logger LOG = LoggerFactory.getLogger(HttpChannel.class);

    private final HTTP protocol;

    private long timeout;

    private int numMessages;

    private final AtomicBoolean closeOnEmptyOutputQueue = new AtomicBoolean(false);

    /**
     * @param protocol
     */
    public HttpChannel(final HTTP protocol) {
        super();
        this.protocol = protocol;
        this.timeout = System.currentTimeMillis() + HttpChannelOptions.KEEP_ALIVE_TIMEOUT.getDefault();
        this.numMessages = HttpChannelOptions.KEEP_ALIVE_MAX_MESSAGES.getDefault();

        this.subscribe(ErrorEvents.PARSE_ERROR, new ChannelEventListener() {

            @Override
            public void onEvent(final Event<?> event) {
                HttpChannel.this.receiveIncomingMessage(HTTP.createResponse(HttpStatusCodes.BAD_REQUEST));
            }
        });

        this.subscribe(LifecycleEvents.OUTPUT_QUEUE_EMPTY, new ChannelEventListener() {
            @Override
            public void onEvent(final Event<?> event) {
                if (HttpChannel.this.closeOnEmptyOutputQueue.get()) {
                    try {
                        HttpChannel.this.close();
                    } catch (final IOException e) {
                        LOG.warn("Error during closing of channel", e);
                    }
                }
            }
        });
    }

    @Override
    public Protocol getProtocol() {
        return this.protocol;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Set<MessageChannelOption> getSupportedOptions() {
        return HttpChannelOptions.SUPPORTED_OPTIONS;
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
        final List<HttpHeaderField> fields = this.parseFields(charBuffer);
        result.getHeader().addHttpHeaderFields(fields);

        if (!this.isKeepAlive(result)) {
            this.closeOnEmptyOutputQueue.set(true);
        }
        this.updateTimeout();
        this.updateMessageCount();

        return result;
    }

    /**
     * Parses the header fields from the charbuffer and returns them in a list
     * 
     * @param charBuffer
     *            the charBuffer to read the fields from. The position of the buffer is on the first character of the
     *            first field.
     * @return the list of parsed header fields
     * @throws HttpProtocolException
     */
    private List<HttpHeaderField> parseFields(final CharBuffer charBuffer) throws HttpProtocolException {
        final List<HttpHeaderField> fields = new ArrayList<>();

        String fieldLine = this.readLine(charBuffer);
        while (fieldLine != null) {
            if (fieldLine.trim().isEmpty()) {
                break;
            }
            fields.add(this.parseRequestHeaderField(fieldLine));
            fieldLine = this.readLine(charBuffer);
        }
        return fields;
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
        if (matcher.matches()) {

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
        Matcher matcher = HTTP.HTTP_REQUEST_FIELD_PATTERN.matcher(fieldLine);
        if (matcher.matches()) {
            return new HttpHeaderField(RequestHeaders.fromString(matcher.group(1)), matcher.group(2));
        }
        matcher = HTTP.HTTP_CUSTOM_FIELD_PATTERN.matcher(fieldLine);
        if (matcher.matches()) {
            return new HttpHeaderField(CustomHeaderFieldDefinition.forName(matcher.group(1)), matcher.group(2));
        }
        throw new HttpProtocolException("Field '" + fieldLine + "' does not conform to http standard");
    }

    @Override
    protected CharBuffer serializeHeader(final Header header) {

        //@formatter:off
        if(!this.closeOnEmptyOutputQueue.get()) {
            header.addFields(this.protocol.getKeepAliverHeaders(
                    this.getOption(HttpChannelOptions.KEEP_ALIVE_TIMEOUT),
                    this.getOption(HttpChannelOptions.KEEP_ALIVE_MAX_MESSAGES)));
        }
        //@formatter:on

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

    /**
     * Sets or Resets the message count before the connection terminates
     */
    private void updateMessageCount() {
        if (this.getOption(HttpChannelOptions.KEEP_ALIVE_MAX_MESSAGES) != -1) {
            if (this.numMessages > 0) {
                this.numMessages--;
            } else {
                this.numMessages = HttpChannelOptions.KEEP_ALIVE_MAX_MESSAGES.getDefault();
            }
        }
    }

    /**
     * Updates the time when the current connection times out.
     */
    private void updateTimeout() {
        this.timeout = System.currentTimeMillis() + this.getOption(HttpChannelOptions.KEEP_ALIVE_TIMEOUT) * 1000;
    }

    /**
     * Determines if the connection should be kept alive after the processing of a message or not.
     * 
     * @param protocol
     *            the protocol that is used to interpret the requestor information in the request
     * @param request
     *            the request containing possibly information from the information how to handle the connection
     * @return <code>true</code> if the connection should be kept alive
     */
    private boolean isKeepAlive(final Message request) {
        final boolean keepAlive = !this.protocol.closeChannelOnCompletion(request);
        final boolean timeoutReached = this.timeout < System.currentTimeMillis();
        final boolean messageLimitReached = this.numMessages == 0;
        return keepAlive && !timeoutReached && !messageLimitReached;
    }

}
