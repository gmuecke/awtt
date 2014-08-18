package li.moskito.awtt.server;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.List;

import li.moskito.awtt.common.Configurable;
import li.moskito.awtt.protocol.Header;
import li.moskito.awtt.protocol.HeaderField;
import li.moskito.awtt.protocol.HeaderFieldDefinition;
import li.moskito.awtt.protocol.Message;
import li.moskito.awtt.protocol.MessageChannel;
import li.moskito.awtt.protocol.Protocol;
import li.moskito.awtt.protocol.ProtocolException;
import li.moskito.awtt.protocol.ProtocolHandler;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.tree.xpath.XPathExpressionEngine;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

public class MultiportServerTest {

    @Spy
    private TestProtocolHandler protocolHandler;
    @Spy
    private TestMessageChannel messageChannel;
    @Spy
    private TestProtocol protocol;
    @Spy
    private TestConnectionHandler connectionHandler;

    private MultiportServer subject;
    private HierarchicalConfiguration config;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        TestProtocolHandler.spy = this.protocolHandler;
        TestMessageChannel.spy = this.messageChannel;
        TestProtocol.spy = this.protocol;
        TestConnectionHandler.spy = this.connectionHandler;

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

        // // test if port was created and request handler added
        // final Port port = TestConnectionHandler.spy.get(0).port;
        // assertNotNull(port);
        // assertEquals(InetAddress.getByName("localhost"), port.getHostname());
        // assertEquals(11000, port.getPortNumber());
        //
        // this.subject.startServer();
        // Thread.sleep(100); // wait some ms for the executer to execute his task
        // assertTrue(TestConnectionHandler.instances.get(0).run);
        fail("implement");

    }

    public static final class TestProtocolHandler implements ProtocolHandler<Message, Message>, Configurable {

        public static TestProtocolHandler spy;

        @Override
        public boolean accepts(final Message request) {
            return spy.accepts(request);
        }

        @Override
        public Message process(final Message request) {
            return spy.process(request);
        }

        @Override
        public void configure(final HierarchicalConfiguration config) throws ConfigurationException {
            spy.configure(config);
        }

    }

    public static final class TestMessageChannel extends MessageChannel {

        public static TestMessageChannel spy;

        @Override
        public Protocol<?, ?, ?> getProtocol() {
            return spy.getProtocol();
        }

        @Override
        protected Message parseMessage(final ByteBuffer src) throws ProtocolException, IOException {
            return spy.parseMessage(src);
        }

        @Override
        protected CharBuffer serializeHeader(final Header header) {
            return spy.serializeHeader(header);
        }

    }

    public static final class TestProtocol implements Protocol<Message, Message, TestMessageChannel> {

        public static TestProtocol spy;

        @Override
        public int getDefaultPort() {
            return spy.getDefaultPort();
        }

        @Override
        public TestMessageChannel openChannel() {
            return spy.openChannel();
        }

        @Override
        public Message process(final Message message) {
            return spy.process(message);
        }

        @Override
        public boolean isCloseChannelsAfterProcess(final Message request) {
            return spy.isCloseChannelsAfterProcess(request);
        }

        @Override
        public <D extends HeaderFieldDefinition, T extends HeaderField<D, ?>> List<T> getKeepAliverHeaders(
                final ConnectionHandlerParameters connectionControl) {
            return spy.getKeepAliverHeaders(connectionControl);
        }

    }

    public static final class TestConnectionHandler implements ConnectionHandler, Configurable {

        public static TestConnectionHandler spy;

        @Override
        public void run() {
            spy.run();

        }

        @Override
        public void bind(final Port port) {
            spy.bind(port);
        }

        @Override
        public void close() throws IOException {
            spy.close();
        }

        @Override
        public void configure(final HierarchicalConfiguration config) throws ConfigurationException {
            spy.configure(config);
        }

    }

}
