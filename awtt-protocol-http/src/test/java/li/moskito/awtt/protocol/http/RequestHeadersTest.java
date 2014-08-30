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
        assertEquals(RequestHeaders.CONNECTION, RequestHeaders.fromString("Connection"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromString_noMatch() throws Exception {
        RequestHeaders.fromString("Nothing");
    }

    @Test
    public void testGetName() throws Exception {
        assertEquals("Accept", RequestHeaders.ACCEPT.getName());
    }

}
