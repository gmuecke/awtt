/**
 * 
 */
package li.moskito.awtt.protocol;


/**
 * A protocol defines a set of Messages to be exchanged between two communication partners and the semantic how those
 * messages and their content relate to each other.
 * 
 * @author Gerald
 */
public interface Protocol {

    /**
     * The default port for the protocol. Most protocols are bound to a specific protocol.
     * 
     * @return a port number
     */
    int getDefaultPort();

    /**
     * Opens a channel to communicate using the protocol.
     * 
     * @return a message channel over which messages could be read and written.
     */
    MessageChannel openChannel();

    /**
     * Processes the message.
     * 
     * @param message
     *            the message to be processed
     * @return the message that should be returned in response to the input message or <code>null</code> if there is no
     *         response
     */
    Message process(Message message);

}
