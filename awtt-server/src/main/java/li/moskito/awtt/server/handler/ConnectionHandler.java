/**
 * 
 */
package li.moskito.awtt.server.handler;

import li.moskito.awtt.server.Port;

/**
 * @author Gerald
 */
public interface ConnectionHandler extends Runnable {

    /**
     * Associates this connection handler with the given port. Both have a bidirectional relation with each other
     * 
     * @param port
     */
    void bind(Port port);
}
