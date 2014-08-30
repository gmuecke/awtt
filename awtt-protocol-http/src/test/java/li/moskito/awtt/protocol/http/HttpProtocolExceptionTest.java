package li.moskito.awtt.protocol.http;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class HttpProtocolExceptionTest {
    @Mock
    private Throwable cause;

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testHttpProtocolExceptionStringStringThrowable() throws Exception {
        final HttpProtocolException x = new HttpProtocolException("aMessage", "aInput", this.cause);
        assertEquals(this.cause, x.getCause());
        assertEquals("aMessage", x.getMessage());
        assertEquals("aInput", x.getOriginalInput());
    }

    @Test
    public void testHttpProtocolExceptionStringThrowable() throws Exception {
        final HttpProtocolException x = new HttpProtocolException("aMessage", this.cause);
        assertEquals(this.cause, x.getCause());
        assertEquals("aMessage", x.getMessage());
    }

    @Test
    public void testHttpProtocolExceptionString() throws Exception {
        final HttpProtocolException x = new HttpProtocolException("aMessage");
        assertEquals("aMessage", x.getMessage());
    }

    @Test
    public void testHttpProtocolExceptionThrowable() throws Exception {
        final HttpProtocolException x = new HttpProtocolException(this.cause);
        assertEquals(this.cause, x.getCause());
    }
}
