package li.moskito.awtt.protocol.http;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ResponseHeadersTest {

    @Test
    public void testToString() throws Exception {
        assertEquals("Accept-Ranges", ResponseHeaders.ACCEPT_RANGES.toString());
    }

    @Test
    public void testFromString() throws Exception {
        assertEquals(ResponseHeaders.CONNECTION, ResponseHeaders.fromString("Connection"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromString_noMatch() throws Exception {
        ResponseHeaders.fromString("Nothing");
    }

    @Test
    public void testGetName() throws Exception {
        assertEquals("Accept-Ranges", ResponseHeaders.ACCEPT_RANGES.getName());
    }

}
