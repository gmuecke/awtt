package li.moskito.awtt.server;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

public class ServerRuntimeExceptionTest {

    @Mock
    private Throwable cause;

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testServerRuntimeExceptionThrowable() throws Exception {
        final ServerRuntimeException e = new ServerRuntimeException(this.cause);
        assertEquals(this.cause, e.getCause());
    }

    @Test
    public void testServerRuntimeExceptionStringIOException() throws Exception {
        final ServerRuntimeException e = new ServerRuntimeException("aMessage", this.cause);
        assertEquals(this.cause, e.getCause());
        assertEquals("aMessage", e.getMessage());
    }

}
