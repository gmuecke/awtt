/**
 * 
 */
package li.moskito.awtt.server;

import java.net.InetAddress;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import li.moskito.awtt.server.handler.RequestHandler;

/**
 * A Port relates to a TCP port the server is listening at. It is identified by a hostname and a port number. Multiple
 * {@link RequestHandler}s can be assigned to a port, that provide processing capabilities.
 * 
 * @author Gerald
 */
public class Port {

    private final int port;
    private final InetAddress hostname;
    private final List<RequestHandler> requestHandlers;

    /**
     * @param hostname
     * @param port
     */
    public Port(final InetAddress hostname, final int port) {
        super();
        this.hostname = hostname;
        this.port = port;
        this.requestHandlers = new CopyOnWriteArrayList<>();
    }

    public int getPortNumber() {
        return this.port;
    }

    public InetAddress getHostname() {
        return this.hostname;
    }

    public List<RequestHandler> getRequestHandlers() {
        return Collections.unmodifiableList(this.requestHandlers);
    }

    public void addRequestHandler(final RequestHandler handler) {
        this.requestHandlers.add(handler);
    }

    /**
     * @param requestHandlers
     */
    public void addRequestHandlers(final List<RequestHandler> requestHandlers) {
        this.requestHandlers.addAll(requestHandlers);
    }

}
