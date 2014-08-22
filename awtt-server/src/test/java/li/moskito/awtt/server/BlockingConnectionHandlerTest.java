package li.moskito.awtt.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
import java.util.HashSet;

import li.moskito.awtt.protocol.MessageChannel;
import li.moskito.awtt.protocol.MessageChannelOption;
import li.moskito.awtt.protocol.MessageChannelOptions;
import li.moskito.awtt.protocol.Protocol;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
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
    @Mock
    private Protocol protocol;

    @Mock
    private MessageChannel mockChannel;

    private BlockingConnectionHandler subject;

    @SuppressWarnings("rawtypes")
    private HashSet<MessageChannelOption> supportedOptions;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(this.port.getHostname()).thenReturn(InetAddress.getLoopbackAddress());
        when(this.port.getPortNumber()).thenReturn(TEST_PORT);
        when(this.port.getProtocol()).thenReturn(this.protocol);
        when(this.protocol.openChannel()).thenReturn(this.mockChannel);

        this.subject = new BlockingConnectionHandler();
        this.subject.configure(new HierarchicalConfiguration());

        this.supportedOptions = new HashSet<>();
        when(this.mockChannel.getSupportedOptions()).thenReturn(this.supportedOptions);

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

    @SuppressWarnings("unchecked")
    @Test
    public void testRun_portBound() throws Exception {
        this.subject.bind(this.port);
        when(this.mockChannel.getOption(any(MessageChannelOption.class))).thenReturn(Integer.valueOf(20));

        final Thread subjectThread = new Thread(this.subject);
        subjectThread.start();
        Thread.sleep(200);

        final SocketAddress address = new InetSocketAddress("localhost", TEST_PORT);
        final SocketChannel clientConnection = SocketChannel.open(address);
        assertTrue(clientConnection.isConnected());
        this.subject.close();
        clientConnection.close();
    }

    @SuppressWarnings({
            "unchecked", "rawtypes"
    })
    @Test
    public void testRun_setChannelOptions() throws Exception {
        this.supportedOptions.add(MessageChannelOptions.KEEP_ALIVE_MAX_MESSAGES);
        this.supportedOptions.add(MessageChannelOptions.KEEP_ALIVE_TIMEOUT);
        when(this.mockChannel.getSupportedOptions()).thenReturn(this.supportedOptions);
        when(this.mockChannel.getOption(any(MessageChannelOption.class))).thenReturn(Integer.valueOf(20));

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
        final ArgumentCaptor<Integer> valueCaptor = ArgumentCaptor.forClass(Integer.class);
        final ArgumentCaptor<MessageChannelOption> optionCaptor = ArgumentCaptor.forClass(MessageChannelOption.class);

        verify(this.mockChannel).getSupportedOptions();
        verify(this.mockChannel).setOption(optionCaptor.capture(), valueCaptor.capture());
        assertEquals(MessageChannelOptions.KEEP_ALIVE_TIMEOUT, optionCaptor.getValue());
        assertEquals(Integer.valueOf(5), valueCaptor.getValue());
        // was not configured
        verify(this.mockChannel, times(0)).setOption(MessageChannelOptions.KEEP_ALIVE_MAX_MESSAGES, Integer.valueOf(5));

    }

    @Test(expected = ServerRuntimeException.class)
    public void testRun_portAlreadyBound() throws Exception {
        this.subject.bind(this.port);

        try (ServerSocket s = new ServerSocket(TEST_PORT)) {
            // the test should terminate with an exception because the port of the handler is already in use
            this.subject.run();
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testRun_handlerClosed() throws Exception {
        this.subject.bind(this.port);
        this.subject.close();
        when(this.mockChannel.getOption(any(MessageChannelOption.class))).thenReturn(Integer.valueOf(20));

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
