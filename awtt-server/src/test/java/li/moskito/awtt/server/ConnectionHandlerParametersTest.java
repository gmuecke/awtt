package li.moskito.awtt.server;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class ConnectionHandlerParametersTest {

    private ConnectionHandlerParameters subject;

    @Before
    public void setUp() throws Exception {
        this.subject = new ConnectionHandlerParameters(5, 10, 20);
    }

    @Test
    public void testGetMaxConnections() throws Exception {
        assertEquals(5, this.subject.getMaxConnections());
    }

    @Test
    public void testGetKeepAliveTimeout() throws Exception {
        assertEquals(10, this.subject.getKeepAliveTimeout());
    }

    @Test
    public void testGetMaxMessagesPerConnection() throws Exception {
        assertEquals(20, this.subject.getMaxMessagesPerConnection());
    }

}
