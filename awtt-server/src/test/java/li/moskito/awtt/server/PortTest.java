package li.moskito.awtt.server;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.net.InetAddress;

import li.moskito.awtt.protocol.Protocol;

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
    private Protocol protocol;

    private final int portNumber = 100;

    private Port port;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(this.protocol.getDefaultPort()).thenReturn(80);
        this.port = new Port(this.hostname, this.portNumber, this.protocol);
    }

    @Test
    public void testDefaultPortNumber() throws Exception {
        final Port port = new Port(this.hostname, this.protocol);
        assertEquals(80, port.getPortNumber());
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
    public void testGetProtocol() throws Exception {
        assertEquals(this.protocol, this.port.getProtocol());
    }

}
