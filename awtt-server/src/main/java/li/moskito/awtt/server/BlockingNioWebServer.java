/**
 * 
 */
package li.moskito.awtt.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * WebServer Implementation based on blocking java.nio Channels
 * 
 * @author Gerald
 */
public class BlockingNioWebServer {

    /**
     * SLF4J Logger for this class
     */
    private static final Logger LOG = LoggerFactory.getLogger(BlockingNioWebServer.class);

    public static void main(final String[] args) {

        final int port = 8080; // TODO make configurable
        final RequestHandler handler = new StaticFileContentRequestHandler(); // TODO make configurable
        // TODO add support for multiple handlers

        final SocketAddress bindAddress = new InetSocketAddress(port);

        // create the thread pool of 5 thread
        // TODO make threadpool configurable
        final ExecutorService executor = Executors.newFixedThreadPool(1);

        // implementation inspired by http://www.onjava.com/pub/a/onjava/2002/09/04/nio.html?page=2
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {

            serverSocketChannel.configureBlocking(true); // set to non-blocking
            serverSocketChannel.bind(bindAddress);
            LOG.info("Listening on {}", bindAddress);

            // event loop
            while (true) { // TODO add shutdown option

                // wait for incoming connections
                final SocketChannel client = serverSocketChannel.accept();
                client.configureBlocking(true);

                // create a new worker for the incoming connection
                final Runnable worker = new RequestWorker(client, handler);
                // and dispatch it to the thread pool
                executor.execute(worker);
            }

        } catch (final IOException e) {
            LOG.error("Could not create server socket", e);
        } finally {
            executor.shutdown();
        }

    }
}
