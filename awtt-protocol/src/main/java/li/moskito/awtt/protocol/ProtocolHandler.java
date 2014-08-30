/**
 * 
 */
package li.moskito.awtt.protocol;

/**
 * A handler to process messages of a protocol.
 * 
 * @author Gerald
 */
public interface ProtocolHandler<I extends Message, O extends Message> {

    /**
     * Verifies if the handler does accept the request for processing
     * 
     * @param request
     *            the request to check
     * @return <code>true</code> if the handler is able to process the request, <code>false</code> if not
     */
    boolean accepts(I request);

    /**
     * Processes the incoming message and creates an outgoing message
     * 
     * @param request
     *            the request to process
     * @return a http response to be returned to the client
     */
    O process(I request);

}
