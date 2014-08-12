/**
 * 
 */
package li.moskito.awtt.incubator;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import li.moskito.awtt.server.ConnectionControl;
import li.moskito.awtt.server.RequestWorker;
import li.moskito.awtt.server.handler.StaticFileContentRequestHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gerald
 */
public class NonBlockingNioWebServer {

    /**
     * SLF4J Logger for this class
     */
    private static final Logger LOG = LoggerFactory.getLogger(NonBlockingNioWebServer.class);

    public static void main(final String[] args) {

        final int port = 8080; // TODO make configurable
        final StaticFileContentRequestHandler handler = new StaticFileContentRequestHandler(); // TODO make configurable

        final SocketAddress bindAddress = new InetSocketAddress(port);

        // create the thread pool of 5 thread
        // TODO make threadpool configurable
        final ExecutorService executor = Executors.newFixedThreadPool(5);

        // implementation inspired by http://www.onjava.com/pub/a/onjava/2002/09/04/nio.html?page=2
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {

            serverSocketChannel.configureBlocking(false); // set to non-blocking
            serverSocketChannel.bind(bindAddress);
            LOG.info("Listening on {}", bindAddress);

            final Selector selector = Selector.open();
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            // event loop
            while (true) { // TODO add shutdown option
                selector.select(); // blocking

                final Set<SelectionKey> keys = selector.selectedKeys();

                LOG.info("Keys {}", keys);
                for (final Iterator<SelectionKey> keyIterator = keys.iterator(); keyIterator.hasNext();) {
                    final SelectionKey key = keyIterator.next();
                    keyIterator.remove();

                    // if isAccetable = true then a client required a connection
                    if (key.isAcceptable()) {
                        LOG.info("Key {} is acceptable", key);
                        // get client socket channel
                        final ServerSocketChannel server = (ServerSocketChannel) key.channel();
                        final SocketChannel client = server.accept();
                        client.configureBlocking(false);
                        client.register(selector, SelectionKey.OP_READ);
                    }
                    if (key.isReadable()) {
                        LOG.info("Key {} is readable", key);
                        // dispatch the processing of a client to a separate thread
                        final SocketChannel client = (SocketChannel) key.channel();
                        final Runnable worker = new RequestWorker(client, (List) Arrays.asList(handler),
                                ConnectionControl.DEFAULT_CONNECTION_CONTROL);
                        // executor.execute(worker);
                        worker.run();
                    }
                }
            }

        } catch (final IOException e) {
            LOG.error("Could not create server socket", e);
        }

        executor.shutdown();

    }
}
