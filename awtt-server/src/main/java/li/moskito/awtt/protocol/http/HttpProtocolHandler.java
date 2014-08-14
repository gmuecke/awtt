/**
 * 
 */
package li.moskito.awtt.protocol.http;

import java.util.Date;

import li.moskito.awtt.protocol.ProtocolHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Protocol Handler for the HTTP protocol. The handler invokes a onXXX event method upon the receipt of a http request
 * with specific command where each command is mapped to one of the onXXX event method.
 * 
 * @author Gerald
 */
public class HttpProtocolHandler implements ProtocolHandler<HttpRequest, HttpResponse> {

    /**
     * SLF4J Logger for this class
     */
    private static final Logger LOG = LoggerFactory.getLogger(HttpProtocolHandler.class);

    @Override
    public boolean accepts(final HttpRequest httpRequest) {
        return true;
    }

    @Override
    public HttpResponse process(final HttpRequest httpRequest) {

        final HttpResponse response;
        switch (httpRequest.getCommand()) {
            case GET:
                response = this.onGet(httpRequest);
                break;
            case POST:
                response = this.onPost(httpRequest);
                break;
            case PUT:
                response = this.onPut(httpRequest);
                break;
            case DELETE:
                response = this.onDelete(httpRequest);
                break;
            case HEAD:
                response = this.onHead(httpRequest);
                break;
            case OPTIONS:
                response = this.onOptions(httpRequest);
                break;
            case CONNECT:
                response = this.onConnect(httpRequest);
                break;
            case TRACE:
                response = this.onTrace(httpRequest);
                break;
            default:
                LOG.warn("Unsuppported Command '{}'", httpRequest.getCommand());
                response = HTTP.createResponse(HttpStatusCodes.BAD_REQUEST);
                break;
        }

        return this.addMandatoryHeaders(response);
    }

    /**
     * Adds header fields to the response that are mandatory for each request
     * 
     * @param response
     *            the response to be enriched
     * @return the modified response
     */
    private HttpResponse addMandatoryHeaders(final HttpResponse response) {
        response.addField(ResponseHeaders.DATE, HTTP.toHttpDate(new Date()));
        return response;
    }

    /**
     * @param httpRequest
     * @return
     */
    protected HttpResponse onGet(final HttpRequest httpRequest) {
        return HTTP.createResponse(HttpStatusCodes.NOT_IMPLEMENTED);

    }

    /**
     * @param httpRequest
     * @return
     */
    protected HttpResponse onPost(final HttpRequest httpRequest) {
        return HTTP.createResponse(HttpStatusCodes.NOT_IMPLEMENTED);
    }

    /**
     * @param httpRequest
     * @return
     */
    protected HttpResponse onPut(final HttpRequest httpRequest) {
        return HTTP.createResponse(HttpStatusCodes.NOT_IMPLEMENTED);
    }

    /**
     * @param httpRequest
     * @return
     */
    protected HttpResponse onDelete(final HttpRequest httpRequest) {
        return HTTP.createResponse(HttpStatusCodes.NOT_IMPLEMENTED);
    }

    /**
     * @param httpRequest
     * @return
     */
    protected HttpResponse onHead(final HttpRequest httpRequest) {
        return HTTP.createResponse(HttpStatusCodes.NOT_IMPLEMENTED);
    }

    /**
     * @param httpRequest
     * @return
     */
    protected HttpResponse onOptions(final HttpRequest httpRequest) {
        return HTTP.createResponse(HttpStatusCodes.NOT_IMPLEMENTED);
    }

    /**
     * @param httpRequest
     * @return
     */
    protected HttpResponse onConnect(final HttpRequest httpRequest) {
        return HTTP.createResponse(HttpStatusCodes.NOT_IMPLEMENTED);
    }

    /**
     * @param httpRequest
     * @return
     */
    protected HttpResponse onTrace(final HttpRequest httpRequest) {
        return HTTP.createResponse(HttpStatusCodes.NOT_IMPLEMENTED);
    }

}
