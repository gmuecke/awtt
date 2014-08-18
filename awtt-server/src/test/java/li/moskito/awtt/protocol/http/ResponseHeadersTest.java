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
        assertEquals(ResponseHeaders.ACCEPT_RANGES, ResponseHeaders.fromString("Accept-Ranges"));
    }

    @Test
    public void testGetName() throws Exception {
        assertEquals("Accept-Ranges", ResponseHeaders.ACCEPT_RANGES.getName());
    }

}
