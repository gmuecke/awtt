/**
 * 
 */
package li.moskito.awtt.server;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import li.moskito.awtt.common.Configurable;
import li.moskito.awtt.protocol.Protocol;
import li.moskito.awtt.protocol.ProtocolRegistry;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;

/**
 * WebServer Implementation based on blocking java.nio Channels
 * 
 * @author Gerald
 */
public class MultiportServer implements Server {

    private final List<ConnectionHandler> connectionHandlers;

    /**
     * 
     */
    public MultiportServer() {
        this.connectionHandlers = new ArrayList<>();
    }

    @Override
    public void startServer() {

        final ExecutorService portExecutorService = Executors.newFixedThreadPool(this.connectionHandlers.size());

        for (final ConnectionHandler conHandler : this.connectionHandlers) {
            portExecutorService.execute(conHandler);

        }
        portExecutorService.shutdown();
    }

    @Override
    public void configure(final HierarchicalConfiguration config) throws ConfigurationException {

        this.configureProtocols(config);
        this.configurePorts(config);

    }

    /**
     * Reads the port configuration from the configuration and initializes the ports for the server.
     * 
     * @param config
     *            the configuration to read port configurations from. The configuration location is "ports/listenPort"
     * @throws ConfigurationException
     */
    private void configurePorts(final HierarchicalConfiguration config) throws ConfigurationException {
        final List<HierarchicalConfiguration> portConfigs = config.configurationsAt("ports/listenPort");
        for (final HierarchicalConfiguration portConfig : portConfigs) {

            final Port port = this.createPort(portConfig);

            final ConnectionHandler connectionHandler = this.createConnectionHandler(portConfig
                    .configurationAt("connectionHandler"));
            connectionHandler.bind(port);

            this.connectionHandlers.add(connectionHandler);
        }
    }

    /**
     * Reads the protocol configuration from the configuration instance and thereby initializes the protocol registry.
     * 
     * @param config
     *            the configuration to read protocol configurations from. The configuration location is
     *            "protocols/protocol"
     * @throws ConfigurationException
     */
    private void configureProtocols(final HierarchicalConfiguration config) throws ConfigurationException {

        final List<HierarchicalConfiguration> protocolConfigs = config.configurationsAt("protocols/protocol");

        for (final HierarchicalConfiguration protocolConfig : protocolConfigs) {

            this.createProtocol(protocolConfig);
        }
    }

    /**
     * Creates a protocol instance from the specified configuration or returns an instance from the
     * {@link ProtocolRegistry} if a protocol has already been registered with the same name as in the configuration.
     * 
     * @param protocolConfig
     *            the prototocol configuration containing basically the name (identifier) and the class of the protocol
     *            and optionally additional configuration items.
     * @return the protocol instance from the configuration
     * @throws ConfigurationException
     *             when the protocol can not be created from the provided configuration
     */
    private Protocol<?, ?, ?> createProtocol(final HierarchicalConfiguration protocolConfig)
            throws ConfigurationException {

        final String protocolName = protocolConfig.getString("@name");
        final ProtocolRegistry registry = ProtocolRegistry.getInstance();

        final Protocol<?, ?, ?> protocol;

        // if the protocol has already been registered, use the one from the registry
        if (registry.isAvailable(protocolName)) {
            protocol = registry.forName(protocolName);
        } else {
            // otherwise create a new and register it
            try {
                protocol = (Protocol<?, ?, ?>) Class.forName(protocolConfig.getString("@class")).newInstance();
                registry.registerProtocol(protocolName, protocol);
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                throw new ConfigurationException("Could not create protocol", e);
            }
        }

        // configure if possible. That way already configured protocol can be extended
        if (protocol instanceof Configurable) {
            ((Configurable) protocol).configure(protocolConfig);
        }
        return protocol;
    }

    /**
     * Creates a new {@link ConnectionHandler} from the given configuration
     * 
     * @param config
     *            the configuration containing the parameter for the connection handler, class and maxConnections
     * @return an instance of a {@link ConnectionHandler}
     * @throws ConfigurationException
     */
    private ConnectionHandler createConnectionHandler(final HierarchicalConfiguration config)
            throws ConfigurationException {
        try {
            final ConnectionHandler handler = (ConnectionHandler) Class.forName(config.getString("@class"))
                    .newInstance();

            if (handler instanceof Configurable) {
                ((Configurable) handler).configure(config);
            }

            return handler;
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            throw new ConfigurationException("Could not create connection handler", e);
        }
    }

    /**
     * Creates a new {@link Port} instance from the given configuration
     * 
     * @param portConfig
     *            the configuration containing the port parameters hostname and port
     * @return
     * @throws ConfigurationException
     */
    private Port createPort(final HierarchicalConfiguration portConfig) throws ConfigurationException {

        final String hostname = portConfig.getString("@hostname");
        final int portNumber = portConfig.getInt("@port", -1);
        final String protocolName = portConfig.getString("@protocol");

        final Port port = this.createPort(protocolName, hostname, portNumber);

        return port;
    }

    /**
     * Creates the port for the given parameters.
     * 
     * @param protocolName
     *            the name of the protocol the port adheres to for interpreting and responding to data
     * @param hostname
     *            the name of the host to which the port should be bound
     * @param portNumber
     *            If portnumber is undefined (-1 or 0), the protocols default port is used.
     * @return an instance of a {@link Port}
     * @throws ConfigurationException
     */
    private Port createPort(final String protocolName, final String hostname, final int portNumber)
            throws ConfigurationException {

        final InetAddress address = this.translateHostame(hostname);

        final ProtocolRegistry registry = ProtocolRegistry.getInstance();
        if (!registry.isAvailable(protocolName)) {
            throw new ConfigurationException("Unknown protocol " + protocolName);
        }

        final Port port;
        if (portNumber < 1) {
            port = new Port(address, registry.forName(protocolName));
        } else {
            port = new Port(address, portNumber, registry.forName(protocolName));
        }
        return port;
    }

    /**
     * {@link InetAddress} the hostname into an InetAddress
     * 
     * @param hostname
     *            the hostname to be translated
     * @return the corresponding {@link InetAddress}
     * @throws ConfigurationException
     */
    private InetAddress translateHostame(final String hostname) throws ConfigurationException {
        final InetAddress address;
        if (hostname != null) {
            try {
                address = InetAddress.getByName(hostname);
            } catch (final UnknownHostException e) {
                throw new ConfigurationException(e);
            }
        } else {
            address = InetAddress.getLoopbackAddress();
        }
        return address;
    }

}
