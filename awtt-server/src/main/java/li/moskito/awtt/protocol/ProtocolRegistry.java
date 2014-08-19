/**
 * 
 */
package li.moskito.awtt.protocol;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central registry for protocol instances. Every protocol is registered with a different name. The name merely acts as
 * identifier so that multiple protocol implementations of the same protocol can be registered using a different name
 * for each.
 * 
 * @author Gerald
 */
public final class ProtocolRegistry {

    /**
     * Holder idiom
     */
    private static class Holder {
        private static final ProtocolRegistry INSTANCE = new ProtocolRegistry();
        private Holder() {
        }
    }

    private final Map<String, Protocol> protocols;

    /**
     * Singleton constructor
     */
    private ProtocolRegistry() {
        this.protocols = new ConcurrentHashMap<>();
    
    }

    /**
     * Checks if a specific protocol is available
     * 
     * @param protocolName
     *            the name of the registered protocol
     * @return <code>true</code> if a protocol instance is available
     */
    public boolean isAvailable(final String protocolName) {
        return this.protocols.containsKey(protocolName);
    }

    /**
     * Retrieves the protocol instance for a specific name.
     * 
     * @param protocolName
     *            the name of the protocol to retrieve
     * @return the protocol instance or <code>null</code> if no instance was found for the name
     */
    public Protocol forName(final String protocolName) {
        return this.protocols.get(protocolName);
    }

    /**
     * Registers a protocol under the specified name.
     * 
     * @param name
     *            the name that should be used for the registration an later retrieval
     * @param protocol
     *            the protocol to be registered
     */
    public void registerProtocol(final String name, final Protocol protocol) {
        if (this.protocols.containsKey(name)) {
            throw new IllegalStateException("Protocol '" + name + "' already registered");
        }
        this.protocols.put(name, protocol);
    }

    /**
     * Liefert eine Singleton Instanz.
     * 
     * @return
     */
    public static ProtocolRegistry getInstance() {
        return Holder.INSTANCE;
    }

}
