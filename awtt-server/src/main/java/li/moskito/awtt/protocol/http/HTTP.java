/**
 * 
 */
package li.moskito.awtt.protocol.http;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import li.moskito.awtt.util.Channels;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Util class for creating HTTP protocol objects
 * 
 * @author Gerald
 */
public final class HTTP {

    /**
     * SLF4J Logger for this class
     */
    private static final Logger LOG = LoggerFactory.getLogger(HTTP.class);

    private static final String CRLF = "\r\n";

    private HTTP() {}
    //@formatter:off
    private static final Pattern HTTP_REQUEST_LINE_PATTERN = 
                         Pattern.compile("^("+getCommandRegexGroup()+") (\\S+) ("+getVersionRegexGroup()+")(\\r\\n)?$");
    private static final Pattern HTTP_REQUEST_FIELD_PATTERN = 
                         Pattern.compile("^("+getRequestHeaderFieldRegexGroup()+"):\\s*(.*)(\\r\\n)?$");
    private static final Charset CHARSET = StandardCharsets.ISO_8859_1;
    // @formatter:on

    /**
     * Creates the regex group for HTTP Commands from the values of the enum
     * 
     * @return
     */
    private static final String getCommandRegexGroup() {
        final StringBuffer buf = new StringBuffer(50);

        for (final Commands command : Commands.values()) {
            if (buf.length() > 0) {
                buf.append('|');
            }
            buf.append(command.toString());
        }

        return buf.toString();
    }

    /**
     * Creates the regex group for HTTP Versions from the values of the enum
     * 
     * @return
     */
    private static final String getVersionRegexGroup() {
        final StringBuffer buf = new StringBuffer(50);

        buf.append(Version.PROTOCOL_PREFIX.replaceAll("\\/", "\\\\/"));
        buf.append('(');
        boolean oneVersionWritten = false;
        for (final Version version : Version.values()) {
            if (oneVersionWritten) {
                buf.append('|');
            }
            buf.append(version.getVersion().replaceAll("\\.", "\\\\."));
            oneVersionWritten = true;
        }
        buf.append(')');

        return buf.toString();
    }

