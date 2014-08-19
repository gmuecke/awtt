/**
 * 
 */
package li.moskito.awtt.server.http;

import static li.moskito.awtt.protocol.http.ResponseHeaders.CONTENT_LENGTH;
import static li.moskito.awtt.protocol.http.ResponseHeaders.CONTENT_TYPE;
import static li.moskito.awtt.protocol.http.ResponseHeaders.LAST_MODIFIED;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

import li.moskito.awtt.common.Configurable;
import li.moskito.awtt.protocol.BinaryBody;
import li.moskito.awtt.protocol.http.ContentType;
import li.moskito.awtt.protocol.http.HTTP;
import li.moskito.awtt.protocol.http.HttpCommands;
import li.moskito.awtt.protocol.http.HttpHeader;
import li.moskito.awtt.protocol.http.HttpMessage;
import li.moskito.awtt.protocol.http.HttpProtocolHandler;
import li.moskito.awtt.protocol.http.HttpRequest;
import li.moskito.awtt.protocol.http.HttpResponse;
import li.moskito.awtt.protocol.http.HttpStatusCodes;
import li.moskito.awtt.protocol.http.RequestHeaders;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gerald
 */
public class StaticFileContentRequestHandler extends HttpProtocolHandler implements Configurable {

    /**
     * SLF4J Logger for this class
     */
    private static final Logger LOG = LoggerFactory.getLogger(StaticFileContentRequestHandler.class);

    private Path contentRoot;

    private final String indexFileName;
    private final Map<String, ContentType> contentTypes;

    /**
     * 
     */
    public StaticFileContentRequestHandler() {
        // TODO make index file name configurable
        this.indexFileName = "index.html";
        this.contentTypes = new ConcurrentHashMap<>();

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
            throw new ConfigurationException("ContentRoot not valid", e);
        }
    }

    @Override
    public boolean accepts(final HttpRequest httpRequest) {
        return httpRequest.getCommand() == HttpCommands.GET;
    }

    @Override
    protected HttpResponse onGet(final HttpRequest httpRequest) {
        final URI resource = httpRequest.getResource();
        LOG.debug("Requested to read resource {}", resource);
        final StringTokenizer tok = new StringTokenizer(resource.getPath(), "/");

        // TODO sanitze request resource by removing elements that reduce the stack
        final String[] pathElements = new String[tok.countTokens()];
        int i = 0;
        while (tok.hasMoreTokens()) {
            pathElements[i++] = tok.nextToken();
        }
        // TODO AWTT-7 sanitize resource path to prevent sandbox escape
        final Path resourcePath = Paths.get(this.contentRoot.toString(), pathElements);

        try {
            if (Files.exists(resourcePath)) {

                final Path fileResourcePath;
                if (Files.isDirectory(resourcePath)) {
                    fileResourcePath = resourcePath.resolve(this.indexFileName);
                    // TODO AWTT-13 add support of listing directory contents if index file does not exist
                } else {
                    fileResourcePath = resourcePath;
                }

                LOG.debug("Resolved path to resource {}", fileResourcePath);
                if (Files.isRegularFile(fileResourcePath)) {

                    if (this.isModified(httpRequest, fileResourcePath)) {
                        return this.createFileContentResponse(fileResourcePath);
                    } else {
                        return HTTP.createResponse(HttpStatusCodes.NOT_MODIFIED);
                    }

                }
            }

        } catch (final IOException e) {
            LOG.error("Error reading resource {}", resourcePath, e);
            return new HttpResponse(HttpStatusCodes.INTERNAL_SERVER_ERROR);
        }
        return new HttpResponse(HttpStatusCodes.NOT_FOUND);
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
    private boolean isModified(final HttpMessage request, final Path fileResourcePath) throws IOException {
        final HttpHeader header = request.getHeader();
        if (header.hasField(RequestHeaders.IF_MODIFIED_SINCE)) {

            final String date = header.getField(RequestHeaders.IF_MODIFIED_SINCE).getValue();
            try {
                final Date ifModifiedDateDate = HTTP.fromHttpDate(date);
                final Date systemDate = new Date(System.currentTimeMillis());
                final Date modifiedDate = this.getLastModifiedDate(fileResourcePath);

                return ifModifiedDateDate.after(systemDate) || ifModifiedDateDate.before(modifiedDate);

            } catch (final ParseException e) {
                LOG.debug("Could not parse date {}", date, e);
            }
        }
        return true;
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
    private HttpResponse createFileContentResponse(final Path fileResourcePath) throws IOException {
        final BasicFileAttributes attrs = Files.readAttributes(fileResourcePath, BasicFileAttributes.class);
        final HttpResponse httpResponse = new HttpResponse(HttpStatusCodes.OK);

        httpResponse.setBody(new BinaryBody(Files.newByteChannel(fileResourcePath)));

        httpResponse.addField(LAST_MODIFIED, this.getLastModified(fileResourcePath));
        httpResponse.addField(CONTENT_LENGTH, attrs.size());
        final ContentType contentType = this.getContentType(fileResourcePath);
        if (contentType != null) {
            httpResponse.addField(CONTENT_TYPE, contentType);
        }

        return httpResponse;
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
        return HTTP.toHttpDate(modifiedTime);
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
        final long fileTimestamp = Files.getLastModifiedTime(path).toMillis();
        // we have to cut the ms part of the time (round down by 1000) as HTTP date doesn't have ms
        return new Date(fileTimestamp / 1000 * 1000);
    }
}
