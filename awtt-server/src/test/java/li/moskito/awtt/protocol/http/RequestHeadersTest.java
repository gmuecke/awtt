package li.moskito.awtt.protocol.http;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class RequestHeadersTest {

    @Test
    public void testToString() throws Exception {
        assertEquals("Accept", RequestHeaders.ACCEPT.toString());
    }

    @Test
    public void testFromString() throws Exception {
        assertEquals(RequestHeaders.ACCEPT, RequestHeaders.fromString("Accept"));
    }

    @Test
    public void testGetName() throws Exception {
        assertEquals("Accept", RequestHeaders.ACCEPT.getName());
    }

}
