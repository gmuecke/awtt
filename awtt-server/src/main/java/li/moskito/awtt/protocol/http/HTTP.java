/**
 * 
 */
package li.moskito.awtt.protocol.http;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;

import li.moskito.awtt.common.Configurable;
import li.moskito.awtt.protocol.CustomHeaderFieldDefinition;
import li.moskito.awtt.protocol.HeaderField;
import li.moskito.awtt.protocol.Message;
import li.moskito.awtt.protocol.Protocol;
import li.moskito.awtt.protocol.ProtocolRegistry;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The HTTP Protocol implementation
 * 
 * @author Gerald
 */
public class HTTP implements Protocol, Configurable {

    /**
     * SLF4J Logger for this class
     */
    private static final Logger LOG = LoggerFactory.getLogger(HTTP.class);

    public static final String CRLF = "\r\n";

    //@formatter:off
    public static final Pattern HTTP_REQUEST_LINE_PATTERN = 
                         Pattern.compile("^("+getCommandRegexGroup()+") (\\S+) ("+getVersionRegexGroup()+")(\\r\\n)?$");
    public static final Pattern HTTP_REQUEST_FIELD_PATTERN = 
                         Pattern.compile("^("+getRequestHeaderFieldRegexGroup()+"):\\s*(.*)(\\r\\n)?$");
    public static final Pattern HTTP_CUSTOM_FIELD_PATTERN = 
            Pattern.compile("^([A-Z][a-zA-Z\\-]+):\\s*(.*)(\\r\\n)?$");
    public static final Charset CHARSET = StandardCharsets.ISO_8859_1;

    public static final int HTTP_DEFAULT_PORT = 80;
    
    public static final String HTTP_DATE_FORMAT = "EEE, dd MMM yyy HH:mm:ss zzz";

