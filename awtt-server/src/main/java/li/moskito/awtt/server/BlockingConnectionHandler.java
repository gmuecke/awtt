/**
 * 
 */
package li.moskito.awtt.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import li.moskito.awtt.common.Configurable;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gerald
 */
public class BlockingConnectionHandler implements ConnectionHandler, Configurable {

    /**
     * SLF4J Logger for this class
     */
    private static final Logger LOG = LoggerFactory.getLogger(BlockingConnectionHandler.class);

    private Port port;
    private final AtomicBoolean running = new AtomicBoolean(true);

    private ConnectionHandlerParameters connectionControl = ConnectionHandlerParameters.DEFAULT_CONNECTION_PARAMS;

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
            throw new RuntimeException(e);
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
        final ExecutorService connectionExecutorService = Executors.newFixedThreadPool(this.connectionControl
                .getMaxConnections());

        while (this.running.get()) {
            try {
                // wait for incoming connections
                final SocketChannel client = serverSocketChannel.accept();

                if (this.connectionControl.getKeepAliveTimeout() != 0) {
                    client.setOption(StandardSocketOptions.SO_KEEPALIVE, Boolean.TRUE);
                }

                // set to blocking
                client.configureBlocking(true);
                // dispatch the incoming connection to the thread pool
                this.dispatchClientConnection(client, connectionExecutorService);

            } catch (final IOException e) {
                throw new RuntimeException("Error occured", e);
            }
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

        // create a new worker for the incoming connection
        final Runnable worker = new MessageWorker(client, this.port, this.connectionControl);

        // and dispatch it to the thread pool
        connectionExecutorService.execute(worker);
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
        //@formatter:off
        this.connectionControl = new ConnectionHandlerParameters(
                config.getInteger("maxConnections", ConnectionHandlerParameters.DEFAULT_MAX_CONNECTIONS), 
                config.getInteger("keepAlive", ConnectionHandlerParameters.UNLIMITED),
                config.getInteger("maxMessagesPerConnection", ConnectionHandlerParameters.UNLIMITED));
        //@formatter:on
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
