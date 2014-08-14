package li.moskito.awtt.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import li.moskito.awtt.protocol.http.HttpRequest;
import li.moskito.awtt.protocol.http.HttpResponse;
import li.moskito.awtt.server.handler.MessageHandler;

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
    private MessageHandler<HttpRequest, HttpResponse> handler;

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
    public void testGetMessageHandlers() throws Exception {
        assertNotNull(this.port.getMessageHandlers());
        assertTrue(this.port.getMessageHandlers().isEmpty());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetHandlers_addToResult_fail() throws Exception {
        this.port.getMessageHandlers().add(this.handler);

    }

    @Test
    public void testAddHandler() throws Exception {
        this.port.addMessageHandler(this.handler);
        assertNotNull(this.port.getMessageHandlers());
        assertTrue(this.port.getMessageHandlers().contains(this.handler));
    }

    @Test
    public void testAddHandlers() throws Exception {
        final List<MessageHandler<?, ?>> handlers = new ArrayList<>();
        final MessageHandler<?, ?> handler1 = mock(MessageHandler.class);
        final MessageHandler<?, ?> handler2 = mock(MessageHandler.class);
        handlers.add(handler1);
        handlers.add(handler2);
        this.port.addMessageHandlers(handlers);
        assertNotNull(this.port.getMessageHandlers());
        assertTrue(this.port.getMessageHandlers().contains(handler1));
        assertTrue(this.port.getMessageHandlers().contains(handler2));
    }

}
