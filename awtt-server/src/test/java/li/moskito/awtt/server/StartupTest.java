package li.moskito.awtt.server;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.net.URL;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.tree.xpath.XPathExpressionEngine;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StartupTest {

    /**
     * SLF4J Logger for this class
     */
    private static final Logger LOG = LoggerFactory.getLogger(StartupTest.class);

    private int serverPort;

    @Before
    public void setUp() throws Exception {

        // read the port from the default configuration
        final URL url = Startup.class.getClassLoader().getResource("awttServerConfig.xml");
        final XMLConfiguration xconf = new XMLConfiguration(url);
        xconf.setExpressionEngine(new XPathExpressionEngine());
        this.serverPort = xconf.getInt("//ports/listenPort[@protocol='http']/@port");
        this.waitForPortAvailability(this.serverPort);
    }

    @Test
    public void testMain_noArgs() throws Exception {
        Startup.main(new String[0]);
        Thread.sleep(200); // wait for the server to start

        final SocketAddress addr = new InetSocketAddress("localhost", this.serverPort);
        try (SocketChannel socket = SocketChannel.open(addr)) {
            assertTrue(socket.isConnected());
        }
        Startup.stop();
    }

    @Test
    public void testMain_customConfig() throws Exception {
        final Path testConfig = Files.createTempFile("testConfig", "xml");
        final URL url = Startup.class.getClassLoader().getResource("awttServerConfig.xml");
        Files.copy(url.openStream(), testConfig, StandardCopyOption.REPLACE_EXISTING);
        Startup.main(new String[] {
            testConfig.toString()
        });
        Thread.sleep(200); // wait for the server to start

        final SocketAddress addr = new InetSocketAddress("localhost", this.serverPort);
        try (SocketChannel socket = SocketChannel.open(addr)) {
            assertTrue(socket.isConnected());
        }
        Startup.stop();
    }

    @Test(expected = StartupException.class)
    public void testMain_customConfigInvalid() throws Exception {
        final Path testConfig = Files.createTempFile("testConfig", "xml");
        final ByteArrayInputStream bais = new ByteArrayInputStream("<nothing/>".getBytes());
        Files.copy(bais, testConfig, StandardCopyOption.REPLACE_EXISTING);

        Startup.main(new String[] {
            testConfig.toString()
        });
    }

    @Test(expected = StartupException.class)
    public void testMain_invalidConfigLocation() throws Exception {
        Startup.main(new String[] {
            0xa + "://" + 0x9
        });

    }

    private void waitForPortAvailability(final int port) throws InterruptedException {
        int retry = 3;
        while (retry-- > 0 && this.isPortInUse(port)) {
            LOG.warn("Test Port {} is still in use", port);
            Thread.sleep(250);
        }
        if (this.isPortInUse(port)) {
            fail("Test Port " + port + " is still in use");
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

}
