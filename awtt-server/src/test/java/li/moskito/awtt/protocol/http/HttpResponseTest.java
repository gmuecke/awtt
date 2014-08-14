package li.moskito.awtt.protocol.http;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class HttpResponseTest {

    @Test
    public void testResponseVersionStatusCodes() throws Exception {
        final HttpResponse httpResponse = new HttpResponse(HttpVersion.HTTP_1_0, HttpStatusCodes.BAD_REQUEST);
        assertEquals(HttpVersion.HTTP_1_0, httpResponse.getVersion());
        assertEquals(HttpStatusCodes.BAD_REQUEST, httpResponse.getStatus());
    }

    @Test
    public void testResponseStatusCodes() throws Exception {
        final HttpResponse httpResponse = new HttpResponse(HttpStatusCodes.BAD_REQUEST);
        assertEquals(HttpVersion.HTTP_1_1, httpResponse.getVersion());
        assertEquals(HttpStatusCodes.BAD_REQUEST, httpResponse.getStatus());
    }

}
