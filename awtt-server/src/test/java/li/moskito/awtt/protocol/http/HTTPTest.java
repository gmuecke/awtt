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

public class HTTPTest {

    private void assertHttpRequest(final Commands command, final URI resource, final Version version,
            final Request request) throws URISyntaxException {
        assertNotNull("Command " + command + " was parsed to NULL", request);
        assertEquals(command, request.getCommand());
        assertEquals(resource, request.getResource());
        assertEquals(version, request.getVersion());

        // empty fields
        assertNotNull(request.getFields());
        assertTrue(request.getFields().isEmpty());
    }

    private void assertHttpRequestField(final RequestHeaderFieldDefinitions fieldName, final String value,
            final HeaderField field) {
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
        for (final Commands command : Commands.values()) {
            final Request request = HTTP.parseRequestLine(command + " /someFile HTTP/1.1");
            this.assertHttpRequest(command, new URI("/someFile"), Version.HTTP_1_1, request);
        }
    }

    @Test
    public void testParseHeaderFields() throws Exception {
        for (final RequestHeaderFieldDefinitions fieldName : RequestHeaderFieldDefinitions.values()) {
            final HeaderField<RequestHeaderFieldDefinitions> field = HTTP.parseRequestHeaderField(fieldName
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
        final Request request = HTTP.parseRequest(StandardCharsets.ISO_8859_1.encode(requestString));
        assertNotNull(request);
        assertEquals(Commands.GET, request.getCommand());
        assertEquals(new URI("/some/example"), request.getResource());
        assertEquals(Version.HTTP_1_1, request.getVersion());

        final List<HeaderField<?>> fields = request.getFields();
        assertNotNull(fields);
        assertFalse(fields.isEmpty());

        this.assertHttpRequestField(RequestHeaderFieldDefinitions.ACCEPT, "image/gif", fields.get(0));
        this.assertHttpRequestField(RequestHeaderFieldDefinitions.ACCEPT_ENCODING, "gzip", fields.get(1));
        this.assertHttpRequestField(RequestHeaderFieldDefinitions.ACCEPT_LANGUAGE, "en-us", fields.get(2));
        this.assertHttpRequestField(RequestHeaderFieldDefinitions.HOST, "some.example.com", fields.get(3));
        this.assertHttpRequestField(RequestHeaderFieldDefinitions.USER_AGENT, "Mozilla/4.0", fields.get(4));

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

        final Request request = HTTP.parseRequest(cbuf);

        assertNotNull(request);
        assertEquals(Commands.GET, request.getCommand());
        assertEquals(new URI("/some/example"), request.getResource());
        assertEquals(Version.HTTP_1_1, request.getVersion());

        final List<HeaderField<?>> fields = request.getFields();
        assertNotNull(fields);
        assertFalse(fields.isEmpty());

        this.assertHttpRequestField(RequestHeaderFieldDefinitions.ACCEPT, "image/gif", fields.get(0));
        this.assertHttpRequestField(RequestHeaderFieldDefinitions.ACCEPT_ENCODING, "gzip", fields.get(1));
        this.assertHttpRequestField(RequestHeaderFieldDefinitions.ACCEPT_LANGUAGE, "en-us", fields.get(2));
        this.assertHttpRequestField(RequestHeaderFieldDefinitions.HOST, "some.example.com", fields.get(3));
        this.assertHttpRequestField(RequestHeaderFieldDefinitions.USER_AGENT, "Mozilla/4.0", fields.get(4));

    }

    @Test
    public void testSerializeResponseHeader() throws Exception {
        final Response response = new Response(Version.HTTP_1_1, StatusCodes.OK);
        response.addField(ResponseHeaderFieldDefinitions.CONTENT_LENGTH, "200");

        final CharBuffer buf = HTTP.serializeResponseHeader(response);
        assertNotNull(buf);
        final String message = buf.toString();
        assertEquals("HTTP/1.1 200 OK\r\nContent-Length: 200\r\n\r\n", message);
    }

}
