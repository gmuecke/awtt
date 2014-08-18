/**
 * 
 */
package li.moskito.awtt.server;

import java.io.IOException;

/**
 * Connection handler are bound to a {@link Port} and process incoming connections.
 * 
 * @author Gerald
 */
public interface ConnectionHandler extends Runnable {

    /**
     * Associates this connection handler with the given port. Both have a bidirectional relation with each other
     * 
     * @param port
     */
    void bind(Port port);

    /**
     * Closes the ConnectionHandler. Upon this call, no further connections are accepted
     * 
     * @throws IOException
     */
    void close() throws IOException;
}
