/**
 * 
 */
package li.moskito.awtt.protocol;

/**
 * Denotes the Version of a protocol
 * 
 * @author Gerald
 */
public interface ProtocolVersion {

    /**
     * A human readable representation of the version. The string also has to comply with text based protocol messages.
     * 
     * @return
     */
    String getVersion();
}
