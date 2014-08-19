/**
 * 
 */
package li.moskito.awtt.protocol;

import java.util.List;


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

    /**
     * Determines if the channels should be closed after the request has been processed
     * 
     * @param request
     *            the request that might contain information from the client whether to keep the connection open or no
     * @return <code>true</code> if the channels should be closed after the request, <code>false</code> if they should
     *         remain open
     */
    boolean isCloseChannelsAfterProcess(Message request);

    /**
     * Creates keep alive header information using the specified parameters
     * 
     * @param connectionControl
     * @return a list of header fields that can be added to a response in order to inform the receiver on how to handle
     *         the connection
     */
    List<HeaderField> getKeepAliverHeaders(ConnectionAttributes connectionControl);
}
