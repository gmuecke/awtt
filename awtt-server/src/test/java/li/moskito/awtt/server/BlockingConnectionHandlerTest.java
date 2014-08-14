package li.moskito.awtt.server;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;

import li.moskito.awtt.server.Blockinimport li.moskito.awtt.server.handler.BlockingConnectionHandler;
gConnectionHandler;
import li.moskito.awtt.server.Port;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class BlockingConnectionHandlerTest {

    @Mock
    private Port port;
    private BlockingConnectionHandler subject;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(this.port.getPortNumber()).thenReturn(10000);
        this.subject = new BlockingConnectionHandler();
    }

    @Test
    public void testRun_portBound() throws Exception {
        this.subject.bind(this.port);
        final Thread subjectThread = new Thread(this.subject);
        subjectThread.start();
        final SocketAddress address = new InetSocketAddress(10000);
        final SocketChannel clientConnection = SocketChannel.open(address);
        assertTrue(clientConnection.isConnected());
        this.subject.close();
    }

    @Test(expected = ConnectException.class)
    public void testConfigure() throws Exception {

        /*
         * This test is a bit tricky, as the BlockingConnectionHandler should work as it is with default values for the
         * connection pool. The connection pool size is also the only parameter that can be configured. In order to test
         * if the maxConnections parameter is correctly read from the configuration, we set it to an invalid value in
         * order to see, if an error occurs upon startup. We expect a connect exception from the client trying to
         * connect because the server did not start properly (an exception from the server should be seen in the log)
         */

        final HierarchicalConfiguration conf = new HierarchicalConfiguration();
        conf.addProperty("maxConnections", -1); // invalid pool size
        this.subject.configure(conf);

        this.subject.bind(this.port);
        final Thread subjectThread = new Thread(this.subject);
        subjectThread.start();
        final SocketAddress address = new InetSocketAddress(10000);
        final SocketChannel clientConnection = SocketChannel.open(address);
        assertTrue(clientConnection.isConnected());
        this.subject.close();
    }

}
