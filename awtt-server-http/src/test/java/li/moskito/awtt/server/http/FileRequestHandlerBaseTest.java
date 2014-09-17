/**
 * 
 */
package li.moskito.awtt.server.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;

import li.moskito.awtt.protocol.HeaderFieldDefinition;
import li.moskito.awtt.protocol.http.HttpCommands;
import li.moskito.awtt.protocol.http.HttpRequest;
import li.moskito.awtt.protocol.http.HttpResponse;
import li.moskito.awtt.protocol.http.HttpStatusCodes;
import li.moskito.awtt.protocol.http.RequestHeaders;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.tree.xpath.XPathExpressionEngine;
import org.junit.Before;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Abstract base test class for file request handler tests
 * 
 * @author Gerald
 */
public abstract class FileRequestHandlerBaseTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    HttpRequest httpRequest;

    Path contentRoot;

    HierarchicalConfiguration config;

    Path testFile;

    Path pathWithNoIndex;

    Path pathWithIndex;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        this.setupConfiguration(this.setupTestContentRoot());
    }

    private String setupTestContentRoot() throws IOException {
        this.contentRoot = Files.createTempDirectory("contentRoot");
        this.pathWithNoIndex = Files.createTempDirectory(this.contentRoot, "noIndex");
        this.pathWithIndex = Files.createTempDirectory(this.contentRoot, "index");
        Files.createFile(Paths.get(this.contentRoot.toString(), "index.txt"));
        Files.createFile(Paths.get(this.pathWithIndex.toString(), "index.txt"));
        this.testFile = Files.createTempFile(this.contentRoot, "testFile", ".txt");
        return this.contentRoot.toUri().toString();
    }

    private void setupConfiguration(final String contentRoot) throws ConfigurationException {
        this.config = new HierarchicalConfiguration();
        this.config.setExpressionEngine(new XPathExpressionEngine());
        this.config.addProperty("contentRoot", this.contentRoot.toUri().toString());
        this.config.addProperty("indexFile", "index.txt");
        this.config.addProperty("contentRoot", contentRoot);
        this.config.addProperty("indexFile", this.testFile.getFileName().toString());
        this.config.addProperty("contentTypes", "");
        this.config.addProperty("contentTypes/type", "");
        this.config.addProperty("contentTypes/type/@mimeType", "text/plain");
        this.config.addProperty("contentTypes/type/@fileExtension", "txt");
    }

    /**
     * creates an array of randomized data
     * 
     * @param i
     *            size of the random data array
     * @return the array of randomized date
     */
    protected byte[] createRandomData(final int i) {
        final byte[] data = new byte[i];
        new Random().nextBytes(data);
        return data;
    }

    /**
     * Asserts the value of the specified header field
     * 
     * @param expectedValue
     *            the expected value of the header field
     * @param httpResponse
     *            the response carrying the heaer field
     * @param headerField
     *            the name of the header field
     */
    public static void assertHeaderField(final String expectedValue, final HttpResponse httpResponse,
            final HeaderFieldDefinition headerField) {
        assertTrue("Field " + headerField + " is not present", httpResponse.getHeader().hasField(headerField));
        assertEquals(expectedValue, httpResponse.getHeader().getField(headerField).getValue());
    }

    /**
     * Asserts the status code of a HTTPResponse
     * 
     * @param code
     *            the expected status code
     * @param httpResponse
     *            the response containing the status
     */
    public static void assertStatus(final HttpStatusCodes code, final HttpResponse httpResponse) {
        assertEquals(code, httpResponse.getStatusCode());

    }

    /**
     * sets up the mock request with the specified resource
     * 
     * @param pathToResource
     * @throws URISyntaxException
     */
    protected void setupResource(final Path pathToResource) throws URISyntaxException {
        this.setupResource(pathToResource.getFileName().toString());

    }

    /**
     * sets up the mock request with the specified resource
     * 
     * @param resourcePath
     * @throws URISyntaxException
     */
    protected void setupResource(final String resourcePath) throws URISyntaxException {
        when(this.httpRequest.getResource()).thenReturn(new URI(resourcePath));

    }

    /**
     * Sets up the mock request with the specified command
     * 
     * @param cmd
     *            the command to set
     */
    protected void setupCommand(final HttpCommands cmd) {
        when(this.httpRequest.getCommand()).thenReturn(cmd);

    }

    /**
     * Sets up the mock request header with the specified field
     * 
     * @param headerField
     *            headerfield to be added to the mock request
     * @param value
     *            value of the field
     */
    protected void setupHeaderField(final RequestHeaders headerField, final String value) {
        when(this.httpRequest.getHeader().hasField(headerField)).thenReturn(true);
        when(this.httpRequest.getHeader().getField(headerField).getValue()).thenReturn(value);

    }

    /**
     * Converts the timestamp into a HTTP Date string
     * 
     * @param ts
     * @return
     */
    protected String toHttpDate(final long ts) {
        final SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyy HH:mm:ss zzz", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        final String expectedLastModifiedDate = sdf.format(new Date(ts));
        return expectedLastModifiedDate;
    }

    protected URI getTestFileResourceURI() throws URISyntaxException {
        return new URI(this.testFile.getFileName().toString());
    }

}