    // Thread safe date formatter
    private static final ThreadLocal<SimpleDateFormat> HTTP_DATE_FORMATTER = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            final SimpleDateFormat sdf = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.ENGLISH);
            sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
            return sdf;
        }
    };

    // @formatter:on

    /**
     * Handlers to process messages of the protocol
     */
    private final List<HttpProtocolHandler> handlers;

    /**
     * Creates a new protocol instance. Its recommended to use the {@link ProtocolRegistry} instead.
     */
    public HTTP() {
        this.handlers = new CopyOnWriteArrayList<>();
    }

    @Override
    public Message process(final Message message) {
        if (message instanceof HttpRequest) {
            return this.process((HttpRequest) message);
        } else if (message instanceof HttpResponse) {
            return this.process((HttpResponse) message);
        }
        return createResponse(HttpStatusCodes.BAD_REQUEST);
    }

    public HttpResponse process(final HttpResponse message) {
        return message;
    }

    public HttpResponse process(final HttpRequest message) {
        LOG.debug("Processing Request\n{}", message);
        for (final HttpProtocolHandler handler : this.handlers) {
            if (handler.accepts(message)) {
                return handler.process(message);
            }
        }
        return createResponse(HttpStatusCodes.NOT_IMPLEMENTED);
    }

    @Override
    public HttpChannel openChannel() {
        return new HttpChannel(this);
    }

    @Override
    public int getDefaultPort() {
        return HTTP_DEFAULT_PORT;
    }

    @Override
    public void configure(final HierarchicalConfiguration config) throws ConfigurationException {
        final List<HierarchicalConfiguration> handlerConfigs = config.configurationsAt("handler");
        for (final HierarchicalConfiguration handlerConfig : handlerConfigs) {
            this.handlers.add(this.createHandler(handlerConfig));

        }

    }

    /**
     * Creates a handler from the configuration. If the handler is configurable, its configured as well using this
     * configuration.
     * 
     * @param handlerConfig
     *            the handler configuration
     * @return the handler instance created for this configuration.
     * @throws ConfigurationException
     */
    private HttpProtocolHandler createHandler(final HierarchicalConfiguration handlerConfig)
            throws ConfigurationException {
        try {

            final HttpProtocolHandler handler = (HttpProtocolHandler) Class.forName(handlerConfig.getString("@class"))
                    .newInstance();
            if (handler instanceof Configurable) {
                ((Configurable) handler).configure(handlerConfig);
            }
            return handler;
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            throw new ConfigurationException("Could not create protocol handler", e);
        }
    }

    /**
     * Formats a given {@link Date} into a HTTP conform date string.
     * 
     * @param date
     *            the date to be parsed
     * @return the formatted date
     */
    public static String toHttpDate(final Date date) {
        return HTTP_DATE_FORMATTER.get().format(date);
    }

    /**
     * Parses the given string that is supposed to be in http date format into a {@link Date}
     * 
     * @param date
     *            the date string to be parsed
     * @return the parsed date
     * @throws ParseException
     */
    public static Date fromHttpDate(final String date) throws ParseException {
        return HTTP_DATE_FORMATTER.get().parse(date);
    }

    /**
     * Convenience method to create a {@link HttpResponse} with the given {@link HttpStatusCodes}
     * 
     * @param statusCode
     *            the status code for the response
     * @return
     */
    public static HttpResponse createResponse(final HttpStatusCodes statusCode) {
        return new HttpResponse(statusCode);
    }

    /**
     * The method checks the request for a keep alive information. If the client sends a 'close' token or has no
     * keep-alive token and is of HTTP/1.0 version, the method returns 0. Otherwise it returns 1 for HTTP/1.1
     * connections and HTTP/1.0 connections with keep alive token.
     * 
     * @param httpRequest
     *            the request to be checked
     * @return <code>false</code> if the connection has to be kept alive, <code>true</code> if not
     */
    boolean closeChannelOnCompletion(final Message request) {
        final String connectionField;
        final HttpHeader header = (HttpHeader) request.getHeader();
        if (header.hasField(RequestHeaders.CONNECTION)) {
            connectionField = (String) header.getField(RequestHeaders.CONNECTION).getValue();
        } else {
            connectionField = null;
        }
        switch (header.getVersion()) {
            case HTTP_1_0:
                return !"keep-alive".equals(connectionField);
            case HTTP_1_1:
                return "close".equals(connectionField);
            default:
                return false;
        }
    }

    /**
     * Creates keep alive header information using the specified parameters
     * 
     * @param connectionControl
     * @return a list of header fields that can be added to a response in order to inform the receiver on how to handle
     *         the connection
     */
    List<HeaderField> getKeepAliverHeaders(final int timeout, final int maxConnections) {

        final List<HeaderField> keepAliveHeader = new ArrayList<>();

        //@formatter:off
        keepAliveHeader.add(new HttpHeaderField(ResponseHeaders.CONNECTION, "Keep-Alive"));
        keepAliveHeader.add(new HttpHeaderField(CustomHeaderFieldDefinition.forName("Keep-Alive"), 
                String.format("timeout=%s, max=%s", timeout, maxConnections)));
        // @formatter:on
        return keepAliveHeader;
    }

    /**
     * Creates the regex group for HTTP Commands from the values of the enum
     * 
     * @return
     */
    private static final String getCommandRegexGroup() {
        final StringBuilder buf = new StringBuilder(50);

        for (final HttpCommands command : HttpCommands.values()) {
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
        final StringBuilder buf = new StringBuilder(50);

        buf.append(HttpVersion.PROTOCOL_PREFIX.replaceAll("\\/", "\\\\/"));
        buf.append('(');
        boolean oneVersionWritten = false;
        for (final HttpVersion version : HttpVersion.values()) {
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
        final StringBuilder buf = new StringBuilder(128);

        for (final RequestHeaders command : RequestHeaders.values()) {
            if (buf.length() > 0) {
                buf.append('|');
            }
            buf.append(command.toString());
        }

        return buf.toString();
    }

}
