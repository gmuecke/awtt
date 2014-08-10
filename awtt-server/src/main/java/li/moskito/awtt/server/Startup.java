/**
 * 
 */
package li.moskito.awtt.server;

import java.net.URL;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.tree.ExpressionEngine;
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

        final ExpressionEngine engine = new XPathExpressionEngine();
        final URL url = Startup.class.getClassLoader().getResource("awttServerConfig.xml");

        final XMLConfiguration xconf = new XMLConfiguration(url);
        xconf.setExpressionEngine(engine);
        return xconf;
    }
}
