/**
 * 
 */
package li.moskito.awtt.server.handler;

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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

import li.moskito.awtt.protocol.http.Commands;
import li.moskito.awtt.protocol.http.ContentType;
import li.moskito.awtt.protocol.http.Entity;
import li.moskito.awtt.protocol.http.HTTP;
import li.moskito.awtt.protocol.http.Message;
import li.moskito.awtt.protocol.http.Request;
import li.moskito.awtt.protocol.http.RequestHeaderFieldDefinitions;
import li.moskito.awtt.protocol.http.Response;
import li.moskito.awtt.protocol.http.StatusCodes;
import li.moskito.awtt.server.Configurable;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gerald
 */
public class StaticFileContentRequestHandler extends HttpRequestHandler implements Configurable {

    /**
     * SLF4J Logger for this class
     */
    private static final Logger LOG = LoggerFactory.getLogger(StaticFileContentRequestHandler.class);

    private Path contentRoot;

    private final String indexFileName;

    private final static String HTTP_DATE_FORMAT = "EEE, d MMM yyy HH:mm:ss zzz";

    private final Map<String, ContentType> contentTypes;

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
        // TODO make index file name configurable
        this.indexFileName = "index.html";
        this.contentTypes = new ConcurrentHashMap<>();

    }

    @Override
    public boolean accepts(final Request request) {
        return request.getCommand() == Commands.GET;
    }

    @Override
    protected Response onGet(final Request request) {
        final URI resource = request.getResource();
        LOG.debug("Requested to read resource {}", resource);
        final StringTokenizer tok = new StringTokenizer(resource.getPath(), "/");
        final String[] pathElements = new String[tok.countTokens()];
        int i = 0;
        while (tok.hasMoreTokens()) {
            pathElements[i++] = tok.nextToken();
        }
        // TODO AWTT-7 sanitize resource path to prevent sandbox escape
        final Path resourcePath = Paths.get(this.contentRoot.toString(), pathElements);
        LOG.debug("Resolved ResourcePath {}", resourcePath);

        try {
            final BasicFileAttributes attrs = Files.readAttributes(resourcePath, BasicFileAttributes.class);
            final Path fileResourcePath;

            if (attrs.isDirectory()) {
                fileResourcePath = resourcePath.resolve(this.indexFileName);
                // TODO AWTT-13 add support of listing directory contents if index file does not exist
            } else {
                fileResourcePath = resourcePath;
            }

            if (Files.isRegularFile(fileResourcePath)) {
                if (this.isModified(request, fileResourcePath)) {
                    return this.createFileContentResponse(fileResourcePath, attrs);
                } else {
                    return HTTP.createResponse(StatusCodes.NOT_MODIFIED);
                }

            }
        } catch (final IOException e) {
            LOG.error("Error reading resource {}", resourcePath, e);
            return new Response(StatusCodes.INTERNAL_SERVER_ERROR);
        }
        return new Response(StatusCodes.NOT_FOUND);
    }

    /**
     * Determines if the file is modified according to the If-Modified-Since header
     * 
     * @param request
     *            the request containing the headers
     * @param fileResourcePath
     *            the fileResource that should be checked regarding modification date
     * @return <code>true</code> if the file was modified since the date in the request
     * @throws IOException
     */
    private boolean isModified(final Message request, final Path fileResourcePath) throws IOException {

        if (request.hasField(RequestHeaderFieldDefinitions.IF_MODIFIED_SINCE)) {

            final String date = request.getField(RequestHeaderFieldDefinitions.IF_MODIFIED_SINCE).getValue();
            try {
                // TODO fix is modified handling
                final Date ifModifiedDateDate = HTTP_DATE_FORMATTER.get().parse(date);
                final Date systemDate = new Date(System.currentTimeMillis());
                final Date modifiedDate = this.getLastModifiedDate(fileResourcePath);

                return ifModifiedDateDate.after(systemDate) || ifModifiedDateDate.before(modifiedDate);

            } catch (final ParseException e) {
                LOG.debug("Could not parse date {}", date, e);
            }
        }
        return true;
    }

    @Override
    public void configure(final HierarchicalConfiguration config) throws ConfigurationException {

        try {
            this.contentRoot = Paths.get(new URI(config.getString("contentRoot")));
            LOG.info("Serving files from content root {}", this.contentRoot);

            final List<HierarchicalConfiguration> contentTypeConfigs = config.configurationsAt("contentTypes/type");
            for (final HierarchicalConfiguration contentTypeConfig : contentTypeConfigs) {
                final ContentType contentType = new ContentType(contentTypeConfig.getString("@mimeType"));
                final String fileExtension = contentTypeConfig.getString("@fileExtension");
                this.contentTypes.put(fileExtension, contentType);
            }

        } catch (final URISyntaxException e) {
            throw new ConfigurationException("ContentRoot not valid");
        }
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
        final Response response = new Response(StatusCodes.OK);

        response.setEntity(new Entity(Files.newByteChannel(fileResourcePath)));

        response.addField(LAST_MODIFIED, this.getLastModified(fileResourcePath));
        response.addField(CONTENT_LENGTH, attrs.size());
        final ContentType contentType = this.getContentType(fileResourcePath);
        if (contentType != null) {
            response.addField(CONTENT_TYPE, contentType);
        }

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
        final String extension = this.getFileExtension(filename);
        if (extension != null && this.contentTypes.containsKey(extension)) {
            return this.contentTypes.get(extension);
        }
        return null;

    }

    /**
     * Determines the file extension for the given filename
     * 
     * @param filename
     * @return the file extension or <code>null</code> if the file has no extension
     */
    private String getFileExtension(final String filename) {
        final int extensionSeparator = filename.lastIndexOf('.');
        if (extensionSeparator != -1) {
            return filename.substring(extensionSeparator + 1);
        }
        return null;
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
        final Date modifiedTime = this.getLastModifiedDate(fileResourcePath);
        return HTTP_DATE_FORMATTER.get().format(modifiedTime);
    }

    /**
     * Determines the date of the last modification of the specified Resource
     * 
     * @param path
     *            the resource to check
     * @return the date of the last modification
     * @throws IOException
     */
    private Date getLastModifiedDate(final Path path) throws IOException {
        return new Date(Files.getLastModifiedTime(path).toMillis());
    }
}
