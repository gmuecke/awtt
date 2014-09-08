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
import java.util.ArrayDeque;
import java.util.Date;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

import li.moskito.awtt.common.Configurable;
import li.moskito.awtt.protocol.BinaryBody;
import li.moskito.awtt.protocol.http.ContentType;
import li.moskito.awtt.protocol.http.HTTP;
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
public class FileResourceRequestHandler extends HttpProtocolHandler implements Configurable {

    private static final Logger LOG = LoggerFactory.getLogger(FileResourceRequestHandler.class);

    private Path contentRoot;
    private String indexFileName;

    private final Map<String, ContentType> contentTypes;

    public FileResourceRequestHandler() {
        this.contentTypes = new ConcurrentHashMap<>();

    }

    @Override
    public void configure(final HierarchicalConfiguration config) throws ConfigurationException {

        try {
            this.contentRoot = Paths.get(new URI(config.getString("contentRoot")));
            LOG.info("Serving files from content root {}", this.contentRoot);
            this.indexFileName = config.getString("indexFile", "index.html");

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

    /**
     * Checks if the the URI points to a valid and existing file resource. If the resource points to a directory it is
     * still valid if the directory contains an index file
     * 
     * @param resourceId
     *            the resource to verify
     * @return <code>true</code> if the resource points to a file or a directory with an index file
     */
    protected boolean isFileResource(final URI resourceId) {
        final Path resourcePath = this.resolveFileResource(resourceId);
        return resourcePath != null && (Files.isRegularFile(resourcePath) || !resourceId.getPath().endsWith("/"));
    }

    /**
     * Resolves the URI resource to a local file resource that is identified by its {@link Path}
     * 
     * @param resource
     *            the resource to be resolved
     * @return the {@link Path} pointing to the local file or directory
     */
    protected Path resolveFileResource(final URI resource) {
        final Path resourcePath = this.normalizeResourcePath(resource);
        return this.resolveFileResourcePath(resourcePath);
    }

    /**
     * Resolves the given resource path to a file resource path. If the path points to a directory, the configured index
     * file will be used if a file with the configured index file name exists.
     * 
     * @param resourcePath
     *            the resource path to be resolved
     * @return the path to an existing resource in the filesystem. The resource is either a concrete file or a
     *         directory.
     */
    private Path resolveFileResourcePath(final Path resourcePath) {
        Path fileResourcePath = resourcePath;
        if (Files.isDirectory(resourcePath)) {
            final Path indexResourcePath = resourcePath.resolve(this.indexFileName);
            if (Files.exists(indexResourcePath)) {
                fileResourcePath = indexResourcePath;
            }
        }
        return fileResourcePath;
    }

    /**
     * Creates a HTTP Response serving the data from the given fileResourcePath. If the file was not modified according
     * to the information from the http request header, a 304 Not Modified will be returned, otherwise a 200 OK
     * 
     * @param httpRequest
     *            the http request containing information regarding the constraining modification date
     * @param fileResourcePath
     *            the file resource to server
     * @return the http response to be returned to the client
     * @throws IOException
     */
    protected HttpResponse createFileResponse(final HttpRequest httpRequest, final Path fileResourcePath) {
        try {
            if (this.isModified(httpRequest, fileResourcePath)) {
                return this.createFileContentResponse(fileResourcePath);
            } else {
                return HTTP.createResponse(HttpStatusCodes.NOT_MODIFIED);
            }
        } catch (final IOException e) {
            LOG.error("Error accessing resource {}", fileResourcePath, e);
            return HTTP.createResponse(HttpStatusCodes.INTERNAL_SERVER_ERROR);
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
     * Extracts the path value of the URI and normalizes it by dereferencing all relative path elements (. and ..) The
     * method ensures that the path does not reference a resource that is parent to root.
     * 
     * @param resource
     *            the resource identifier to normalize
     * @return the normalized Path to a local file system resource
     */
    private Path normalizeResourcePath(final URI resource) {

        final Deque<String> pathElements = this.getAbsolutePathElements(resource);

        return Paths.get(this.contentRoot.toString(), pathElements.toArray(new String[pathElements.size()]));
    }

    /**
     * Disassembles the path of the resource URI into its elements and returns only those elements that are absolute
     * elements. Relative elements (. and ..) are dereferenced (every .. removes one parent element up to the root)
     * 
     * @param resource
     *            a resource identifier with a path to a resource
     * @return a {@link Deque} with the path elements in the same order as in the the {@link URI}
     */
    private Deque<String> getAbsolutePathElements(final URI resource) {

        final StringTokenizer tok = new StringTokenizer(resource.getPath(), "/");
        final Deque<String> pathElements = new ArrayDeque<>();

        while (tok.hasMoreTokens()) {
            final String pathElement = tok.nextToken();
            if ("..".equals(pathElement) && !pathElements.isEmpty()) {
                pathElements.removeLast();
            } else if (!".".equals(pathElement)) {
                pathElements.addLast(pathElement);
            }
        }
        return pathElements;
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
            final String date = (String) header.getField(RequestHeaders.IF_MODIFIED_SINCE).getValue();
            return this.isModifiedSince(date, fileResourcePath);
        }
        return true;
    }

    /**
     * Determines if the specified file resource is modified since the date specified in the date string (in HTTP Date
     * format).
     * 
     * @param httpDate
     *            the date to check the actual and the system date against
     * @param fileResourcePath
     *            the filesystem that should be checked against the date
     * @return <code>true</code> if the httpDate is after the current system date (in the future), or if it is before
     *         the actual modified date of the file (the file has been modified since that date) or if the date could
     *         not be parsed. In all other cases, the method returns <code>false</code>
     * @throws IOException
     */
    private boolean isModifiedSince(final String httpDate, final Path fileResourcePath) throws IOException {
        try {
            final Date ifModifiedDateDate = HTTP.fromHttpDate(httpDate);
            final Date systemDate = new Date(System.currentTimeMillis());
            final Date modifiedDate = this.getLastModifiedDate(fileResourcePath);

            return ifModifiedDateDate.after(systemDate) || ifModifiedDateDate.before(modifiedDate);

        } catch (final ParseException e) {
            LOG.debug("Could not parse date {}", httpDate, e);
        }
        return true;
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
