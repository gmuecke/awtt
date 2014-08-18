package li.moskito.awtt.protocol;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ProtocolExceptionTest {
    @Mock
    private Throwable cause;

    private final String message = "testMessage";

    private ProtocolException protocolException;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

    }

    @Test
    public void testProtocolExceptionStringThrowable() throws Exception {
        this.protocolException = new ProtocolException(this.message, this.cause);
        assertEquals(this.message, this.protocolException.getMessage());
        assertEquals(this.cause, this.protocolException.getCause());
    }

    @Test
    public void testProtocolExceptionString() throws Exception {
        this.protocolException = new ProtocolException(this.message);
        assertEquals(this.message, this.protocolException.getMessage());
    }

    @Test
    public void testProtocolExceptionThrowable() throws Exception {
        this.protocolException = new ProtocolException(this.cause);
        assertEquals(this.cause, this.protocolException.getCause());
    }

}
