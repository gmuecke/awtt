/**
 * 
 */
package li.moskito.awtt.server.http;

import static li.moskito.awtt.protocol.http.ResponseHeaders.CONTENT_LENGTH;
import static li.moskito.awtt.protocol.http.ResponseHeaders.CONTENT_TYPE;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import li.moskito.awtt.protocol.BinaryBody;
import li.moskito.awtt.protocol.http.HTTP;
import li.moskito.awtt.protocol.http.HTTP.ResponseOptions;
import li.moskito.awtt.protocol.http.HttpCommands;
import li.moskito.awtt.protocol.http.HttpRequest;
import li.moskito.awtt.protocol.http.HttpResponse;
import li.moskito.awtt.protocol.http.HttpStatusCodes;
import li.moskito.awtt.server.ServerRuntimeException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler that lists the content of a directory.
 * 
 * @author Gerald
 */
public class DirectoryListingRequestHandler extends FileResourceRequestHandler {

    /**
     * Visitor that is used when walking the filetree to create the listing of the files.
     * 
     * @author Gerald
     */
    private final class ListingFileVisitor implements FileVisitor<Path> {

        private final StringBuilder listingBuf;
        private final Path resourcePath;
        private final Set<VisitingOption> options;

        /**
         * @param listingBuf
         *            Buffer into which the file listing is written
         * @param rootPath
         */
        private ListingFileVisitor(final StringBuilder listingBuf, final Path rootPath, final VisitingOption... options) {
            this.listingBuf = listingBuf;
            this.resourcePath = rootPath;
            this.options = new HashSet<>();
            this.options.addAll(Arrays.asList(options));
        }

        @Override
        public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
            final boolean isRoot = dir.equals(this.resourcePath);
            if (!isRoot && this.options.contains(VisitingOption.DIRECTORIES)) {
                this.listingBuf.append(DirectoryListingRequestHandler.this.createLink(dir, LinkType.FOLDER));
            }
            return isRoot
                    ? FileVisitResult.CONTINUE
                    : FileVisitResult.SKIP_SUBTREE;
        }

        @Override
        public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
            if (this.options.contains(VisitingOption.FILES)) {
                this.listingBuf.append(DirectoryListingRequestHandler.this.createLink(file, LinkType.FILE));
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(final Path file, final IOException exc) throws IOException {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) throws IOException {
            return FileVisitResult.CONTINUE;
        }
    }

    /**
     * Options for Visiting Files or Directories
     * 
     * @author Gerald
     */
    private enum VisitingOption {
        DIRECTORIES,
        FILES, ;
    }

    /**
     * For for the creation of a html link
     */
    private enum LinkType {
        PARENT,
        FOLDER,
        FILE, ;

        @Override
        public String toString() {
            return super.toString().toLowerCase();
        };
    }

    /**
     * SLF4J Logger for this class
     */
    private static final Logger LOG = LoggerFactory.getLogger(DirectoryListingRequestHandler.class);
    private final Properties templates;

    /**
     * 
     */
    public DirectoryListingRequestHandler() {
        this.templates = new Properties();
        try {
            this.templates.load(this.getClass().getResourceAsStream("templates.properties"));
        } catch (final IOException e) {
            throw new ServerRuntimeException("Unable to load directory listing templates", e);
        }
    }

    /**
     * Accepts GET requests to an existing file or directory with an index file
     */
    @Override
    public boolean accepts(final HttpRequest httpRequest) {
        final boolean commandSupported = httpRequest.getCommand() == HttpCommands.GET;
        final boolean resourceValid = this.isFileSystemResource(httpRequest.getResource());
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
            try {
                return this.createDirectoryListingResponse(httpRequest, resourcePath);
            } catch (final IOException e) {
                LOG.error("Error Listing Directory contents", e);
                return HTTP.createResponse(HttpStatusCodes.INTERNAL_SERVER_ERROR, ResponseOptions.FORCE_CLOSE);
            }
        }
    }

    /**
     * Creates a response containing a listing and navigation links for the requested directory
     * 
     * @param httpRequest
     *            the current request
     * @param resourcePath
     *            the resource path pointing to a directory
     * @return
     * @throws IOException
     */
    private HttpResponse createDirectoryListingResponse(final HttpRequest httpRequest, final Path resourcePath)
            throws IOException {

        final String title = "Directory " + httpRequest.getResource();

        //@formatter:off
        final String style = this.fromTemplate("html.head.style", this.getListItemStyle("parent"), this.getListItemStyle("folder"), this.getListItemStyle("file"));
        
        // @formatter:on
        final String head = this.fromTemplate("html.head", title, style);

        final StringBuilder listingBuf = new StringBuilder(256);

        if (!resourcePath.equals(this.getContentRoot())) {
            listingBuf.append(this.createLink(resourcePath.getParent(), LinkType.PARENT, ".."));
        }
        Files.walkFileTree(resourcePath, new ListingFileVisitor(listingBuf, resourcePath, VisitingOption.DIRECTORIES));
        Files.walkFileTree(resourcePath, new ListingFileVisitor(listingBuf, resourcePath, VisitingOption.FILES));

        final String header = this.fromTemplate("html.h1", httpRequest.getResource());
        final String list = this.fromTemplate("html.ul", listingBuf);
        final String body = this.fromTemplate("html.body", header, list);
        final String html = this.fromTemplate("html", head, body);

        final byte[] data = html.getBytes();
        final HttpResponse httpResponse = new HttpResponse(HttpStatusCodes.OK);
        final ReadableByteChannel channel = Channels.newChannel(new ByteArrayInputStream(data));

        httpResponse.setBody(new BinaryBody(channel));

        httpResponse.addField(CONTENT_LENGTH, data.length);
        httpResponse.addField(CONTENT_TYPE, "text/html");

        return httpResponse;
    }

    private String fromTemplate(final String templateName, final Object... args) {
        return String.format(this.templates.getProperty(templateName), args);
    }

    private String createLink(final Path path, final LinkType type) {
        return this.createLink(path, type, path.getFileName().toString());
    }

    private String createLink(final Path path, final LinkType type, final String name) {
        String resolvedPath = "/";
        if (!path.equals(this.getContentRoot())) {
            resolvedPath += this.getContentRoot().relativize(path).toString().replaceAll("\\\\", "/");
        }
        return String.format("<li class=\"%s\"><a href=\"%s\">%s</a></li>\n", type, resolvedPath, name);
    }

    private String getListItemStyle(final String type) throws IOException {
        final StringBuilder buf = new StringBuilder(64);
        //@formatter:off
        buf.append("display: inline-block;\n").
            append("width: 16px;\n").
            append("height: 16px;\n").
            append("content: \"\";\n").
            append("margin-right: 5px;\n").
            append("background-color: blue;").
            append("background: 0 0/100% no-repeat ").append(this.getImageContentURL(type)).append(';');
        // @formatter:on
        return buf.toString();
    }

    private String getImageContentURL(final String imageName) throws IOException {
        return String.format("url(data:image/png;base64,%s)", this.getImageBase64(imageName));
    }

    private String getImageBase64(final String imageName) throws IOException {
        final InputStream is = this.getClass().getResourceAsStream("/images/" + imageName + ".png");
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        IOUtils.copy(is, baos);

        return new String(Base64.encodeBase64(baos.toByteArray()));
    }
}
