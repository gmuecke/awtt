package li.moskito.awtt.protocol.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;

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
        final Protocol<?, ?, ?> p = this.httpChannel.getProtocol();
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

        final ByteBuffer in = StandardCharsets.ISO_8859_1.encode(rawMessage);
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
    public void testHttpChannel() throws Exception {
        final HTTP protocol = mock(HTTP.class);
        try (HttpChannel channel = new HttpChannel(protocol)) {
            assertEquals(protocol, channel.getProtocol());
        }

    }

}
