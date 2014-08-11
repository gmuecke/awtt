package li.moskito.awtt.server;

import static org.junit.Assert.assertTrue;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.junit.Test;

public class ServerBuilderTest {

    public static final class TestServer implements Server {

        private boolean configured;

        @Override
        public void configure(final HierarchicalConfiguration config) throws ConfigurationException {
            this.configured = true;
        }

        @Override
        public void startServer() {

        }

    }

    @Test
    public void testBuildFromConfiguration() throws Exception {

        final HierarchicalConfiguration conf = new HierarchicalConfiguration();
        conf.addProperty("@type", "li.moskito.awtt.server.ServerBuilderTest$TestServer");

        final Server server = ServerBuilder.buildFromConfiguration(conf);
        assertTrue(server instanceof TestServer);
        assertTrue(((TestServer) server).configured);
    }

}
