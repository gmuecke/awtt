package li.moskito.awtt.server.handler;

import static li.moskito.awtt.protocol.http.StatusCodes.NOT_IMPLEMENTED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import li.moskito.awtt.protocol.http.Commands;
import li.moskito.awtt.protocol.http.Request;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class HttpRequestHandlerTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Request request;

    private HttpRequestHandler subject;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        this.subject = new HttpRequestHandler() {};
    }

    @Test
    public void testAccepts() throws Exception {
        assertTrue(this.subject.accepts(this.request));
    }

    @Test
    public void testProcess() throws Exception {
        // no command set in mock
        when(this.request.getCommand()).thenReturn(Commands.GET);
        assertEquals(NOT_IMPLEMENTED, this.subject.process(this.request).getStatus());
        when(this.request.getCommand()).thenReturn(Commands.POST);
        assertEquals(NOT_IMPLEMENTED, this.subject.process(this.request).getStatus());
        when(this.request.getCommand()).thenReturn(Commands.PUT);
        assertEquals(NOT_IMPLEMENTED, this.subject.process(this.request).getStatus());
        when(this.request.getCommand()).thenReturn(Commands.DELETE);
        assertEquals(NOT_IMPLEMENTED, this.subject.process(this.request).getStatus());
        when(this.request.getCommand()).thenReturn(Commands.HEAD);
        assertEquals(NOT_IMPLEMENTED, this.subject.process(this.request).getStatus());
        when(this.request.getCommand()).thenReturn(Commands.OPTIONS);
        assertEquals(NOT_IMPLEMENTED, this.subject.process(this.request).getStatus());
        when(this.request.getCommand()).thenReturn(Commands.CONNECT);
        assertEquals(NOT_IMPLEMENTED, this.subject.process(this.request).getStatus());
        when(this.request.getCommand()).thenReturn(Commands.TRACE);
        assertEquals(NOT_IMPLEMENTED, this.subject.process(this.request).getStatus());

    }

    @Test
    public void testOnGet() throws Exception {
        when(this.request.getCommand()).thenReturn(Commands.GET);
        assertEquals(NOT_IMPLEMENTED, this.subject.onGet(this.request).getStatus());
    }

    @Test
    public void testOnPost() throws Exception {
        when(this.request.getCommand()).thenReturn(Commands.POST);
        assertEquals(NOT_IMPLEMENTED, this.subject.onPost(this.request).getStatus());
    }

    @Test
    public void testOnPut() throws Exception {
        when(this.request.getCommand()).thenReturn(Commands.PUT);
        assertEquals(NOT_IMPLEMENTED, this.subject.onPut(this.request).getStatus());
    }

    @Test
    public void testOnDelete() throws Exception {
        when(this.request.getCommand()).thenReturn(Commands.DELETE);
        assertEquals(NOT_IMPLEMENTED, this.subject.onDelete(this.request).getStatus());
    }

    @Test
    public void testOnHead() throws Exception {
        when(this.request.getCommand()).thenReturn(Commands.HEAD);
        assertEquals(NOT_IMPLEMENTED, this.subject.onHead(this.request).getStatus());
    }

    @Test
    public void testOnOptions() throws Exception {
        when(this.request.getCommand()).thenReturn(Commands.OPTIONS);
        assertEquals(NOT_IMPLEMENTED, this.subject.onOptions(this.request).getStatus());
    }

    @Test
    public void testOnConnect() throws Exception {
        when(this.request.getCommand()).thenReturn(Commands.CONNECT);
        assertEquals(NOT_IMPLEMENTED, this.subject.onConnect(this.request).getStatus());
    }

    @Test
    public void testOnTrace() throws Exception {
        when(this.request.getCommand()).thenReturn(Commands.TRACE);
        assertEquals(NOT_IMPLEMENTED, this.subject.onTrace(this.request).getStatus());
    }

}