    /**
     * Creates the regex group for HTTP RequestHeader fields from the values of the enum
     * 
     * @return
     */
    private static final String getRequestHeaderFieldRegexGroup() {
        final StringBuffer buf = new StringBuffer(128);

        for (final RequestHeaderFieldDefinitions command : RequestHeaderFieldDefinitions.values()) {
            if (buf.length() > 0) {
                buf.append('|');
            }
            buf.append(command.toString());
        }

        return buf.toString();
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
    private static String readLine(final CharBuffer charBuffer) throws HttpProtocolException {
        final StringBuffer buf = new StringBuffer(8);

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
     * Parses an entire request from the given String. The first line of the string must be a HTTP Request Line
     * 
     * @param request
     *            the entire request
     * @return the parsed request object
     * @throws URISyntaxException
     * @throws IOException
     */
    public static Request parseRequest(final ByteBuffer readBuffer) throws HttpProtocolException, IOException {
        return parseRequest(CHARSET.decode(readBuffer));
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
    public static Request parseRequest(final CharBuffer charBuffer) throws HttpProtocolException, IOException {
        // skip to end of line
        final String requestLine = readLine(charBuffer);
        if (requestLine == null) {
            return null;
        }
        final Request result = parseRequestLine(requestLine);

        final List<HeaderField<?>> fields = new ArrayList<>();
        for (String fieldLine = readLine(charBuffer); fieldLine != null;) {
            if (fieldLine.trim().isEmpty()) {
                break; // reached end of header
            }
            fields.add(parseRequestHeaderField(fieldLine));
            fieldLine = readLine(charBuffer);
        }
        result.addFields(fields);
        return result;
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
    public static Request parseRequestLine(final String requestLine) throws HttpProtocolException {
        final Matcher matcher = HTTP_REQUEST_LINE_PATTERN.matcher(requestLine);

        final Request request;
        if (matcher.matches() && matcher.groupCount() >= 3) {

            try {
                request = new Request(Commands.valueOf(matcher.group(1)), new URI(matcher.group(2)),
                        Version.fromString(matcher.group(3)));
            } catch (final URISyntaxException e) {
                throw new HttpProtocolException("Resource identifier in " + e.getInput() + " was invalid at position "
                        + e.getIndex(), requestLine, e);
            }

        } else {
            throw new HttpProtocolException("RequestLine " + requestLine + " does not conform to http standard");
        }

        return request;
    }

    /**
     * see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html">Header Field Definitions</a>
     * 
     * @param fieldLine
     *            the string containing a line describing a field and its value
     * @return the request header field
     */
    public static HeaderField<RequestHeaderFieldDefinitions> parseRequestHeaderField(final String fieldLine)
            throws HttpProtocolException {
        final Matcher matcher = HTTP_REQUEST_FIELD_PATTERN.matcher(fieldLine);

        final HeaderField<RequestHeaderFieldDefinitions> field;
        if (matcher.matches() && matcher.groupCount() >= 2) {

            field = new HeaderField<>(RequestHeaderFieldDefinitions.fromString(matcher.group(1)), matcher.group(2));

        } else {
            throw new HttpProtocolException("Field '" + fieldLine + "' does not conform to http standard");
        }

        return field;

    }

    /**
     * Serializes a response into a CharBuffer.
     * 
     * @param response
     *            the response to be serialized
     * @return a CharBuffer containing the response in character representation
     */
    public static CharBuffer serializeResponseHeader(final Response response) {
        final StringBuffer buf = new StringBuffer(128);
        buf.append(response.getVersion()).append(' ').append(response.getStatus()).append(CRLF);
        for (final HeaderField<?> field : response.getFields()) {
            buf.append(field).append(CRLF);
        }
        buf.append(CRLF);
        final char[] serializedResponse = new char[buf.length()];
        buf.getChars(0, buf.length(), serializedResponse, 0);
        LOG.debug("Serialized Response: {}", buf);
        return CharBuffer.wrap(serializedResponse);
    }

    /**
     * Sends a response header over the socket channel
     * 
     * @param response
     *            the response to send
     * @param channel
     *            the channel to send the response
     * @throws IOException
     */
    public static void sendResponseHeader(final Response response, final SocketChannel channel) throws IOException {
        final CharBuffer output = serializeResponseHeader(response);
        channel.write(CHARSET.encode(output));
    }

    /**
     * Sends an entity - the body of a http message - over the socket channel
     * 
     * @param entity
     *            the entity to be streamed to the channel
     * @param outputChannel
     *            the output channel to which the content of the entity is to be streamed
     * @throws IOException
     */
    public static void sendEntity(final Entity entity, final SocketChannel outputChannel) throws IOException {
        final ReadableByteChannel inputChannel = entity.getByteChannel();

        Channels.stream(inputChannel, outputChannel);

    }

    /**
     * Sends a response to the given socket channel
     * 
     * @param response
     * @param channel
     * @return
     * @throws IOException
     */
    public static void sendResponse(final Response response, final SocketChannel channel) throws IOException {
        sendResponseHeader(response, channel);

        final Entity entity = response.getEntity();
        if (entity != null) {
            sendEntity(entity, channel);
        }
    }

    /**
     * Creates a Response with the given StatusCode
     * 
     * @param statusCode
     *            the status code for the response
     * @return
     */
    public static Response createResponse(final StatusCodes statusCode) {
        return new Response(statusCode);
    }

    /**
     * The method checks the request for a keep alive information. If the client sends a 'close' token or has no
     * keep-alive token and is of HTTP/1.0 version, the method returns 0. Otherwise it returns 1 for HTTP/1.1
     * connections and HTTP/1.0 connections with keep alive token.
     * 
     * @param request
     *            the request to be checked
     * @return <code>true</code> if the connection has to be kept alive, <code>false</code> if not
     */
    public static boolean isKeepAlive(final Request request) {
        final String connectionField;
        if (request.hasField(RequestHeaderFieldDefinitions.CONNECTION)) {
            connectionField = request.getField(RequestHeaderFieldDefinitions.CONNECTION).getValue();
        } else {
            connectionField = null;
        }
        switch (request.getVersion()) {
            case HTTP_1_0:
                return "keep-alive".equals(connectionField);
            case HTTP_1_1:
                return !"close".equals(connectionField);
            default:
                return true;
        }
    }

}
