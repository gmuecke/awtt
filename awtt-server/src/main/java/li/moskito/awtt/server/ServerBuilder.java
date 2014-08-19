/**
 * 
 */
package li.moskito.awtt.server;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;

/**
 * Builder to create a server instance. Inspired by JBoss Undertow, see <a
 * href="http://undertow.io/documentation/core/bootstrapping.html">Bootstrapping Undertow</a>
 * 
 * @author Gerald
 */
public final class ServerBuilder {

    private ServerBuilder() {
    }

    public static Server buildFromConfiguration(final HierarchicalConfiguration conf) throws ConfigurationException {
        final Server server;
        try {
            server = (Server) Class.forName(conf.getString("@type")).newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new ConfigurationException("Could not create server", e);
        }
        server.configure(conf);
        return server;
    }
}
