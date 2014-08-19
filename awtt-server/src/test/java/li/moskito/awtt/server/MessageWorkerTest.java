package li.moskito.awtt.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicBoolean;

import li.moskito.awtt.protocol.Message;
import li.moskito.awtt.protocol.MessageChannel;
import li.moskito.awtt.protocol.Protocol;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(MockitoJUnitRunner.class)
public class MessageWorkerTest {

    /**
     * SLF4J Logger for this class
     */
    private static final Logger LOG = LoggerFactory.getLogger(MessageWorkerTest.class);

    private static final String TEST_HOST = "localhost";
    private static final int TEST_PORT = 11010;

    private SocketChannel clientChannel;

    @Mock
    private ConnectionHandlerParameters connectionParams;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Port port;

    @Mock
    private Message requestMessage;

    @Mock
    private Message responseMessage;

    private MessageWorker messageWorker;

    private TestServer server;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        this.server = new TestServer();
        this.server.start();
        Thread.sleep(100);
        if (this.server.getStartupException() != null) {
            throw this.server.getStartupException();
        }

        this.clientChannel = SocketChannel.open(new InetSocketAddress(TEST_HOST, TEST_PORT));
        this.messageWorker = new MessageWorker(this.clientChannel, this.port, this.connectionParams);
    }

    @After
    public void tearDown() throws Exception {
        try {
            this.clientChannel.close();
        } catch (final IOException e) {
            LOG.warn("Could not close client connection", e);
        }
        this.server.shutdown();

    }

    @Test
    public void testGetConnectionControl() throws Exception {
        assertEquals(this.connectionParams, this.messageWorker.getConnectionControl());
    }

    @Test
    public void testRun() throws Exception {

        final Protocol protocol = this.port.getProtocol();
        final MessageChannel mCh = protocol.openChannel();

        when(mCh.hasMessage()).thenReturn(true, false);
        when(mCh.readMessage()).thenReturn(this.requestMessage, (Message) null);
        when(mCh.read(any(ByteBuffer.class))).thenReturn(-1);
        when(protocol.process(any(Message.class))).thenReturn(this.responseMessage);

        assertTrue(this.clientChannel.isConnected());

        this.messageWorker.run();

        assertFalse(this.clientChannel.isConnected());
        verify(protocol).process(this.requestMessage);
        verify(mCh).write(this.responseMessage);

    }

    public static class TestServer extends Thread {

        private final AtomicBoolean running = new AtomicBoolean(true);
        private Exception startupException = null;
        private ServerSocketChannel serverChannel;

        @Override
        public void run() {
            LOG.info("Starting test server socket");
            try (ServerSocketChannel serverChannel = ServerSocketChannel.open()) {
                this.serverChannel = serverChannel;
                serverChannel.bind(new InetSocketAddress(TEST_HOST, TEST_PORT));
                while (this.running.get()) {
                    final SocketChannel clientConnection = serverChannel.accept();
                    LOG.info("connected from {}", clientConnection.getRemoteAddress());
                    while (clientConnection.isConnected()) {
                        Thread.sleep(10);
                    }
                    LOG.info("Client Connection closed");
                }
                LOG.info("Shutdown request received");
            } catch (IOException | InterruptedException e) {
                this.startupException = e;
            } finally {
                LOG.info("Test Server socket closed");
            }
        }

        public Exception getStartupException() {
            return this.startupException;
        }

        public void shutdown() {
            LOG.info("Shutting down server");
            this.running.set(false);
            try {
                this.serverChannel.close();
            } catch (final IOException e) {
                LOG.warn("Could not close server channel", e);
            }
        }
    }
}
