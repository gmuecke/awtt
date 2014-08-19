package li.moskito.awtt.protocol.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class HttpProtocolHandlerTest {

    @Mock
    private HttpRequest request;

    private HttpProtocolHandler handler;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        this.handler = new HttpProtocolHandler();
    }

    @Test
    public void testAccepts() throws Exception {
        assertTrue(this.handler.accepts(this.request));
    }

    private void assertStatusCodeOnCommand(final HttpStatusCodes statusCode, final HttpCommands command) {
        when(this.request.getCommand()).thenReturn(command);
        final HttpResponse response = this.handler.process(this.request);
        assertNotNull(response);
        assertEquals(statusCode, response.getStatusCode());
    }

    @Test
    public void testProcess_allCommands() throws Exception {
        for (final HttpCommands command : HttpCommands.values()) {
            this.assertStatusCodeOnCommand(HttpStatusCodes.NOT_IMPLEMENTED, command);
        }
    }

    @Test
    public void testOnGet() throws Exception {
        this.assertStatusCodeOnCommand(HttpStatusCodes.NOT_IMPLEMENTED, HttpCommands.GET);
    }

    @Test
    public void testOnPost() throws Exception {
        this.assertStatusCodeOnCommand(HttpStatusCodes.NOT_IMPLEMENTED, HttpCommands.POST);
    }

    @Test
    public void testOnPut() throws Exception {
        this.assertStatusCodeOnCommand(HttpStatusCodes.NOT_IMPLEMENTED, HttpCommands.PUT);
    }

    @Test
    public void testOnDelete() throws Exception {
        this.assertStatusCodeOnCommand(HttpStatusCodes.NOT_IMPLEMENTED, HttpCommands.DELETE);
    }

    @Test
    public void testOnHead() throws Exception {
        this.assertStatusCodeOnCommand(HttpStatusCodes.NOT_IMPLEMENTED, HttpCommands.HEAD);
    }

    @Test
    public void testOnOptions() throws Exception {
        this.assertStatusCodeOnCommand(HttpStatusCodes.NOT_IMPLEMENTED, HttpCommands.OPTIONS);
    }

    @Test
    public void testOnConnect() throws Exception {
        this.assertStatusCodeOnCommand(HttpStatusCodes.NOT_IMPLEMENTED, HttpCommands.CONNECT);
    }

    @Test
    public void testOnTrace() throws Exception {
        this.assertStatusCodeOnCommand(HttpStatusCodes.NOT_IMPLEMENTED, HttpCommands.TRACE);
    }

}
