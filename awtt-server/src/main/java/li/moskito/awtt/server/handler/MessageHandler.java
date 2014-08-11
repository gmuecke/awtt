/**
 * 
 */
package li.moskito.awtt.server.handler;

import li.moskito.awtt.protocol.http.Message;

/**
 * A Handler that processes an incoming message and produces and outgoing message.
 * 
 * @author Gerald
 */
public interface MessageHandler<I extends Message<?>, O extends Message<?>> {

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
