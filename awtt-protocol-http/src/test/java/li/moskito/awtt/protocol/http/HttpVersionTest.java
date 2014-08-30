package li.moskito.awtt.protocol.http;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class HttpVersionTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testGetVersion() throws Exception {
        assertEquals("0.9", HttpVersion.HTTP_0_9.getVersion());
        assertEquals("1.0", HttpVersion.HTTP_1_0.getVersion());
        assertEquals("1.1", HttpVersion.HTTP_1_1.getVersion());
    }

    @Test
    public void testToString() throws Exception {
        assertEquals("HTTP/0.9", HttpVersion.HTTP_0_9.toString());
        assertEquals("HTTP/1.0", HttpVersion.HTTP_1_0.toString());
        assertEquals("HTTP/1.1", HttpVersion.HTTP_1_1.toString());
    }

    @Test
    public void testFromString() throws Exception {
        assertEquals(HttpVersion.HTTP_1_1, HttpVersion.fromString("HTTP/1.1"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromString_invalidString() throws Exception {
        HttpVersion.fromString("FTP/1.1");
    }

}
