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
import java.util.Collections;
import java.util.HashSet;
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
import li.moskito.awtt.protocol.MessageChannelOptions;
import li.moskito.awtt.protocol.Protocol;
import li.moskito.awtt.protocol.ProtocolException;
import li.moskito.awtt.protocol.http.HTTP.ResponseOptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.emory.mathcs.backport.java.util.concurrent.atomic.AtomicBoolean;
import edu.emory.mathcs.backport.java.util.concurrent.atomic.AtomicInteger;
import edu.emory.mathcs.backport.java.util.concurrent.atomic.AtomicLong;

/**
 * @author Gerald
 */
public class HttpChannel extends MessageChannel {

    /**
     * SLF4J Logger for this class
     */
    private static final Logger LOG = LoggerFactory.getLogger(HttpChannel.class);

    private final HTTP protocol;

    private final AtomicLong timeout = new AtomicLong();

    private final AtomicInteger numMessages = new AtomicInteger();

    private final AtomicBoolean closeOnEmptyOutputQueue = new AtomicBoolean(false);

    private final AtomicBoolean initialized = new AtomicBoolean(false);

    /**
     * Set of the options supported by HTTP
     */
    @SuppressWarnings("rawtypes")
    private static final Set<MessageChannelOption> SUPPORTED_OPTIONS;

    static {
        @SuppressWarnings("rawtypes")
        final Set<MessageChannelOption> set = new HashSet<>();
        set.add(MessageChannelOptions.KEEP_ALIVE_TIMEOUT);
        set.add(MessageChannelOptions.KEEP_ALIVE_MAX_MESSAGES);
        SUPPORTED_OPTIONS = Collections.unmodifiableSet(set);
    }

    /**
     * @param protocol
     */
    public HttpChannel(final HTTP protocol) {
        super();
        this.protocol = protocol;
        this.subscribe(ErrorEvents.PARSE_ERROR, new ChannelEventListener() {

            @Override
            public void onEvent(final Event<?> event) {
                HttpChannel.this.receiveIncomingMessage(HTTP.createResponse(HttpStatusCodes.BAD_REQUEST,
                        ResponseOptions.FORCE_CLOSE));
            }
        });

        this.subscribe(LifecycleEvents.OUTPUT_QUEUE_EMPTY, new ChannelEventListener() {
            @Override
            public void onEvent(final Event<?> event) {
                if (HttpChannel.this.closeOnEmptyOutputQueue.get()) {
                    try {
                        LOG.debug("Closing channel");
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
        return SUPPORTED_OPTIONS;
    }

    @Override
    protected HttpMessage parseMessage(final ByteBuffer src) throws ProtocolException, IOException {
        this.checkAndInitializeState();
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

        this.updateState(result);

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
        final HttpHeader httpHeader = (HttpHeader) header;

        this.closeOnEmptyOutputQueue.compareAndSet(false, this.protocol.isClosedByHeader(httpHeader));

        if (!this.closeOnEmptyOutputQueue.get()) {
            httpHeader.addFields(this.protocol.getKeepAliverHeaders(
                    this.getOption(MessageChannelOptions.KEEP_ALIVE_TIMEOUT),
                    this.getOption(MessageChannelOptions.KEEP_ALIVE_MAX_MESSAGES)));
        }

        return this.serializeHeader(httpHeader);
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
     * Check if the channel has been initialized and initialize it if not. The initialization is done be setting initial
     * timeout and message count with the values from the channel options (if not set, defaults are used).
     */
    private void checkAndInitializeState() {
        if (this.initialized.compareAndSet(false, true)) {
            this.timeout.set(this.getElapsedSeconds() + this.getOption(MessageChannelOptions.KEEP_ALIVE_TIMEOUT));
            this.numMessages.set(this.getOption(MessageChannelOptions.KEEP_ALIVE_MAX_MESSAGES));
        }
    }

    /**
     * Updates the internal state depending on the current request. The message count updated, the timeout will be
     * reset. The request may require to close the channel afterwards, or the message limit has been reached or the
     * timeout has been reached.
     * 
     * @param currentRequest
     */
    private void updateState(final HttpRequest currentRequest) {
        // first update the message count
        this.decreaseMessageCount();
        // determine whether to close the connection based on parameters of the request, the current (updated) message
        // count and the current (not yet updated) timeout counter
        this.updateKeepAliveState(currentRequest);
        // reset the timeout
        this.resetTimeout();
    }

    /**
     * Sets or Resets the message count before the connection terminates
     */
    private void decreaseMessageCount() {
        if (this.getOption(MessageChannelOptions.KEEP_ALIVE_MAX_MESSAGES) != -1) {
            this.numMessages.decrementAndGet();
        }
    }

    /**
     * Updates the time when the current connection times out.
     */
    private void resetTimeout() {
        if (this.getOption(MessageChannelOptions.KEEP_ALIVE_TIMEOUT) != -1) {
            this.timeout.set(this.getElapsedSeconds() + this.getOption(MessageChannelOptions.KEEP_ALIVE_TIMEOUT));
        }
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
    private void updateKeepAliveState(final Message request) {
        final boolean closeOnRequest = this.protocol.isClosedByRequest(request);
        final boolean timeoutReached = this.timeout.get() != -1 && this.timeout.get() < this.getElapsedSeconds();
        final boolean messageLimitReached = this.numMessages.get() <= 0;
        this.closeOnEmptyOutputQueue.set(closeOnRequest || timeoutReached || messageLimitReached);
    }

    /**
     * Returns a timer of seconds of the JVMs lifetime. This does not refer to a system time and is based on the
     * System.nanoTime()
     * 
     * @return
     */
    private long getElapsedSeconds() {
        return System.nanoTime() / 1_000_000_000;
    }

}
