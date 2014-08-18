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
    private static final int TEST_PORT = 11000;

    private SocketChannel clientChannel;
    private static boolean running = true;

    @Mock
    private ConnectionHandlerParameters connectionParams;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Port port;

    @Mock
    private Message requestMessage;

    @Mock
    private Message responseMessage;

    private MessageWorker messageWorker;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        running = true;
        new Thread() {
            @Override
            public void run() {
                LOG.info("Starting test server socket");
                try (ServerSocketChannel serverChannel = ServerSocketChannel.open()) {
                    serverChannel.bind(new InetSocketAddress(TEST_HOST, TEST_PORT));
                    while (running) {
                        final SocketChannel clientConnection = serverChannel.accept();
                        LOG.info("connected from {}", clientConnection.getRemoteAddress());
                        while (clientConnection.isConnected()) {
                            Thread.sleep(200);
                        }
                        LOG.info("Client Connection closed");
                    }
                    LOG.info("Test Server socket closed");
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }

            }
        }.start();
        Thread.sleep(200);
        this.clientChannel = SocketChannel.open(new InetSocketAddress(TEST_HOST, TEST_PORT));
        this.messageWorker = new MessageWorker(this.clientChannel, this.port, this.connectionParams);
    }

    @After
    public void tearDown() throws Exception {
        running = false;
    }

    @Test
    public void testGetConnectionControl() throws Exception {
        assertEquals(this.connectionParams, this.messageWorker.getConnectionControl());
    }

    @SuppressWarnings({
            "unchecked", "rawtypes"
    })
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

}
