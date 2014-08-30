package li.moskito.awtt.server.http;

import static li.moskito.awtt.protocol.http.RequestHeaders.IF_MODIFIED_SINCE;
import static li.moskito.awtt.protocol.http.ResponseHeaders.LAST_MODIFIED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import li.moskito.awtt.protocol.HeaderFieldDefinition;
import li.moskito.awtt.protocol.http.HttpCommands;
import li.moskito.awtt.protocol.http.HttpRequest;
import li.moskito.awtt.protocol.http.HttpResponse;
import li.moskito.awtt.protocol.http.HttpStatusCodes;
import li.moskito.awtt.protocol.http.HttpVersion;
import li.moskito.awtt.protocol.http.RequestHeaders;
import li.moskito.awtt.protocol.http.ResponseHeaders;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.tree.xpath.XPathExpressionEngine;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class StaticFileContentRequestHandlerTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private HttpRequest httpRequest;

    private StaticFileContentRequestHandler subject;

    private HierarchicalConfiguration config;

    private Path testFile;

    private Path pathWithNoIndex;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        this.config = new HierarchicalConfiguration();
        this.config.setExpressionEngine(new XPathExpressionEngine());
        this.subject = new StaticFileContentRequestHandler();
        final String contentRoot = this.createTestContentRoot();
        this.configureSubject(contentRoot);

        when(this.httpRequest.getHeader().getVersion()).thenReturn(HttpVersion.HTTP_1_1);
    }

    private String createTestContentRoot() throws IOException {
        final Path tempContentRoot = Files.createTempDirectory("contentRoot");
        this.pathWithNoIndex = Files.createTempDirectory(tempContentRoot, "noIndex");
        this.testFile = Files.createTempFile(tempContentRoot, "testFile", ".txt");
        return tempContentRoot.toUri().toString();
    }

    private void configureSubject(final String contentRoot) throws ConfigurationException {
        this.config.addProperty("contentRoot", contentRoot);
        this.config.addProperty("indexFile", this.testFile.getFileName().toString());
        this.config.addProperty("contentTypes", "");
        this.config.addProperty("contentTypes/type", "");
        this.config.addProperty("contentTypes/type/@mimeType", "text/plain");
        this.config.addProperty("contentTypes/type/@fileExtension", "txt");
        this.subject.configure(this.config);
    }

    @Test
    public void testAccepts_supportedCommand_and_existingFile() throws Exception {
        this.setupCommand(HttpCommands.GET);
        this.setupResource(this.testFile);
        assertTrue(this.subject.accepts(this.httpRequest));
    }

    @Test
    public void testAccepts_directoryWithIndex() throws Exception {
        this.setupCommand(HttpCommands.GET);
        this.setupResource("/");
        assertTrue(this.subject.accepts(this.httpRequest));
    }

    @Test
    public void testAccepts_nonExistingResource_accept() throws Exception {
        this.setupCommand(HttpCommands.GET);
        this.setupResource("trallala");
        assertTrue(this.subject.accepts(this.httpRequest));
    }

    @Test
    public void testAccepts_directoryWithoutIndex_reject() throws Exception {
        this.setupCommand(HttpCommands.GET);
        this.setupResource("/" + this.pathWithNoIndex.getFileName() + "/");
        assertFalse(this.subject.accepts(this.httpRequest));
    }

    @Test
    public void testAccepts_unsupportedCommand_reject() throws Exception {
        this.setupResource(this.testFile);
        this.setupCommand(HttpCommands.POST);
        assertFalse(this.subject.accepts(this.httpRequest));
        this.setupCommand(HttpCommands.PUT);
        assertFalse(this.subject.accepts(this.httpRequest));
        this.setupCommand(HttpCommands.DELETE);
        assertFalse(this.subject.accepts(this.httpRequest));
        this.setupCommand(HttpCommands.HEAD);
        assertFalse(this.subject.accepts(this.httpRequest));
        this.setupCommand(HttpCommands.OPTIONS);
        assertFalse(this.subject.accepts(this.httpRequest));
        this.setupCommand(HttpCommands.TRACE);
        assertFalse(this.subject.accepts(this.httpRequest));
        this.setupCommand(HttpCommands.CONNECT);
        assertFalse(this.subject.accepts(this.httpRequest));
    }

    @Test
    public void testOnGet_relativePathToParent() throws Exception {

        // setup the request
        this.setupCommand(HttpCommands.GET);
        this.setupResource(".././../" + this.testFile.getFileName());
        final String expectedLastModifiedDate = this.toHttpDate(Files.getLastModifiedTime(this.testFile).toMillis());

        // act
        final HttpResponse httpResponse = this.subject.process(this.httpRequest);

        // assert
        assertNotNull(httpResponse);
        this.assertStatus(HttpStatusCodes.OK, httpResponse);
        this.assertHeaderField(expectedLastModifiedDate, httpResponse, LAST_MODIFIED);
        this.assertHeaderField("text/plain", httpResponse, ResponseHeaders.CONTENT_TYPE);
    }

    @Test
    public void testOnGet_indexedPath() throws Exception {

        // setup the request
        this.setupCommand(HttpCommands.GET);
        this.setupResource("/");
        // the test file is the index file
        final String expectedLastModifiedDate = this.toHttpDate(Files.getLastModifiedTime(this.testFile).toMillis());

        // act
        final HttpResponse httpResponse = this.subject.process(this.httpRequest);

        // assert
        assertNotNull(httpResponse);
        this.assertStatus(HttpStatusCodes.OK, httpResponse);
        this.assertHeaderField(expectedLastModifiedDate, httpResponse, LAST_MODIFIED);
        this.assertHeaderField("text/plain", httpResponse, ResponseHeaders.CONTENT_TYPE);
    }

    @Test
    public void testOnGet_nonIndexedPath() throws Exception {

        // setup the request
        this.setupCommand(HttpCommands.GET);
        this.setupResource("/" + this.pathWithNoIndex.getFileName() + "/");

        // act
        final HttpResponse httpResponse = this.subject.process(this.httpRequest);

        // assert
        assertNotNull(httpResponse);
        this.assertStatus(HttpStatusCodes.NOT_FOUND, httpResponse);
    }

    @Test
    public void testOnGet_missingResource() throws Exception {

        // setup the request
        this.setupCommand(HttpCommands.GET);
        this.setupResource("trallala");
        // the test file is the index file

        // act
        final HttpResponse httpResponse = this.subject.process(this.httpRequest);

        // assert
        assertNotNull(httpResponse);
        this.assertStatus(HttpStatusCodes.NOT_FOUND, httpResponse);
        this.assertHeaderField("close", httpResponse, ResponseHeaders.CONNECTION);
        this.assertHeaderField("0", httpResponse, ResponseHeaders.CONTENT_LENGTH);
    }

    @Test
    public void testOnGet_noIfModifiedDate() throws Exception {

        final String expectedLastModifiedDate = this.toHttpDate(Files.getLastModifiedTime(this.testFile).toMillis());

        // setup the request
        this.setupCommand(HttpCommands.GET);
        this.setupResource(this.testFile);

        // act
        final HttpResponse httpResponse = this.subject.process(this.httpRequest);

        // assert
        assertNotNull(httpResponse);
        this.assertStatus(HttpStatusCodes.OK, httpResponse);
        this.assertHeaderField(expectedLastModifiedDate, httpResponse, LAST_MODIFIED);
        this.assertHeaderField("text/plain", httpResponse, ResponseHeaders.CONTENT_TYPE);

    }

    @Test
    public void testOnGet_ifModifiedAfterSystemDate() throws Exception {
        final long systemDate = System.currentTimeMillis();
        Files.setLastModifiedTime(this.testFile, FileTime.fromMillis(systemDate + 8000));
        final String ifModifiedDate = this.toHttpDate(systemDate + 4000);
        final String expectedLastModifiedDate = this.toHttpDate(Files.getLastModifiedTime(this.testFile).toMillis());

        // setup the request
        this.setupCommand(HttpCommands.GET);
        this.setupResource(this.testFile);
        this.setupHeaderField(IF_MODIFIED_SINCE, ifModifiedDate);

        // act
        final HttpResponse httpResponse = this.subject.process(this.httpRequest);

        // assert
        assertNotNull(httpResponse);
        this.assertStatus(HttpStatusCodes.OK, httpResponse);
        this.assertHeaderField(expectedLastModifiedDate, httpResponse, LAST_MODIFIED);
        this.assertHeaderField("text/plain", httpResponse, ResponseHeaders.CONTENT_TYPE);

    }

    @Test
    public void testOnGet_ifModifiedBeforeModifiedDate() throws Exception {

        final long systemDate = System.currentTimeMillis();
        Files.setLastModifiedTime(this.testFile, FileTime.fromMillis(systemDate - 4000));
        final String ifModifiedDate = this.toHttpDate(systemDate - 8000);
        final String expectedLastModifiedDate = this.toHttpDate(Files.getLastModifiedTime(this.testFile).toMillis());

        // setup the request
        this.setupCommand(HttpCommands.GET);
        this.setupResource(this.testFile);
        this.setupHeaderField(IF_MODIFIED_SINCE, ifModifiedDate);

        // act
        final HttpResponse httpResponse = this.subject.process(this.httpRequest);

        // assert
        assertNotNull(httpResponse);
        this.assertStatus(HttpStatusCodes.OK, httpResponse);
        this.assertHeaderField(expectedLastModifiedDate, httpResponse, LAST_MODIFIED);
        this.assertHeaderField("text/plain", httpResponse, ResponseHeaders.CONTENT_TYPE);

    }

    @Test
    public void testOnGet_ifModifiedBeforeSystemDate_and_ifModifiedAfterModifiedDate() throws Exception {
        final long now = System.currentTimeMillis();
        Files.setLastModifiedTime(this.testFile, FileTime.fromMillis(now - 8000));
        final String ifModifiedDate = this.toHttpDate(now - 4000);
        this.toHttpDate(Files.getLastModifiedTime(this.testFile).toMillis());

        // setup the request
        this.setupCommand(HttpCommands.GET);
        this.setupResource(this.testFile);
        this.setupHeaderField(IF_MODIFIED_SINCE, ifModifiedDate);

        // act
        final HttpResponse httpResponse = this.subject.process(this.httpRequest);

        // assert
        assertNotNull(httpResponse);
        this.assertStatus(HttpStatusCodes.NOT_MODIFIED, httpResponse);
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
    protected void assertHeaderField(final String expectedValue, final HttpResponse httpResponse,
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
    protected void assertStatus(final HttpStatusCodes code, final HttpResponse httpResponse) {
        assertEquals(code, httpResponse.getStatusCode());

    }

    /**
     * sets up the mock request with the specified resource
     * 
     * @param pathToResource
     * @throws URISyntaxException
     */
    private void setupResource(final Path pathToResource) throws URISyntaxException {
        this.setupResource(pathToResource.getFileName().toString());

    }

    /**
     * sets up the mock request with the specified resource
     * 
     * @param resourcePath
     * @throws URISyntaxException
     */
    private void setupResource(final String resourcePath) throws URISyntaxException {
        when(this.httpRequest.getResource()).thenReturn(new URI(resourcePath));

    }

    /**
     * Sets up the mock request with the specified command
     * 
     * @param cmd
     *            the command to set
     */
    private void setupCommand(final HttpCommands cmd) {
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
    private void setupHeaderField(final RequestHeaders headerField, final String value) {
        when(this.httpRequest.getHeader().hasField(headerField)).thenReturn(true);
        when(this.httpRequest.getHeader().getField(headerField).getValue()).thenReturn(value);

    }

    /**
     * Converts the timestamp into a HTTP Date string
     * 
     * @param ts
     * @return
     */
    private String toHttpDate(final long ts) {
        final SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyy HH:mm:ss zzz", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        final String expectedLastModifiedDate = sdf.format(new Date(ts));
        return expectedLastModifiedDate;
    }
}
