package li.moskito.awtt.protocol.http;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class HttpChannelOptionsTest {

    @Test
    public void testType() throws Exception {
        assertEquals(Integer.class, HttpChannelOptions.KEEP_ALIVE_MAX_MESSAGES.type());
        assertEquals(Integer.class, HttpChannelOptions.KEEP_ALIVE_TIMEOUT.type());
    }

    @Test
    public void testName() throws Exception {
        assertEquals("maxMessagesPerConnection", HttpChannelOptions.KEEP_ALIVE_MAX_MESSAGES.name());
        assertEquals("keepAliveTimeout", HttpChannelOptions.KEEP_ALIVE_TIMEOUT.name());
    }

    @Test
    public void testToString() throws Exception {
        assertEquals("maxMessagesPerConnection", HttpChannelOptions.KEEP_ALIVE_MAX_MESSAGES.toString());
        assertEquals("keepAliveTimeout", HttpChannelOptions.KEEP_ALIVE_TIMEOUT.toString());
    }

    @Test
    public void testGetDefault() throws Exception {
        assertEquals(100, HttpChannelOptions.KEEP_ALIVE_MAX_MESSAGES.getDefault().intValue());
        assertEquals(5, HttpChannelOptions.KEEP_ALIVE_TIMEOUT.getDefault().intValue());
    }

    @Test
    public void testFromString() throws Exception {
        assertEquals(Integer.valueOf(100), HttpChannelOptions.KEEP_ALIVE_MAX_MESSAGES.fromString("100"));
        assertEquals(Integer.valueOf(5), HttpChannelOptions.KEEP_ALIVE_MAX_MESSAGES.fromString("5"));
    }

}
