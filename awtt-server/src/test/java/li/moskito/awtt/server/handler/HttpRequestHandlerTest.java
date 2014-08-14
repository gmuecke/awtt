package li.moskito.awtt.server.handler;

import static li.moskito.awtt.protocol.http.HttpStatusCodes.NOT_IMPLEMENTED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import li.moskito.awtt.protocol.http.HttpCommands;
import li.moskito.awtt.protocol.http.HttpRequest;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class HttpRequestHandlerTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private HttpRequest httpRequest;

    private HttpRequestHandler subject;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        this.subject = new HttpRequestHandler() {};
    }

    @Test
    public void testAccepts() throws Exception {
        assertTrue(this.subject.accepts(this.httpRequest));
    }

    @Test
    public void testProcess() throws Exception {
        // no command set in mock
        when(this.httpRequest.getCommand()).thenReturn(HttpCommands.GET);
        assertEquals(NOT_IMPLEMENTED, this.subject.process(this.httpRequest).getStatus());
        when(this.httpRequest.getCommand()).thenReturn(HttpCommands.POST);
        assertEquals(NOT_IMPLEMENTED, this.subject.process(this.httpRequest).getStatus());
        when(this.httpRequest.getCommand()).thenReturn(HttpCommands.PUT);
        assertEquals(NOT_IMPLEMENTED, this.subject.process(this.httpRequest).getStatus());
        when(this.httpRequest.getCommand()).thenReturn(HttpCommands.DELETE);
        assertEquals(NOT_IMPLEMENTED, this.subject.process(this.httpRequest).getStatus());
        when(this.httpRequest.getCommand()).thenReturn(HttpCommands.HEAD);
        assertEquals(NOT_IMPLEMENTED, this.subject.process(this.httpRequest).getStatus());
        when(this.httpRequest.getCommand()).thenReturn(HttpCommands.OPTIONS);
        assertEquals(NOT_IMPLEMENTED, this.subject.process(this.httpRequest).getStatus());
        when(this.httpRequest.getCommand()).thenReturn(HttpCommands.CONNECT);
        assertEquals(NOT_IMPLEMENTED, this.subject.process(this.httpRequest).getStatus());
        when(this.httpRequest.getCommand()).thenReturn(HttpCommands.TRACE);
        assertEquals(NOT_IMPLEMENTED, this.subject.process(this.httpRequest).getStatus());

    }

    @Test
    public void testOnGet() throws Exception {
        when(this.httpRequest.getCommand()).thenReturn(HttpCommands.GET);
        assertEquals(NOT_IMPLEMENTED, this.subject.onGet(this.httpRequest).getStatus());
    }

    @Test
    public void testOnPost() throws Exception {
        when(this.httpRequest.getCommand()).thenReturn(HttpCommands.POST);
        assertEquals(NOT_IMPLEMENTED, this.subject.onPost(this.httpRequest).getStatus());
    }

    @Test
    public void testOnPut() throws Exception {
        when(this.httpRequest.getCommand()).thenReturn(HttpCommands.PUT);
        assertEquals(NOT_IMPLEMENTED, this.subject.onPut(this.httpRequest).getStatus());
    }

    @Test
    public void testOnDelete() throws Exception {
        when(this.httpRequest.getCommand()).thenReturn(HttpCommands.DELETE);
        assertEquals(NOT_IMPLEMENTED, this.subject.onDelete(this.httpRequest).getStatus());
    }

    @Test
    public void testOnHead() throws Exception {
        when(this.httpRequest.getCommand()).thenReturn(HttpCommands.HEAD);
        assertEquals(NOT_IMPLEMENTED, this.subject.onHead(this.httpRequest).getStatus());
    }

    @Test
    public void testOnOptions() throws Exception {
        when(this.httpRequest.getCommand()).thenReturn(HttpCommands.OPTIONS);
        assertEquals(NOT_IMPLEMENTED, this.subject.onOptions(this.httpRequest).getStatus());
    }

    @Test
    public void testOnConnect() throws Exception {
        when(this.httpRequest.getCommand()).thenReturn(HttpCommands.CONNECT);
        assertEquals(NOT_IMPLEMENTED, this.subject.onConnect(this.httpRequest).getStatus());
    }

    @Test
    public void testOnTrace() throws Exception {
        when(this.httpRequest.getCommand()).thenReturn(HttpCommands.TRACE);
        assertEquals(NOT_IMPLEMENTED, this.subject.onTrace(this.httpRequest).getStatus());
    }

}
