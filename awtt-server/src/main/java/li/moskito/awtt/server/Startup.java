/**
 * 
 */
package li.moskito.awtt.server;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.tree.xpath.XPathExpressionEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Startup class that reads the configuration and initializes the server.
 * 
 * @author Gerald
 */
public final class Startup {

    /**
     * SLF4J Logger for this class
     */
    private static final Logger LOG = LoggerFactory.getLogger(Startup.class);

    private static AtomicReference<Server> SERVER_INSTANCE = new AtomicReference<>();

    private Startup() {
    }

    public static void main(final String[] args) throws StartupException {

        try {
            final XMLConfiguration xconf;
            if (args.length == 1) {
                xconf = getCustomConfiguration(args[0]);
            } else {
                xconf = getDefaultConfiguration();
            }
            start(xconf);
        } catch (ConfigurationException | MalformedURLException | InvalidPathException e) {
            throw new StartupException(e);
        }

    }

    public static void start(final XMLConfiguration xconf) throws ConfigurationException {
        if (SERVER_INSTANCE.compareAndSet(null, ServerBuilder.buildFromConfiguration(xconf))) {
            SERVER_INSTANCE.get().startServer();
        } else {
            LOG.warn("Server already started");
        }
    }

    public static void stop() {
        final Server server = SERVER_INSTANCE.getAndSet(null);
        if (server != null) {
            server.stopServer();
        }
    }

    /**
     * @param string
     * @return
     * @throws MalformedURLException
     * @throws ConfigurationException
     */
    private static XMLConfiguration getCustomConfiguration(final String string) throws MalformedURLException,
            ConfigurationException {
        final URL url = Paths.get(string).toUri().toURL();
        return loadConfiguration(url);
    }

    private static XMLConfiguration getDefaultConfiguration() throws ConfigurationException {
        final URL url = Startup.class.getClassLoader().getResource("awttServerConfig.xml");
        return loadConfiguration(url);
    }

    private static XMLConfiguration loadConfiguration(final URL url) throws ConfigurationException {
        final XMLConfiguration xconf = new XMLConfiguration(url);
        if (!"awttServer".equals(xconf.getRootElementName())) {
            throw new ConfigurationException("Configuration is no awttServer configuration: "
                    + xconf.getRootElementName());
        }
        xconf.setExpressionEngine(new XPathExpressionEngine());
        return xconf;
    }
}
