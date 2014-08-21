package li.moskito.awtt.protocol.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;

import li.moskito.awtt.protocol.CustomHeaderFieldDefinition;
import li.moskito.awtt.protocol.HeaderField;
import li.moskito.awtt.protocol.HeaderFieldDefinition;
import li.moskito.awtt.protocol.Message;
import li.moskito.awtt.protocol.Protocol;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class HttpChannelTest {
    @Mock
    private HTTP protocol;
    @InjectMocks
    private HttpChannel httpChannel;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetProtocol() throws Exception {
        final Protocol p = this.httpChannel.getProtocol();
        assertTrue(p instanceof HTTP);
    }

    @Test
    public void testParseMessageByteBuffer_HTTPV11() throws Exception {

        //@formatter:off
        final String rawMessage = 
                "GET / HTTP/1.1\r\n" 
              + "Connection: keep-alive\r\n" 
              + "Host: localhost:80\r\n";
        // @formatter:on

        final ByteBuffer in = this.toByteBuffer(rawMessage);
        final HttpMessage message = this.httpChannel.parseMessage(in);

        assertNotNull(message);
        assertEquals(StandardCharsets.ISO_8859_1, message.getCharset());
        final HttpHeader header = message.getHeader();
        assertNotNull(header);
        assertEquals(HttpVersion.HTTP_1_1, header.getVersion());
        assertEquals(HttpCommands.GET, header.getCommand());
        assertTrue(header.hasField(RequestHeaders.CONNECTION));
        assertTrue(header.hasField(RequestHeaders.HOST));
        assertNull(message.getBody());

    }

    @Test(expected = HttpProtocolException.class)
    public void testParseMessageByteBuffer_invalidCommand() throws Exception {
        final ByteBuffer in = this.toByteBuffer("FIND /someBug HTTP/1.1\r\n\r\n");
        this.httpChannel.parseMessage(in);
    }

    @Test(expected = HttpProtocolException.class)
    public void testParseMessageByteBuffer_invalidVersion() throws Exception {
        final ByteBuffer in = this.toByteBuffer("GET /someBug HTTP/2.5\r\n\r\n");
        this.httpChannel.parseMessage(in);
    }

    @Test(expected = HttpProtocolException.class)
    public void testParseMessageByteBuffer_missingVersion() throws Exception {
        final ByteBuffer in = this.toByteBuffer("GET /someBug\r\n\r\n");
        this.httpChannel.parseMessage(in);
    }

    @Test(expected = HttpProtocolException.class)
    public void testParseMessageByteBuffer_invalidResource() throws Exception {
        final ByteBuffer in = this.toByteBuffer("GET " + 0xa + "://" + 0x9 + " HTTP/1.1\r\n\r\n");
        this.httpChannel.parseMessage(in);
    }

    @Test
    public void testParseMessageByteBuffer_allCommands() throws Exception {
        for (final HttpCommands command : HttpCommands.values()) {
            final ByteBuffer in = this.toByteBuffer(command + " /someFile HTTP/1.1");
            final HttpRequest httpRequest = (HttpRequest) this.httpChannel.parseMessage(in);
            this.assertHttpRequest(command, new URI("/someFile"), HttpVersion.HTTP_1_1, httpRequest);
        }
    }

    @Test
    public void testParseMessageByteBuffer_allVersions() throws Exception {
        for (final HttpVersion version : HttpVersion.values()) {
            final ByteBuffer in = this.toByteBuffer("GET /someFile " + version);
            final HttpRequest httpRequest = (HttpRequest) this.httpChannel.parseMessage(in);
            this.assertHttpRequest(HttpCommands.GET, new URI("/someFile"), version, httpRequest);
        }
    }

    @Test
    public void testParseMessageByteBuffer_allStandardFields() throws Exception {
        for (final RequestHeaders fieldName : RequestHeaders.values()) {
            final ByteBuffer in = this.toByteBuffer("GET /someFile HTTP/1.1\r\n" + fieldName + ": someValue");
            final HttpRequest httpRequest = (HttpRequest) this.httpChannel.parseMessage(in);
            final HeaderField field = httpRequest.getHeader().getField(fieldName);
            this.assertHeaderField(fieldName, "someValue", field);
        }
    }

    @Test
    public void testParseMessageByteBuffer_unknownHeaderField() throws Exception {
        final HeaderFieldDefinition fieldName = CustomHeaderFieldDefinition.forName("Cookie");

        final ByteBuffer in = this.toByteBuffer("GET /someFile HTTP/1.1\r\n" + fieldName + ": someValue");
        final HttpRequest httpRequest = (HttpRequest) this.httpChannel.parseMessage(in);
        final HeaderField field = httpRequest.getHeader().getField(fieldName);
        this.assertHeaderField(fieldName, "someValue", field);

    }

    @Test(expected = HttpProtocolException.class)
    public void testParseMessageByteBuffer_invalidHeader_noSeparator() throws Exception {
        final ByteBuffer in = this.toByteBuffer("GET /someFile HTTP/1.1\r\nCookie noSeparator");
        this.httpChannel.parseMessage(in);
    }

    @Test(expected = HttpProtocolException.class)
    public void testParseMessageByteBuffer_invalidHeader_invalidField() throws Exception {
        final ByteBuffer in = this.toByteBuffer("GET /someFile HTTP/1.1\r\nCookie");
        this.httpChannel.parseMessage(in);
    }

    @Test
    public void testParseMessageByteBuffer_emptyRequestLine() throws Exception {
        final ByteBuffer in = this.toByteBuffer("\n");
        assertNull(this.httpChannel.parseMessage(in));
    }

    @Test
    public void testParseMessageByteBuffer_emptyRequestHeaderFields() throws Exception {
        // does not conform to http header
        final ByteBuffer in = this.toByteBuffer("GET / HTTP/1.1\r\n \r\n");
        final HttpMessage message = this.httpChannel.parseMessage(in);
        assertNotNull(message);
        assertTrue(message.getHeader().getFields().isEmpty());
    }

    @Test
    public void testSerializeHeader_Header_Simple() throws Exception {

        final HttpHeader header = new HttpHeader(HttpStatusCodes.ACCEPTED);

        final CharBuffer serHeader = this.httpChannel.serializeHeader(header);
        assertNotNull(serHeader);

        //@formatter:off
        final String expectedHeader = 
                "HTTP/1.1 202 Accepted\r\n"
              + "\r\n";
        // @formatter:on
        assertEquals(expectedHeader, serHeader.toString());

    }

    @Test
    public void testSerializeHeader_Header_withFields() throws Exception {

        final HttpHeader header = new HttpHeader(HttpStatusCodes.ACCEPTED);
        header.addField(new HttpHeaderField(ResponseHeaders.CONNECTION, "keep-alive"));
        header.addField(new HttpHeaderField(CustomHeaderFieldDefinition.forName("Keep-Alive"), "timeout=5, max=100"));

        final CharBuffer serHeader = this.httpChannel.serializeHeader(header);
        assertNotNull(serHeader);

        //@formatter:off
        final String expectedHeader = 
                "HTTP/1.1 202 Accepted\r\n"
              + "Connection: keep-alive\r\n"
              + "Keep-Alive: timeout=5, max=100\r\n"
              + "\r\n";
        // @formatter:on
        assertEquals(expectedHeader, serHeader.toString());

    }

    @Test
    public void testHttpChannel() throws Exception {
        final HTTP protocol = mock(HTTP.class);
        try (HttpChannel channel = new HttpChannel(protocol)) {
            assertEquals(protocol, channel.getProtocol());
        }
    }

    @Test
    public void testProcessMessages_andCloseAfterwards() throws Exception {
        when(this.protocol.closeChannelOnCompletion(any(Message.class))).thenReturn(true);

        this.httpChannel.write(this.toByteBuffer("GET / HTTP/1.1"));
        this.httpChannel.processMessages();
        this.httpChannel.read(ByteBuffer.allocate(2014));

        assertFalse(this.httpChannel.isOpen());
    }

    @Test
    public void testProcessMessages_andRemainOpen() throws Exception {
        when(this.protocol.closeChannelOnCompletion(any(Message.class))).thenReturn(false);

        this.httpChannel.read(this.toByteBuffer("GET / HTTP/1.1"));
        this.httpChannel.processMessages();
        this.httpChannel.write(ByteBuffer.allocate(2014));

        assertTrue(this.httpChannel.isOpen());
    }

    private ByteBuffer toByteBuffer(final String rawMessage) {
        final ByteBuffer in = StandardCharsets.ISO_8859_1.encode(rawMessage);
        return in;
    }

    private void assertHttpRequest(final HttpCommands command, final URI resource, final HttpVersion version,
            final HttpRequest httpRequest) throws URISyntaxException {
        assertNotNull("Command " + command + " was parsed to NULL", httpRequest);
        assertEquals(command, httpRequest.getCommand());
        assertEquals(resource, httpRequest.getResource());
        assertEquals(version, httpRequest.getHeader().getVersion());

        // empty fields
        assertNotNull(httpRequest.getHeader().getFields());
        assertTrue(httpRequest.getHeader().getFields().isEmpty());
    }

    private void assertHeaderField(final HeaderFieldDefinition fieldName, final String value, final HeaderField field) {
        assertNotNull("Field " + fieldName + " was parsed to NULL", field);
        assertEquals(fieldName, field.getHeaderFieldDefinition());
        assertEquals(value, field.getValue());
    }

}
