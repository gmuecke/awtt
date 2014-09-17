package li.moskito.awtt.server.http;

import static li.moskito.awtt.protocol.http.RequestHeaders.IF_MODIFIED_SINCE;
import static li.moskito.awtt.protocol.http.ResponseHeaders.LAST_MODIFIED;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.nio.file.Files;
import java.nio.file.attribute.FileTime;

import li.moskito.awtt.protocol.http.HttpCommands;
import li.moskito.awtt.protocol.http.HttpResponse;
import li.moskito.awtt.protocol.http.HttpStatusCodes;
import li.moskito.awtt.protocol.http.HttpVersion;
import li.moskito.awtt.protocol.http.ResponseHeaders;

import org.junit.Before;
import org.junit.Test;

public class StaticFileContentRequestHandlerTest extends FileRequestHandlerBaseTest {

    private FileResourceRequestHandler subject;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        this.subject = new StaticFileContentRequestHandler();
        this.subject.configure(super.config);
        when(this.httpRequest.getHeader().getVersion()).thenReturn(HttpVersion.HTTP_1_1);
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
        assertStatus(HttpStatusCodes.OK, httpResponse);
        assertHeaderField(expectedLastModifiedDate, httpResponse, LAST_MODIFIED);
        assertHeaderField("text/plain", httpResponse, ResponseHeaders.CONTENT_TYPE);
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
        assertStatus(HttpStatusCodes.OK, httpResponse);
        assertHeaderField(expectedLastModifiedDate, httpResponse, LAST_MODIFIED);
        assertHeaderField("text/plain", httpResponse, ResponseHeaders.CONTENT_TYPE);
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
        assertStatus(HttpStatusCodes.NOT_FOUND, httpResponse);
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
        assertStatus(HttpStatusCodes.NOT_FOUND, httpResponse);
        assertHeaderField("close", httpResponse, ResponseHeaders.CONNECTION);
        assertHeaderField("0", httpResponse, ResponseHeaders.CONTENT_LENGTH);
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
        assertStatus(HttpStatusCodes.OK, httpResponse);
        assertHeaderField(expectedLastModifiedDate, httpResponse, LAST_MODIFIED);
        assertHeaderField("text/plain", httpResponse, ResponseHeaders.CONTENT_TYPE);

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
        assertStatus(HttpStatusCodes.OK, httpResponse);
        assertHeaderField(expectedLastModifiedDate, httpResponse, LAST_MODIFIED);
        assertHeaderField("text/plain", httpResponse, ResponseHeaders.CONTENT_TYPE);

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
        assertStatus(HttpStatusCodes.OK, httpResponse);
        assertHeaderField(expectedLastModifiedDate, httpResponse, LAST_MODIFIED);
        assertHeaderField("text/plain", httpResponse, ResponseHeaders.CONTENT_TYPE);

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
        assertStatus(HttpStatusCodes.NOT_MODIFIED, httpResponse);
    }
}
