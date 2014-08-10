/**
 * 
 */
package li.moskito.awtt.server;

import li.moskito.awtt.protocol.http.Request;
import li.moskito.awtt.protocol.http.Response;

/**
 * @author Gerald
 */
public interface RequestHandler {

    /**
     * Verifies if the handler does accept the request for processing
     * 
     * @param request
     *            the request to check
     * @return <code>true</code> if the handler is able to process the request, <code>false</code> if not
     */
    boolean accepts(Request request);

    /**
     * Processes the request and creating a response for it
     * 
     * @param request
     *            the request to process
     * @return a http response to be returned to the client
     */
    Response process(Request request);
}
