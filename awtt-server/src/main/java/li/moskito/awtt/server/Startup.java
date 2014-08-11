/**
 * 
 */
package li.moskito.awtt.server;

import java.net.URL;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.tree.xpath.XPathExpressionEngine;

/**
 * Startup class that reads the configuration and initializes the server.
 * 
 * @author Gerald
 */
public class Startup {

    public static void main(final String[] args) throws ConfigurationException {

        // TODO add support for custom configuration file
        final XMLConfiguration xconf = getDefaultConfiguration();

        final Server server = ServerBuilder.buildFromConfiguration(xconf);
        server.startServer();
    }

    private static XMLConfiguration getDefaultConfiguration() throws ConfigurationException {
        final URL url = Startup.class.getClassLoader().getResource("awttServerConfig.xml");
        final XMLConfiguration xconf = loadConfiguration(url);
        return xconf;
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
