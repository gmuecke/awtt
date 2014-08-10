/**
 * 
 */
package li.moskito.awtt.server;

import static li.moskito.awtt.protocol.http.ResponseHeaderFieldDefinitions.CONTENT_LENGTH;
import static li.moskito.awtt.protocol.http.ResponseHeaderFieldDefinitions.CONTENT_TYPE;
import static li.moskito.awtt.protocol.http.ResponseHeaderFieldDefinitions.LAST_MODIFIED;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.StringTokenizer;

import li.moskito.awtt.common.ContentType;
import li.moskito.awtt.common.ContentTypes;
import li.moskito.awtt.protocol.http.Entity;
import li.moskito.awtt.protocol.http.Request;
import li.moskito.awtt.protocol.http.Response;
import li.moskito.awtt.protocol.http.StatusCodes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gerald
 */
public class StaticFileContentRequestHandler implements RequestHandler {

    /**
     * SLF4J Logger for this class
     */
    private static final Logger LOG = LoggerFactory.getLogger(StaticFileContentRequestHandler.class);

    private final Path contentRoot;

    private String indexFileName;

    private final static String HTTP_DATE_FORMAT = "EEE, d MMM yyy HH:mm:ss zzz";

    // Thread safe date formatter
    private static final ThreadLocal<SimpleDateFormat> HTTP_DATE_FORMATTER = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.ENGLISH);
        }
    };

    /**
     * 
     */
    public StaticFileContentRequestHandler() {

        try {
            // TODO make the base path configurable
            this.contentRoot = Paths.get(new URI("file:/c:/http/htdocs/"));
            // TODO make index file name configurable
            this.indexFileName = "index.html";
            LOG.info("Serving files from content root {}", this.contentRoot);

        } catch (final URISyntaxException e) {
            throw new RuntimeException("Invalid ContentRoot");
        }
    }

    @Override
    public boolean accepts(final Request request) {
        return true;
    }

    @Override
    public Response process(final Request request) {

        switch (request.getCommand()) {
            case GET:
                final URI resource = request.getResource();
                LOG.debug("Requested to read resource {}", resource);
                final StringTokenizer tok = new StringTokenizer(resource.getPath(), "/");
                final String[] pathElements = new String[tok.countTokens()];
                int i = 0;
                while (tok.hasMoreTokens()) {
                    pathElements[i++] = tok.nextToken();
                }
                // TODO sanitize resource path to prevent sandbox escape
                final Path resourcePath = Paths.get(this.contentRoot.toString(), pathElements);
                LOG.debug("Resolved ResourcePath {}", resourcePath);

                try {
                    final BasicFileAttributes attrs = Files.readAttributes(resourcePath, BasicFileAttributes.class);
                    final Path fileResourcePath;

                    if (attrs.isDirectory()) {
                        fileResourcePath = resourcePath.resolve(this.indexFileName);
                        // TODO add support of listing directory contents if index file does not exist
                    } else {
                        fileResourcePath = resourcePath;
                    }

                    if (Files.isRegularFile(fileResourcePath)) {

                        return this.createFileContentResponse(fileResourcePath, attrs);

                    }
                } catch (final IOException e) {
                    LOG.error("Error reading resource {}", resourcePath, e);
                    return new Response(StatusCodes.SERVER_ERR_500_INTERNAL_SERVER_ERROR);
                }
                return new Response(StatusCodes.CLIENT_ERR_404_NOT_FOUND);
            default:
                LOG.warn("Unsuppported Command '{}'", request.getCommand());

        }
        return null;
    }

    /**
     * Creates a response for returning the content of a file
     * 
     * @param fileResourcePath
     *            the path to the file resource whose content should be returned to the client
     * @param attrs
     * @return
     * @throws IOException
     */
    private Response createFileContentResponse(final Path fileResourcePath, final BasicFileAttributes attrs)
            throws IOException {
        final Response response = new Response(StatusCodes.SUCCESSFUL_200_OK);

        final ContentType contentType = this.getContentType(fileResourcePath);

        response.setEntity(new Entity(Files.newByteChannel(fileResourcePath)));

        response.addField(CONTENT_TYPE, contentType);
        response.addField(LAST_MODIFIED, this.getLastModified(fileResourcePath));
        response.addField(CONTENT_LENGTH, attrs.size());

        return response;
    }

    /**
     * Determines the Content Type for the given file resource
     * 
     * @param fileResourcePath
     *            the path to the file resource
     * @return the contentType for the filetype
     */
    private ContentType getContentType(final Path fileResourcePath) {

        final String filename = fileResourcePath.getFileName().toString();
        return ContentTypes.byFileName(filename);

    }

    /**
     * Determines the last modified date and returns the date as String in HTTP Date format.
     * 
     * @param fileResourcePath
     *            the path to the file resource
     * @return the String representing the last modified date
     * @throws IOException
     */
    private String getLastModified(final Path fileResourcePath) throws IOException {
        final Date modifiedTime = new Date(Files.getLastModifiedTime(fileResourcePath).toMillis());
        final String modifiedTimeString = HTTP_DATE_FORMATTER.get().format(modifiedTime);
        return modifiedTimeString;
    }
}
