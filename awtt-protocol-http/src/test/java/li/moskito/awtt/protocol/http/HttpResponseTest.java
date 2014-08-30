package li.moskito.awtt.protocol.http;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class HttpResponseTest {

    private HttpStatusCodes status;

    private HttpVersion version;
    private HttpResponse httpResponse;

    @Before
    public void setUp() throws Exception {
        this.status = HttpStatusCodes.ACCEPTED;
        this.version = HttpVersion.HTTP_1_0;
        this.httpResponse = new HttpResponse(this.version, this.status);
    }

    @Test
    public void testHttpResponseHttpVersionHttpStatusCodes() throws Exception {
        final HttpResponse response = new HttpResponse(HttpVersion.HTTP_0_9, HttpStatusCodes.BAD_GATEWAY);
        assertEquals(HttpVersion.HTTP_0_9, response.getHeader().getVersion());
        assertEquals(HttpStatusCodes.BAD_GATEWAY, response.getHeader().getStatusCode());
    }

    @Test
    public void testHttpResponseHttpStatusCodes_DefaultVersion() throws Exception {
        final HttpResponse response = new HttpResponse(HttpStatusCodes.BAD_GATEWAY);
        assertEquals(HttpVersion.HTTP_1_1, response.getHeader().getVersion());
        assertEquals(HttpStatusCodes.BAD_GATEWAY, response.getHeader().getStatusCode());
    }

    @Test
    public void testGetStatusCode() throws Exception {
        assertEquals(this.status, this.httpResponse.getStatusCode());
    }

    @Test
    public void testToString() throws Exception {
        final String strResponse = "HTTP/1.0 202 Accepted\r\n\r\n";
        assertEquals(strResponse, this.httpResponse.toString());
    }

    @Test
    public void testToString_WithHeaders() throws Exception {
        final String strResponse = "HTTP/1.0 202 Accepted\r\nConnection: Keep-Alive\r\n\r\n";
        this.httpResponse.addField(ResponseHeaders.CONNECTION, "Keep-Alive");
        assertEquals(strResponse, this.httpResponse.toString());
    }

}
