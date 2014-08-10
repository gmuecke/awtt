/**
 * 
 */
package li.moskito.awtt.server.handler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import li.moskito.awtt.server.Configurable;
import li.moskito.awtt.server.Port;
import li.moskito.awtt.server.RequestWorker;

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

    private int maxConnections = 5; // default
    private Port port;

    @Override
    public void run() {

        final ExecutorService connectionExecutorService = Executors.newFixedThreadPool(this.maxConnections);

        final SocketAddress bindAddress = new InetSocketAddress(this.port.getHostname(), this.port.getPortNumber());

        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {

            serverSocketChannel.bind(bindAddress);
            LOG.info("Listening on {}", bindAddress);
            serverSocketChannel.configureBlocking(true);

            // event loop
            while (true) { // TODO add shutdown option

                // wait for incoming connections
                SocketChannel client;
                try {
                    client = serverSocketChannel.accept();
                    // set to blocking
                    client.configureBlocking(true);
                    // create a new worker for the incoming connection
                    final Runnable worker = new RequestWorker(client, this.port.getRequestHandlers());
                    // and dispatch it to the thread pool
                    connectionExecutorService.execute(worker);

                } catch (final IOException e) {
                    throw new RuntimeException("Error occured", e);
                }
            }

        } catch (final IOException e) {
            LOG.error("Could not create server socket", e);
            throw new RuntimeException("Unable to set server socket to blocking mode", e);
        }
    }

    @Override
    public void configure(final HierarchicalConfiguration config) throws ConfigurationException {
        this.maxConnections = config.getInt("maxConnections");
    }

    @Override
    public void bind(final Port port) {
        this.port = port;
    }

}
