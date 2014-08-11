/**
 * 
 */
package li.moskito.awtt.server.handler;

import li.moskito.awtt.protocol.http.HTTP;
import li.moskito.awtt.protocol.http.Request;
import li.moskito.awtt.protocol.http.Response;
import li.moskito.awtt.protocol.http.StatusCodes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract implementation of the request handler that support the commands of the HTTP protocol. Depending on the
 * command an event method (onXXX) is invoked. The default implementation of each of these event method is to return a
 * Not Implemented response. The handler does accept all requests per default.
 * 
 * @author Gerald
 */
public abstract class HttpRequestHandler implements MessageHandler<Request, Response> {

    /**
     * SLF4J Logger for this class
     */
    private static final Logger LOG = LoggerFactory.getLogger(HttpRequestHandler.class);

    @Override
    public boolean accepts(final Request request) {
        return true;
    }

    @Override
    public Response process(final Request request) {

        switch (request.getCommand()) {
            case GET:
                return this.onGet(request);
            case POST:
                return this.onPost(request);
            case PUT:
                return this.onPut(request);
            case DELETE:
                return this.onDelete(request);
            case HEAD:
                return this.onHead(request);
            case OPTIONS:
                return this.onOptions(request);
            case CONNECT:
                return this.onConnect(request);
            case TRACE:
                return this.onTrace(request);
            default:
                LOG.warn("Unsuppported Command '{}'", request.getCommand());
                return HTTP.createResponse(StatusCodes.BAD_REQUEST);
        }
    }

    /**
     * @param request
     * @return
     */
    protected Response onGet(final Request request) {
        return HTTP.createResponse(StatusCodes.NOT_IMPLEMENTED);

    }

    /**
     * @param request
     * @return
     */
    protected Response onPost(final Request request) {
        return HTTP.createResponse(StatusCodes.NOT_IMPLEMENTED);
    }

    /**
     * @param request
     * @return
     */
    protected Response onPut(final Request request) {
        return HTTP.createResponse(StatusCodes.NOT_IMPLEMENTED);
    }

    /**
     * @param request
     * @return
     */
    protected Response onDelete(final Request request) {
        return HTTP.createResponse(StatusCodes.NOT_IMPLEMENTED);
    }

    /**
     * @param request
     * @return
     */
    protected Response onHead(final Request request) {
        return HTTP.createResponse(StatusCodes.NOT_IMPLEMENTED);
    }

    /**
     * @param request
     * @return
     */
    protected Response onOptions(final Request request) {
        return HTTP.createResponse(StatusCodes.NOT_IMPLEMENTED);
    }

    /**
     * @param request
     * @return
     */
    protected Response onConnect(final Request request) {
        return HTTP.createResponse(StatusCodes.NOT_IMPLEMENTED);
    }

    /**
     * @param request
     * @return
     */
    protected Response onTrace(final Request request) {
        return HTTP.createResponse(StatusCodes.NOT_IMPLEMENTED);
    }

}
