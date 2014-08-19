package li.moskito.awtt.protocol.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class HttpHeaderTest {

    private URI resource;

    private HttpHeader requestHeader;
    private HttpHeader responseHeader;

    @Before
    public void setUp() throws Exception {
        this.resource = new URI("/testResource");
        this.requestHeader = new HttpHeader(HttpCommands.GET, this.resource);
        this.responseHeader = new HttpHeader(HttpStatusCodes.OK);
    }

    @Test
    public void testGetCommand() throws Exception {
        assertEquals(HttpCommands.GET, this.requestHeader.getCommand());
        assertNull(this.responseHeader.getCommand());
    }

    @Test
    public void testGetResource() throws Exception {
        assertEquals(new URI("/testResource"), this.requestHeader.getResource());
        assertNull(this.responseHeader.getResource());
    }

    @Test
    public void testGetStatusCode() throws Exception {
        assertEquals(HttpStatusCodes.OK, this.responseHeader.getStatusCode());
        assertNull(this.requestHeader.getStatusCode());
    }

    @Test
    public void testGetVersion_defaultVersion() throws Exception {
        assertEquals(HttpVersion.HTTP_1_1, this.requestHeader.getVersion());
        assertEquals(HttpVersion.HTTP_1_1, this.responseHeader.getVersion());
    }

    @Test
    public void testGetVersion_customVersion() throws Exception {
        assertEquals(HttpVersion.HTTP_1_0, new HttpHeader(HttpVersion.HTTP_1_0, HttpStatusCodes.NOT_FOUND).getVersion());
    }

    @Test
    public void testIsRequest() throws Exception {
        assertTrue(this.requestHeader.isRequest());
        assertFalse(this.responseHeader.isRequest());
    }

    @Test
    public void testIsResponse() throws Exception {
        assertFalse(this.requestHeader.isResponse());
        assertTrue(this.responseHeader.isResponse());
    }

    @Test
    public void testGetFields() throws Exception {
        assertNotNull(this.requestHeader.getFields());
        assertNotNull(this.responseHeader.getFields());
        assertTrue(this.requestHeader.getFields().isEmpty());
        assertTrue(this.responseHeader.getFields().isEmpty());
    }

    @Test
    public void testAddHttpHeaderFields() throws Exception {
        final List<HttpHeaderField> fields = new ArrayList<>();
        fields.add(new HttpHeaderField(RequestHeaders.CONNECTION, "aValue"));
        fields.add(new HttpHeaderField(RequestHeaders.HOST, "aHost"));
        this.requestHeader.addHttpHeaderFields(fields);

        assertEquals(2, this.requestHeader.getFields().size());

        assertEquals("aValue", this.requestHeader.getField(RequestHeaders.CONNECTION).getValue());
        assertEquals("aHost", this.requestHeader.getField(RequestHeaders.HOST).getValue());
    }

}
