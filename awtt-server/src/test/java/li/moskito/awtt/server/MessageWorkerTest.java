package li.moskito.awtt.server;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.Queue;
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

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Port port;

    @Mock
    private Message requestMessage;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
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
        this.messageWorker = new MessageWorker(this.clientChannel, this.port.getProtocol().openChannel());
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
    public void testRun_connected() throws Exception {

        final Protocol protocol = this.port.getProtocol();
        final MessageChannel mCh = this.prepareChannel(protocol, 1);

        assertTrue(this.clientChannel.isConnected());

        this.messageWorker.run();

        assertFalse(this.clientChannel.isConnected());
        verify(mCh).processMessages();

    }

    @Test
    public void testRun_clientNotConnected() throws Exception {

        final Protocol protocol = this.port.getProtocol();
        final MessageChannel mCh = this.prepareChannel(protocol, 1);

        this.clientChannel.close();
        assertFalse(this.clientChannel.isConnected());

        this.messageWorker.run();

        assertFalse(this.clientChannel.isConnected());
        verify(protocol, times(0)).process(this.requestMessage);
        verify(mCh, times(0)).write(this.responseMessage);

    }

    @Test
    public void testRun_noMessage() throws Exception {

        final Protocol protocol = this.port.getProtocol();
        final MessageChannel mCh = protocol.openChannel();

        // the message channel never returns a message and therefore no invocation of process and write (see below)
        when(mCh.hasMessage()).thenReturn(false);

        assertTrue(this.clientChannel.isConnected());

        // start the message worker and wait till its started (50ms should be enough)
        new Thread(this.messageWorker).start();
        Thread.sleep(50);
        // write a message via server to the client channel of the message worker, the server will shutdown the
        // output and therefore send an -1/EOF
        this.server.write("message".getBytes());
        Thread.sleep(50);
        assertFalse(this.clientChannel.isConnected());

        verify(protocol, times(0)).process(this.requestMessage);
        verify(mCh, times(0)).write(this.responseMessage);

    }

    /**
     * Prepares the channel of the protocol to return exactly 1 message.
     * 
     * @param protocol
     * @param numMessages
     *            the number of messages that should be received by the channel. Every message will be the same request
     * @return
     * @throws IOException
     */
    private MessageChannel prepareChannel(final Protocol protocol, final int numMessages) throws IOException {
        final MessageChannel mCh = protocol.openChannel();
        final Message[] requests = new Message[numMessages];
        final Boolean[] hasMessages = new Boolean[numMessages];
        for (int i = 0; i < numMessages - 1; i++) {
            hasMessages[i] = Boolean.TRUE;
            requests[0] = this.requestMessage;
        }
        hasMessages[numMessages - 1] = Boolean.FALSE;
        requests[numMessages - 1] = null;

        when(mCh.isOpen()).thenReturn(true, hasMessages);
        when(mCh.hasMessage()).thenReturn(true, hasMessages);
        when(mCh.readMessage()).thenReturn(this.requestMessage, requests);
        when(mCh.read(any(ByteBuffer.class))).thenReturn(-1);
        when(protocol.process(any(Message.class))).thenReturn(this.responseMessage);
        return mCh;
    }

    public static class TestServer extends Thread {

        private final AtomicBoolean running = new AtomicBoolean(true);
        private Exception startupException = null;
        private ServerSocketChannel serverChannel;
        private final Queue<byte[]> data = new LinkedList<>();

        @Override
        public void run() {
            LOG.info("Starting test server socket");
            try (ServerSocketChannel serverChannel = ServerSocketChannel.open()) {
                this.serverChannel = serverChannel;
                serverChannel.bind(new InetSocketAddress(TEST_HOST, TEST_PORT));
                while (this.running.get()) {
                    final SocketChannel clientConnection = serverChannel.accept();
                    LOG.info("connected from {}", clientConnection.getRemoteAddress());
                    boolean dataSend = false;
                    while (clientConnection.isConnected() && !dataSend) {
                        while (!this.data.isEmpty()) {
                            clientConnection.write(ByteBuffer.wrap(this.data.poll()));
                            Thread.sleep(10);
                            dataSend = true;
                        }
                        Thread.sleep(10);
                    }
                    clientConnection.shutdownOutput();
                    LOG.info("Client Connection closed");
                }
                LOG.info("Shutdown request received");
            } catch (IOException | InterruptedException e) {
                this.startupException = e;
            } finally {
                LOG.info("Test Server socket closed");
            }
        }

        public void write(final byte[]... data) {
            for (final byte[] d : data) {
                this.data.add(d);
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
