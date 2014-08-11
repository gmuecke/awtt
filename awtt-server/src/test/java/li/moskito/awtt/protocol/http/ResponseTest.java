package li.moskito.awtt.protocol.http;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ResponseTest {

    @Test
    public void testResponseVersionStatusCodes() throws Exception {
        final Response response = new Response(Version.HTTP_1_0, StatusCodes.BAD_REQUEST);
        assertEquals(Version.HTTP_1_0, response.getVersion());
        assertEquals(StatusCodes.BAD_REQUEST, response.getStatus());
    }

    @Test
    public void testResponseStatusCodes() throws Exception {
        final Response response = new Response(StatusCodes.BAD_REQUEST);
        assertEquals(Version.HTTP_1_1, response.getVersion());
        assertEquals(StatusCodes.BAD_REQUEST, response.getStatus());
    }

}
