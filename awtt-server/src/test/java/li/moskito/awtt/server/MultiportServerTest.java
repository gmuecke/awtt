package li.moskito.awtt.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import li.moskito.awtt.protocol.http.Request;
import li.moskito.awtt.protocol.http.Response;
import li.moskito.awtt.server.handler.ConnectionHandler;
import li.moskito.awtt.server.handler.MessageHandler;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.tree.xpath.XPathExpressionEngine;
import org.junit.Before;
import org.junit.Test;

public class MultiportServerTest {

    private MultiportServer subject;
    private HierarchicalConfiguration config;

    @Before
    public void setUp() throws Exception {
        this.subject = new MultiportServer();
        this.config = new HierarchicalConfiguration();
        this.config.setExpressionEngine(new XPathExpressionEngine());

        this.config.addProperty("ports", "");
        this.config.addProperty("ports/listenPort", "");
        this.config.addProperty("ports/listenPort/@hostname", "localhost");
        this.config.addProperty("ports/listenPort/@port", "11000");
        this.config.addProperty("ports/listenPort/connectionHandler", "");
        this.config.addProperty("ports/listenPort/connectionHandler/@class",
                "li.moskito.awtt.server.MultiportServerTest$TestConnectionHandler");
        this.config.addProperty("ports/listenPort/messageHandlers", "");
        this.config.addProperty("ports/listenPort/messageHandlers/handler", "");
        this.config.addProperty("ports/listenPort/messageHandlers/handler/@class",
                "li.moskito.awtt.server.MultiportServerTest$TestRequestHandler");

    }

    @Test
    public void testConfigureAndStartServer() throws Exception {
        this.subject.configure(this.config);

        // test if both handler have been created and configured
        assertEquals(1, TestConnectionHandler.instances.size());
        assertEquals(1, TestRequestHandler.instances.size());

        assertTrue(TestConnectionHandler.instances.get(0).configured);
        assertTrue(TestRequestHandler.instances.get(0).configured);

        // test if port was created and request handler added
        final Port port = TestConnectionHandler.instances.get(0).port;
        assertNotNull(port);
        assertEquals(InetAddress.getByName("localhost"), port.getHostname());
        assertEquals(11000, port.getPortNumber());
        assertEquals(1, port.getMessageHandlers().size());

        // test if port was bound to connection handler
        assertEquals(port.getMessageHandlers().get(0), TestRequestHandler.instances.get(0));

        this.subject.startServer();
        Thread.sleep(100); // wait some ms for the executer to execute his task
        assertTrue(TestConnectionHandler.instances.get(0).run);

    }

    public static final class TestRequestHandler implements MessageHandler<Request, Response>, Configurable {

        public static final List<TestRequestHandler> instances = new CopyOnWriteArrayList<>();
        private boolean configured;

        /**
         * 
         */
        public TestRequestHandler() {
            instances.add(this);
        }

        @Override
        public boolean accepts(final Request request) {
            return false;
        }

        @Override
        public Response process(final Request request) {
            return null;
        }

        @Override
        public void configure(final HierarchicalConfiguration config) throws ConfigurationException {
            this.configured = true;

        }

    }

    public static final class TestConnectionHandler implements ConnectionHandler, Configurable {

        private Port port;
        private boolean run;
        private boolean configured;

        public static final List<TestConnectionHandler> instances = new CopyOnWriteArrayList<>();

        /**
         * 
         */
        public TestConnectionHandler() {
            instances.add(this);
        }

        @Override
        public void run() {
            this.run = true;

        }

        @Override
        public void bind(final Port port) {
            this.port = port;

        }

        @Override
        public void close() throws IOException {

        }

        @Override
        public void configure(final HierarchicalConfiguration config) throws ConfigurationException {
            this.configured = true;

        }

    }

}
