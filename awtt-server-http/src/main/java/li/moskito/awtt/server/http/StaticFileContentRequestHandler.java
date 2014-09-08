/**
 * 
 */
package li.moskito.awtt.server.http;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

import li.moskito.awtt.protocol.http.HTTP;
import li.moskito.awtt.protocol.http.HTTP.ResponseOptions;
import li.moskito.awtt.protocol.http.HttpCommands;
import li.moskito.awtt.protocol.http.HttpRequest;
import li.moskito.awtt.protocol.http.HttpResponse;
import li.moskito.awtt.protocol.http.HttpStatusCodes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A HTTP Request Handler that serves static files of a directory. If the requested resource is a directory the handler
 * returns its index file if existing. The handler does not provide directory listings. The handler does only serve GET
 * requests.
 * 
 * @author Gerald
 */
public class StaticFileContentRequestHandler extends FileResourceRequestHandler {

    /**
     * SLF4J Logger for this class
     */
    static final Logger LOG = LoggerFactory.getLogger(StaticFileContentRequestHandler.class);

    /**
     * Accepts GET requests to an existing file or directory with an index file
     */
    @Override
    public boolean accepts(final HttpRequest httpRequest) {
        final boolean commandSupported = httpRequest.getCommand() == HttpCommands.GET;
        final boolean resourceValid = this.isFileResource(httpRequest.getResource());
        return commandSupported && resourceValid;
    }

    @Override
    protected HttpResponse onGet(final HttpRequest httpRequest) {
        final URI resource = httpRequest.getResource();
        LOG.debug("Requested to read resource {}", resource);
        final Path resourcePath = this.resolveFileResource(resource);

        if (Files.isRegularFile(resourcePath)) {
            return this.createFileResponse(httpRequest, resourcePath);
        } else {
            return HTTP.createResponse(HttpStatusCodes.NOT_FOUND, ResponseOptions.FORCE_CLOSE);
        }
    }
}
