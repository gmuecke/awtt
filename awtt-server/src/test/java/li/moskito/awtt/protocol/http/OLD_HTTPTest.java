package li.moskito.awtt.protocol.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.Test;

public class OLD_HTTPTest {

    private void assertHttpRequest(final HttpCommands command, final URI resource, final HttpVersion version,
            final HttpRequest httpRequest) throws URISyntaxException {
        assertNotNull("Command " + command + " was parsed to NULL", httpRequest);
        assertEquals(command, httpRequest.getCommand());
        assertEquals(resource, httpRequest.getResource());
        assertEquals(version, httpRequest.getVersion());

        // empty fields
        assertNotNull(httpRequest.getFields());
        assertTrue(httpRequest.getFields().isEmpty());
    }

    private void assertHttpRequestField(final RequestHeaders fieldName, final String value,
            final HttpHeaderField field) {
        assertNotNull("Field " + fieldName + " was parsed to NULL", field);
        assertEquals(fieldName, field.getFieldName());
        assertEquals(value, field.getValue());
    }

    @Test(expected = HttpProtocolException.class)
    public void testParseRequestLine_invalidCommand() throws Exception {
        HTTP.parseRequestLine("FIND /someBug HTTP/1.1");
    }

    @Test(expected = HttpProtocolException.class)
    public void testParseRequestLine_invalidVersion() throws Exception {
        HTTP.parseRequestLine("GET /someBug HTTP/2.0");
    }

    @Test(expected = HttpProtocolException.class)
    public void testParseRequestLine_missingVersion() throws Exception {
        HTTP.parseRequestLine("FIND /someBug");
    }

    @Test(expected = HttpProtocolException.class)
    public void testParseRequestLine_invalidResource() throws Exception {
        HTTP.parseRequestLine("GET " + 0xa + "://" + 0x9 + " HTTP/1.1");
    }

    @Test
    public void testParseRequestLine_Commands() throws Exception {
        for (final HttpCommands command : HttpCommands.values()) {
            final HttpRequest httpRequest = HTTP.parseRequestLine(command + " /someFile HTTP/1.1");
            this.assertHttpRequest(command, new URI("/someFile"), HttpVersion.HTTP_1_1, httpRequest);
        }
    }

    @Test
    public void testParseHeaderFields() throws Exception {
        for (final RequestHeaders fieldName : RequestHeaders.values()) {
            final HttpHeaderField<RequestHeaders> field = HTTP.parseRequestHeaderField(fieldName
                    + ": someValue");
            this.assertHttpRequestField(fieldName, "someValue", field);
        }

    }

    @Test
    public void testParseRequest_String() throws Exception {
        //@formatter:off
        final String requestString = 
          "GET /some/example HTTP/1.1\r\n"
            +"Accept: image/gif\r\n"
            +"Accept-Language: en-us\r\n"
            +"Accept-Encoding: gzip\r\n"
            +"User-Agent: Mozilla/4.0\r\n"
            +"Host: some.example.com\r\n";
        // @formatter:on
        final HttpRequest httpRequest = HTTP.parseRequest(StandardCharsets.ISO_8859_1.encode(requestString));
        assertNotNull(httpRequest);
        assertEquals(HttpCommands.GET, httpRequest.getCommand());
        assertEquals(new URI("/some/example"), httpRequest.getResource());
        assertEquals(HttpVersion.HTTP_1_1, httpRequest.getVersion());

        final List<HttpHeaderField<?>> fields = httpRequest.getFields();
        assertNotNull(fields);
        assertFalse(fields.isEmpty());

        this.assertHttpRequestField(RequestHeaders.ACCEPT, "image/gif", fields.get(0));
        this.assertHttpRequestField(RequestHeaders.ACCEPT_ENCODING, "gzip", fields.get(1));
        this.assertHttpRequestField(RequestHeaders.ACCEPT_LANGUAGE, "en-us", fields.get(2));
        this.assertHttpRequestField(RequestHeaders.HOST, "some.example.com", fields.get(3));
        this.assertHttpRequestField(RequestHeaders.USER_AGENT, "Mozilla/4.0", fields.get(4));

    }

    @Test
    public void testParseRequest_CharBuffer() throws Exception {
        //@formatter:off
        final String requestString = 
          "GET /some/example HTTP/1.1\r\n"
            +"Accept: image/gif\r\n"
            +"Accept-Language: en-us\r\n"
            +"Accept-Encoding: gzip\r\n"
            +"User-Agent: Mozilla/4.0\r\n"
            +"Host: some.example.com\r\n";
        // @formatter:on
        final CharBuffer cbuf = CharBuffer.wrap(requestString);

        final HttpRequest httpRequest = HTTP.parseRequest(cbuf);

        assertNotNull(httpRequest);
        assertEquals(HttpCommands.GET, httpRequest.getCommand());
        assertEquals(new URI("/some/example"), httpRequest.getResource());
        assertEquals(HttpVersion.HTTP_1_1, httpRequest.getVersion());

        final List<HttpHeaderField<?>> fields = httpRequest.getFields();
        assertNotNull(fields);
        assertFalse(fields.isEmpty());

        this.assertHttpRequestField(RequestHeaders.ACCEPT, "image/gif", fields.get(0));
        this.assertHttpRequestField(RequestHeaders.ACCEPT_ENCODING, "gzip", fields.get(1));
        this.assertHttpRequestField(RequestHeaders.ACCEPT_LANGUAGE, "en-us", fields.get(2));
        this.assertHttpRequestField(RequestHeaders.HOST, "some.example.com", fields.get(3));
        this.assertHttpRequestField(RequestHeaders.USER_AGENT, "Mozilla/4.0", fields.get(4));

    }

    @Test
    public void testSerializeResponseHeader() throws Exception {
        final HttpResponse httpResponse = new HttpResponse(HttpVersion.HTTP_1_1, HttpStatusCodes.OK);
        httpResponse.addField(ResponseHeaders.CONTENT_LENGTH, "200");

        final CharBuffer buf = HTTP.serializeHeader(httpResponse);
        assertNotNull(buf);
        final String message = buf.toString();
        assertEquals("HTTP/1.1 200 OK\r\nContent-Length: 200\r\n\r\n", message);
    }

}
