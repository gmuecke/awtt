package li.moskito.awtt.protocol.http;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class HttpStatusCodesTest {

    @Test
    public void testGetReason() throws Exception {
        assertEquals("Accepted", HttpStatusCodes.ACCEPTED.getReason());
    }

    @Test
    public void testGetCode() throws Exception {
        assertEquals(202, HttpStatusCodes.ACCEPTED.getCode());
    }

    @Test
    public void testToString() throws Exception {
        assertEquals("202 Accepted", HttpStatusCodes.ACCEPTED.toString());
    }

}
