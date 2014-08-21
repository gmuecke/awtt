package li.moskito.awtt.server;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.Set;

import li.moskito.awtt.protocol.MessageChannel;
import li.moskito.awtt.protocol.MessageChannelOption;
import li.moskito.awtt.protocol.http.HttpChannelOptions;

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

        this.waitForPortAvailability();
    }

    private void waitForPortAvailability() throws InterruptedException {
        int retry = 3;
        while (retry-- > 0 && this.isPortInUse(TEST_PORT)) {
            LOG.warn("Test Port {} is still in use", TEST_PORT);
            Thread.sleep(250);
        }
        if (this.isPortInUse(TEST_PORT)) {
            fail("Test Port " + TEST_PORT + " is still in use");
        } else {
            Thread.sleep(250);
        }
    }

    private boolean isPortInUse(final int portNumber) {
        boolean result;
        try (ServerSocket s = new ServerSocket(portNumber)) {
            result = false;

        } catch (final Exception e) {
            result = true;
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

    @SuppressWarnings("rawtypes")
    @Test
    public void testRun_setChannelOptions() throws Exception {
        final Set<MessageChannelOption> supportedOptions = new HashSet<>();
        supportedOptions.add(HttpChannelOptions.KEEP_ALIVE_MAX_MESSAGES);
        supportedOptions.add(HttpChannelOptions.KEEP_ALIVE_TIMEOUT);
        final MessageChannel mockChannel = this.port.getProtocol().openChannel();
        when(mockChannel.getSupportedOptions()).thenReturn(supportedOptions);

        final HierarchicalConfiguration conf = new HierarchicalConfiguration();
        conf.addProperty("maxConnections", "1");
        conf.addProperty("keepAliveTimeout", "5");
        this.subject.configure(conf);
        this.subject.bind(this.port);

        final Thread subjectThread = new Thread(this.subject);
        subjectThread.start();
        Thread.sleep(200);

        final SocketAddress address = new InetSocketAddress("localhost", TEST_PORT);
        final SocketChannel clientConnection = SocketChannel.open(address);
        assertTrue(clientConnection.isConnected());
        this.subject.close();
        clientConnection.close();

        // keep alive was configured and is therefore passed to the channel
        verify(mockChannel).setOption(HttpChannelOptions.KEEP_ALIVE_TIMEOUT, Integer.valueOf(5));
        // was not configured
        verify(mockChannel, times(0)).setOption(HttpChannelOptions.KEEP_ALIVE_MAX_MESSAGES, Integer.valueOf(5));

    }

    @Test(expected = ServerRuntimeException.class)
    public void testRun_portAlreadyBound() throws Exception {
        this.subject.bind(this.port);

        try (ServerSocket s = new ServerSocket(TEST_PORT)) {
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
        Thread.sleep(250);
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
