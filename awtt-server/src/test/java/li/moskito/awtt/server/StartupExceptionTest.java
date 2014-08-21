package li.moskito.awtt.server;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class StartupExceptionTest {

    @Test
    public void testStartupException() throws Exception {
        final Exception x = new Exception();
        final StartupException sx = new StartupException(x);
        assertEquals(x, sx.getCause());
    }

}
