package li.moskito.awtt.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import li.moskito.awtt.server.handler.RequestHandler;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PortTest {
    @Mock
    private InetAddress hostname;
    @Mock
    private RequestHandler handler;

    private final int portNumber = 100;

    private Port port;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        this.port = new Port(this.hostname, this.portNumber);
    }

    @Test
    public void testGetPortNumber() throws Exception {
        assertEquals(this.portNumber, this.port.getPortNumber());
    }

    @Test
    public void testGetHostname() throws Exception {
        assertEquals(this.hostname, this.port.getHostname());
    }

    @Test
    public void testGetRequestHandlers() throws Exception {
        assertNotNull(this.port.getRequestHandlers());
        assertTrue(this.port.getRequestHandlers().isEmpty());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetHandlers_addToResult_fail() throws Exception {
        this.port.getRequestHandlers().add(this.handler);

    }

    @Test
    public void testAddHandler() throws Exception {
        this.port.addRequestHandler(this.handler);
        assertNotNull(this.port.getRequestHandlers());
        assertTrue(this.port.getRequestHandlers().contains(this.handler));
    }

    @Test
    public void testAddHandlers() throws Exception {
        final List<RequestHandler> handlers = new ArrayList<>();
        final RequestHandler handler1 = mock(RequestHandler.class);
        final RequestHandler handler2 = mock(RequestHandler.class);
        handlers.add(handler1);
        handlers.add(handler2);
        this.port.addRequestHandlers(handlers);
        assertNotNull(this.port.getRequestHandlers());
        assertTrue(this.port.getRequestHandlers().contains(handler1));
        assertTrue(this.port.getRequestHandlers().contains(handler2));
    }

}
