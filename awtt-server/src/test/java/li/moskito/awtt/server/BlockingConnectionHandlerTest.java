package li.moskito.awtt.server;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlockingConnectionHandlerTest {

    /**
     * SLF4J Logger for this class
     */
    private static final Logger LOG = LoggerFactory.getLogger(BlockingConnectionHandlerTest.class);

    private static final int TEST_PORT = 55000;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Port port;
    private BlockingConnectionHandler subject;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(this.port.getHostname()).thenReturn(InetAddress.getLoopbackAddress());
        when(this.port.getPortNumber()).thenReturn(TEST_PORT);
        this.subject = new BlockingConnectionHandler();
        this.subject.configure(new HierarchicalConfiguration());

        int retry = 3;
        while (retry-- > 0 && this.isPortInUse(TEST_PORT)) {
            LOG.warn("Test Port {} is still in use", TEST_PORT);
            Thread.sleep(250);
        }
        if (this.isPortInUse(TEST_PORT)) {
            fail("Test Port " + TEST_PORT + " is still in use");
        }
        Thread.sleep(100);

    }

    private boolean isPortInUse(final int portNumber) {
        boolean result;
        try (Socket s = new Socket(InetAddress.getLocalHost(), portNumber)) {
            result = true;

        } catch (final Exception e) {
            result = false;
        }

        return result;
    }

    @Test
    public void testRun_portBound() throws Exception {
        this.subject.bind(this.port);

        final Thread subjectThread = new Thread(this.subject);
        subjectThread.start();
        Thread.sleep(200);

        final SocketAddress address = new InetSocketAddress("localhost", TEST_PORT);
        final SocketChannel clientConnection = SocketChannel.open(address);
        assertTrue(clientConnection.isConnected());
        this.subject.close();
        clientConnection.close();
    }

    @Test(expected = ServerRuntimeException.class)
    public void testRun_portAlreadyBound() throws Exception {
        this.subject.bind(this.port);

        try (ServerSocketChannel server = ServerSocketChannel.open()) {
            server.bind(new InetSocketAddress("localhost", TEST_PORT));

            // the test should terminate with an exception because the port of the handler is already in use
            this.subject.run();
        }
    }

    @Test
    public void testRun_handlerClosed() throws Exception {
        this.subject.bind(this.port);
        this.subject.close();

        final Thread subjectThread = new Thread(this.subject);
        subjectThread.start();
        Thread.sleep(50);
        assertFalse(subjectThread.isAlive()); // subject should have finished by now

    }

    @Test(expected = IllegalArgumentException.class)
    public void testConfigure_invalidPoolSize() throws Exception {

        final HierarchicalConfiguration conf = new HierarchicalConfiguration();
        conf.addProperty("maxConnections", "-1"); // invalid pool size
        this.subject.configure(conf);

        this.subject.bind(this.port);
        this.subject.run();

    }

}
