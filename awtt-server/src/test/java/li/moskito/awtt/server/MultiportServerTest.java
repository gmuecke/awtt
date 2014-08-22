package li.moskito.awtt.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.Set;

import li.moskito.awtt.common.Configurable;
import li.moskito.awtt.protocol.Header;
import li.moskito.awtt.protocol.Message;
import li.moskito.awtt.protocol.MessageChannel;
import li.moskito.awtt.protocol.MessageChannelOption;
import li.moskito.awtt.protocol.Protocol;
import li.moskito.awtt.protocol.ProtocolException;
import li.moskito.awtt.protocol.ProtocolHandler;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.tree.xpath.XPathExpressionEngine;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class MultiportServerTest {

    @Mock
    private TestProtocolHandler protocolHandler;
    @Mock
    private TestMessageChannel messageChannel;
    @Mock
    private TestProtocol protocol;
    @Mock
    private TestConnectionHandler connectionHandler;

    private MultiportServer subject;
    private HierarchicalConfiguration config;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        TestProtocolHandler.mock = this.protocolHandler;
        TestMessageChannel.mock = this.messageChannel;
        TestProtocol.mock = this.protocol;
        TestConnectionHandler.mock = this.connectionHandler;

        this.subject = new MultiportServer();
        this.config = new HierarchicalConfiguration();
        this.config.setExpressionEngine(new XPathExpressionEngine());

        // create protocol configuration
        this.config.addProperty("protocols", "");
        this.config.addProperty("protocols/protocol", "");
        this.config.addProperty("protocols/protocol/@name", "test");
        this.config.addProperty("protocols/protocol/@class", "li.moskito.awtt.server.MultiportServerTest$TestProtocol");
        this.config.addProperty("protocols/protocol/handler", "");
        this.config.addProperty("protocols/protocol/handler/@class",
                "li.moskito.awtt.server.MultiportServerTest$TestProtocolHandler");

        // create ports configuration
        this.config.addProperty("ports", "");
        this.config.addProperty("ports/listenPort", "");
        this.config.addProperty("ports/listenPort/@hostname", "localhost");
        this.config.addProperty("ports/listenPort/@port", "11000");
        this.config.addProperty("ports/listenPort/@protocol", "test");

        // create connection handler configuration
        this.config.addProperty("ports/listenPort/connectionHandler", "");
        this.config.addProperty("ports/listenPort/connectionHandler/@class",
                "li.moskito.awtt.server.MultiportServerTest$TestConnectionHandler");
    }

    @Test
    public void testConfigureAndStartServer() throws Exception {
        this.subject.configure(this.config);

        // test if port was created and request handler added
        final ArgumentCaptor<Port> captor = ArgumentCaptor.forClass(Port.class);
        Mockito.verify(TestConnectionHandler.mock).bind(captor.capture());
        final Port port = captor.getValue();

        assertNotNull(port);
        assertEquals(InetAddress.getByName("localhost"), port.getHostname());
        assertEquals(11000, port.getPortNumber());
        final Protocol protocol = port.getProtocol();
        assertNotNull(protocol);
        assertTrue(protocol instanceof TestProtocol);

        this.subject.startServer();
        Thread.sleep(100); // wait some ms for the executer to execute his task
        verify(TestConnectionHandler.mock).run();

        // TODO add some more assertions regarding the protocol

    }

    @Test
    public void testStopServer() throws Exception {
        assertFalse(this.subject.isRunning());
        this.testConfigureAndStartServer();
        assertTrue(this.subject.isRunning());
        this.subject.stopServer();
        assertFalse(this.subject.isRunning());
    }
    public static class TestProtocolHandler implements ProtocolHandler<Message, Message>, Configurable {

        public static TestProtocolHandler mock;

        @Override
        public boolean accepts(final Message request) {
            return mock.accepts(request);
        }

        @Override
        public Message process(final Message request) {
            return mock.process(request);
        }

        @Override
        public void configure(final HierarchicalConfiguration config) throws ConfigurationException {
            mock.configure(config);
        }

    }

    public static class TestMessageChannel extends MessageChannel {

        public static TestMessageChannel mock;

        @Override
        public Protocol getProtocol() {
            return mock.getProtocol();
        }

        @Override
        protected Message parseMessage(final ByteBuffer src) throws ProtocolException, IOException {
            return mock.parseMessage(src);
        }

        @Override
        protected CharBuffer serializeHeader(final Header header) {
            return mock.serializeHeader(header);
        }

        @SuppressWarnings("rawtypes")
        @Override
        public Set<MessageChannelOption> getSupportedOptions() {
            return mock.getSupportedOptions();
        }

    }

    public static class TestProtocol implements Protocol {

        public static TestProtocol mock;

        @Override
        public int getDefaultPort() {
            return mock.getDefaultPort();
        }

        @Override
        public TestMessageChannel openChannel() {
            return mock.openChannel();
        }

        @Override
        public Message process(final Message message) {
            return mock.process(message);
        }

    }

    public static class TestConnectionHandler implements ConnectionHandler, Configurable {

        public static TestConnectionHandler mock;

        @Override
        public void run() {
            mock.run();

        }

        @Override
        public void bind(final Port port) {
            mock.bind(port);
        }

        @Override
        public void close() throws IOException {
            mock.close();
        }

        @Override
        public void configure(final HierarchicalConfiguration config) throws ConfigurationException {
            mock.configure(config);
        }

    }

}
