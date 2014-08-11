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

import li.moskito.awtt.server.handler.ConnectionHandler;
import li.moskito.awtt.server.handler.MessageHandler;

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

        final List<HierarchicalConfiguration> portConfigs = config.configurationsAt("ports/listenPort");
        for (final HierarchicalConfiguration portConfig : portConfigs) {

            final Port port = this.createPort(portConfig);

            final ConnectionHandler connectionHandler = this.createConnectionHandler(portConfig
                    .configurationAt("connectionHandler"));

            final List<MessageHandler<?, ?>> requestHandlers = this.createMessageHandlers(portConfig
                    .configurationsAt("messageHandlers/handler"));

            port.addMessageHandlers(requestHandlers);
            connectionHandler.bind(port);

            this.connectionHandlers.add(connectionHandler);
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
        final int portNumber = portConfig.getInt("@port");
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
        return new Port(address, portNumber);
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
     * @param configurationsAt
     * @return
     * @throws ConfigurationException
     */
    private List<MessageHandler<?, ?>> createMessageHandlers(final List<HierarchicalConfiguration> config)
            throws ConfigurationException {
        final List<MessageHandler<?, ?>> handlers = new ArrayList<>();
        for (final HierarchicalConfiguration handlerConfig : config) {

            try {
                //@formatter:off
                final MessageHandler<?, ?> handler = 
                     (MessageHandler<?, ?>) Class
                        .forName(handlerConfig.getString("@class")).newInstance();
                // @formatter:on
                if (handler instanceof Configurable) {
                    ((Configurable) handler).configure(handlerConfig);
                }
                handlers.add(handler);
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                throw new ConfigurationException("Could not create handler", e);
            }
        }
        return handlers;
    }

}
