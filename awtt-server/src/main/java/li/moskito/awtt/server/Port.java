/**
 * 
 */
package li.moskito.awtt.server;

import java.net.InetAddress;

import li.moskito.awtt.protocol.Protocol;

/**
 * A Port relates to a TCP port the server is listening at. It is identified by a hostname and a port number. Multiple
 * {@link RequestHandler}s can be assigned to a port, that provide processing capabilities.
 * 
 * @author Gerald
 */
public class Port {

    private final int port;
    private final InetAddress hostname;
    private final Protocol protocol;

    /**
     * Creates a new port with the default port of the protocol
     * 
     * @param hostname
     *            the hostname binding for the port
     * @param protocol
     *            the protocol to accept on the port
     */
    public Port(final InetAddress hostname, final Protocol protocol) {
        this(hostname, protocol.getDefaultPort(), protocol);
    }

    /**
     * @param hostname
     * @param port
     */
    public Port(final InetAddress hostname, final int port, final Protocol protocol) {
        super();
        this.hostname = hostname;
        this.port = port;
        this.protocol = protocol;
    }

    public int getPortNumber() {
        return this.port;
    }

    public InetAddress getHostname() {
        return this.hostname;
    }

    public Protocol getProtocol() {
        return this.protocol;
    }

}
