/**
 * 
 */
package li.moskito.awtt.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import li.moskito.awtt.common.Configurable;
import li.moskito.awtt.protocol.MessageChannel;
import li.moskito.awtt.protocol.MessageChannelOption;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gerald
 */
public class BlockingConnectionHandler implements ConnectionHandler, Configurable {

    /**
     * 
     */
    private static final String KEEP_ALIVE_TIMEOUT_OPTION = "keepAliveTimeout";

    /**
     * SLF4J Logger for this class
     */
    private static final Logger LOG = LoggerFactory.getLogger(BlockingConnectionHandler.class);

    /**
     * 
     */
    private static final String MAX_CONNECTIONS_OPTION = "maxConnections";

    private static final int DEFAULT_MAX_CONNECTIONS = 5;

    private Port port;
    private final AtomicBoolean running = new AtomicBoolean(true);

    private final Map<String, String> connectionOptions = new HashMap<>();

    @Override
    public void run() {

        // create a socket address from port configuration
        final SocketAddress bindAddress = new InetSocketAddress(this.port.getHostname(), this.port.getPortNumber());

        // open the server socket (AutoCloseable)
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {

            // bind the channel to the address
            this.initializeChannel(serverSocketChannel, bindAddress);

            // handle incoming connections (blocking operation)
            this.handleConnections(serverSocketChannel);

        } catch (final IOException e) {
            LOG.error("Could not create server socket", e);
            throw new ServerRuntimeException(e);
        }
    }

    /**
     * Accepts incoming connection on the specified serverSocketChannel. Every incoming connection is dispatched to a
     * worker in the thread pool.
     * 
     * @param serverSocketChannel
     */
    private void handleConnections(final ServerSocketChannel serverSocketChannel) {

        // create a thread pool for incoming connections according to configuration
        final ExecutorService connectionExecutorService = Executors.newFixedThreadPool(this.getMaxConnections());

        while (this.running.get()) {
            try {
                // wait for incoming connections
                final SocketChannel client = serverSocketChannel.accept();

                LOG.debug("Processing connection from {}", client.getRemoteAddress());

                this.setKeepAlive(client);

                // set to blocking
                client.configureBlocking(true);
                // dispatch the incoming connection to the thread pool
                this.dispatchClientConnection(client, connectionExecutorService);

            } catch (final ClosedByInterruptException e) {
                LOG.info("Received interrupt signal, shutting down connection if closed");
                LOG.debug("Received interrupt exception", e);
            } catch (final IOException e) {
                throw new ServerRuntimeException("Error occured", e);
            }
        }
    }

    /**
     * Retrieves the max connection count from the connection options
     * 
     * @return
     */
    private Integer getMaxConnections() {
        return Integer.valueOf(this.connectionOptions.get(MAX_CONNECTIONS_OPTION));
    }

    /**
     * True, if a "keepAliveTimeout" property was configured
     * 
     * @return
     */
    private boolean isKeepAliveEnabled() {
        return this.connectionOptions.containsKey(KEEP_ALIVE_TIMEOUT_OPTION);
    }

    /**
     * Sets the keep alive socket option to the client if a keep alive timeout has been specified
     * 
     * @param client
     *            the client connection for which the keepalive option should be set
     * @throws IOException
     */
    private void setKeepAlive(final SocketChannel client) throws IOException {
        if (this.isKeepAliveEnabled()) {
            client.setOption(StandardSocketOptions.SO_KEEPALIVE, Boolean.TRUE);
        } else {
            client.setOption(StandardSocketOptions.SO_KEEPALIVE, Boolean.FALSE);
        }
    }

    /**
     * Dispatches the client connection to a worker.
     * 
     * @param client
     * @param connectionExecutorService
     * @throws IOException
     */
    private void dispatchClientConnection(final SocketChannel client, final ExecutorService connectionExecutorService)
            throws IOException {

        // open a new message channel to process messages from the client
        final MessageChannel serverChannel = this.openMessageChannel();

        // create a new worker for the incoming connection
        final Runnable worker = new MessageWorker(client, serverChannel);

        // and dispatch it to the thread pool
        connectionExecutorService.execute(worker);
    }

    /**
     * Opens a new message channel using the protocol of the port. The channel's option are set according to the
     * configuration of the connection handler.
     * 
     * @return the new message channel
     */
    private MessageChannel openMessageChannel() {
        final MessageChannel serverChannel = this.port.getProtocol().openChannel();
        for (final MessageChannelOption<?> option : serverChannel.getSupportedOptions()) {
            if (this.connectionOptions.containsKey(option.name())) {
                serverChannel.setOption(option, option.fromString(this.connectionOptions.get(option.name())));
            }
        }
        return serverChannel;
    }

    /**
     * Initializes the channel by binding it to the specified address and setting it to blocking mode
     * 
     * @param serverSocketChannel
     * @param bindAddress
     * @throws IOException
     */
    private void initializeChannel(final ServerSocketChannel serverSocketChannel, final SocketAddress bindAddress)
            throws IOException {
        serverSocketChannel.bind(bindAddress);
        LOG.info("Listening on {}", bindAddress);
        serverSocketChannel.configureBlocking(true);
    }

    @Override
    public void configure(final HierarchicalConfiguration config) throws ConfigurationException {
        // set defaults
        this.connectionOptions.put(MAX_CONNECTIONS_OPTION, Integer.toString(DEFAULT_MAX_CONNECTIONS));
        // override default with settings from configuration
        for (final Iterator<String> keyIt = config.getKeys(); keyIt.hasNext();) {
            final String key = keyIt.next();
            this.connectionOptions.put(key, config.getString(key));
        }
    }

    @Override
    public void bind(final Port port) {
        this.port = port;
    }

    /**
     * @throws IOException
     */
    @Override
    public void close() throws IOException {
        this.running.set(false);

    }

}
