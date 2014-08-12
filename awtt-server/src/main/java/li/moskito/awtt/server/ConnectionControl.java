/**
 * 
 */
package li.moskito.awtt.server;

/**
 * @author Gerald
 */
public class ConnectionControl {

    /**
     * Value to be used for unlimited parameters (-1)
     */
    public static final int UNLIMITED = -1;
    /**
     * Value to be used to inidicate a value is none (no timeout, no connection, etc) (0)
     */
    public static final int NONE = 0;

    /**
     * The default number of maximum simultaneous connections. (Default: 1)
     */
    public static final int DEFAULT_MAX_CONNECTIONS = 1;

    /**
     * Default connection timeout in seconds (Default: unlimited). After that time, if no data was sent over the server,
     * the connection will be closed.
     */
    public static final int DEFAULT_CONNECTION_TIMEOUT = UNLIMITED;

    /**
     * Default number of message processed on a connection before the connection is closed. Default is (Unlimited)
     */
    public static final int DEFAULT_MAX_MESSAGES_PER_CONNECTION = UNLIMITED;

    public static final ConnectionControl DEFAULT_CONNECTION_CONTROL = new ConnectionControl(DEFAULT_MAX_CONNECTIONS,
            DEFAULT_CONNECTION_TIMEOUT, DEFAULT_MAX_MESSAGES_PER_CONNECTION);

    private final int maxConnections;
    private final int keepAliveTimeout;
    private final int maxMessagesPerConnection;

    /**
     * Creates a new connection control
     * 
     * @param maxConnections
     *            number of maximum connections
     * @param keepAliveTimeout
     *            number of seconds for a connection to be kept alive
     * @param maxMessagesPerConnection
     *            number of messages processed for a connection before it closes
     */
    public ConnectionControl(final int maxConnections, final int keepAliveTimeout, final int maxMessagesPerConnection) {
        super();
        this.maxConnections = maxConnections;
        this.keepAliveTimeout = keepAliveTimeout;
        this.maxMessagesPerConnection = maxMessagesPerConnection;
    }

    public int getMaxConnections() {
        return this.maxConnections;
    }

    public int getKeepAliveTimeout() {
        return this.keepAliveTimeout;
    }

    public int getMaxMessagesPerConnection() {
        return this.maxMessagesPerConnection;
    }

}
