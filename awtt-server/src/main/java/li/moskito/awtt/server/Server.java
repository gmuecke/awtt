/**
 * 
 */
package li.moskito.awtt.server;

import li.moskito.awtt.common.Configurable;

/**
 * Interface for AWTT servers
 * 
 * @author Gerald
 */
public interface Server extends Configurable {

    /**
     * Starts the server
     */
    void startServer();

    /**
     * Stops the server
     */
    void stopServer();

    /**
     * True, if the server is running.
     * 
     * @return
     */
    boolean isRunning();

}
