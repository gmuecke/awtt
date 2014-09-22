package li.moskito.awtt.server.http;

import static li.moskito.awtt.protocol.http.RequestHeaders.IF_MODIFIED_SINCE;
import static li.moskito.awtt.protocol.http.ResponseHeaders.LAST_MODIFIED;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileTime;

import li.moskito.awtt.protocol.http.HttpCommands;
import li.moskito.awtt.protocol.http.HttpResponse;
import li.moskito.awtt.protocol.http.HttpStatusCodes;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

public class FileResourceRequestHandlerTest extends FileRequestHandlerBaseTest {

    private FileResourceRequestHandler subject;
    private Path testFile;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        MockitoAnnotations.initMocks(this);
        this.subject = new FileResourceRequestHandler() {
        };
        this.subject.configure(super.config);
    }

    @Test
    public void testIsFileResource_isExistingFile_true() throws Exception {
        assertTrue(this.subject.isFileResource(this.getTestFileResourceURI()));
    }

    @Test
    public void testIsFileResource_isExistingDirectory_false() throws Exception {
        assertFalse(this.subject.isFileResource(new URI(super.pathWithNoIndex.getFileName() + "/")));
    }

    @Test
    public void testIsFileResource_isExistingDirectoryWithIndexFile_true() throws Exception {
        // content root has index file
        assertTrue(this.subject.isFileResource(new URI(super.pathWithIndex.getFileName() + "/")));
    }

    @Test
    public void testIsFileResource_isNonExistingDirectory_false() throws Exception {
        assertFalse(this.subject.isFileResource(new URI("a/b/")));
    }

    @Test
    public void testIsFileResource_isNonExistingFile_true() throws Exception {
        assertTrue(this.subject.isFileResource(new URI("a/file")));
    }

    @Test
    public void testIsFileSystemResource_isDirectory() throws Exception {
        assertTrue(this.subject.isFileSystemResource(new URI(super.pathWithNoIndex.getFileName() + "/")));
    }

    @Test
    public void testIsFileSystemResource_isExistingFile_true() throws Exception {
        assertTrue(this.subject.isFileSystemResource(this.getTestFileResourceURI()));
    }

    @Test
    public void testIsFileSystemResource_isExistingDirectory_true() throws Exception {
        final Path requestPath = super.contentRoot.relativize(super.pathWithNoIndex);
        assertTrue(this.subject.isFileSystemResource(new URI(requestPath.toString() + "/")));
    }

    @Test
    public void testIsFileSystemResource_isExistingDirectoryWithIndexFile_true() throws Exception {
        // content root has index file
        assertTrue(this.subject.isFileSystemResource(new URI(super.pathWithIndex.getFileName() + "/")));
    }

    @Test
    public void testIsFileSystemResource_isNonExistingDirectory_false() throws Exception {
        assertFalse(this.subject.isFileSystemResource(new URI("a/b/")));
    }

    @Test
    public void testIsFileSystemResource_isNonExistingFile_false() throws Exception {
        assertFalse(this.subject.isFileSystemResource(new URI("a/file")));
    }

    @Test
    public void testResolveFileResource() throws Exception {
        final Path actualFile = this.subject.resolveFileResource(this.getTestFileResourceURI());
        assertEquals(super.testFile, actualFile);
    }

    @Test
    public void testCreateFileResponse_modifiedFile() throws Exception {
        final Path tempFile = Files.createTempFile("test", ".txt");

        final byte[] expected = this.createRandomData(1024);
        Files.copy(new ByteArrayInputStream(expected), tempFile, StandardCopyOption.REPLACE_EXISTING);

        final HttpResponse response = this.subject.createFileResponse(this.httpRequest, tempFile);

        assertNotNull(response);
        assertEquals(HttpStatusCodes.OK, response.getStatusCode());

        final ByteBuffer dst = ByteBuffer.allocate(1024);
        response.getBody().getByteChannel().read(dst);

        assertArrayEquals(expected, dst.array());
    }

    @Test
    public void testCreateFileResponse_exceptionOnReadingFile() throws Exception {
        // act
        // use a directory instead of a file to induce an io exception
        final HttpResponse httpResponse = this.subject.createFileResponse(this.httpRequest, this.pathWithNoIndex);

        assertNotNull(httpResponse);
        assertEquals(HttpStatusCodes.INTERNAL_SERVER_ERROR, httpResponse.getStatusCode());
    }

    @Test
    public void testCreateFileResponse_ifModifiedAfterSystemDate() throws Exception {
        this.testFile = Files.createTempFile("test", ".txt");
        final long systemDate = System.currentTimeMillis();
        Files.setLastModifiedTime(this.testFile, FileTime.fromMillis(systemDate + 8000));
        final String ifModifiedDate = this.toHttpDate(systemDate + 4000);
        final String expectedLastModifiedDate = this.toHttpDate(Files.getLastModifiedTime(this.testFile).toMillis());

        // setup the request
        this.setupCommand(HttpCommands.GET);
        this.setupResource(this.testFile);
        this.setupHeaderField(IF_MODIFIED_SINCE, ifModifiedDate);

        // act
        final HttpResponse httpResponse = this.subject.createFileResponse(this.httpRequest, this.testFile);

        // assert
        assertNotNull(httpResponse);
        assertStatus(HttpStatusCodes.OK, httpResponse);
        assertHeaderField(expectedLastModifiedDate, httpResponse, LAST_MODIFIED);

    }

    @Test
    public void testCreateFileResponse_ifModifiedBeforeModifiedDate() throws Exception {
        this.testFile = Files.createTempFile("test", ".txt");
        final long systemDate = System.currentTimeMillis();
        Files.setLastModifiedTime(this.testFile, FileTime.fromMillis(systemDate - 4000));
        final String ifModifiedDate = this.toHttpDate(systemDate - 8000);
        final String expectedLastModifiedDate = this.toHttpDate(Files.getLastModifiedTime(this.testFile).toMillis());

        // setup the request
        this.setupCommand(HttpCommands.GET);
        this.setupResource(this.testFile);
        this.setupHeaderField(IF_MODIFIED_SINCE, ifModifiedDate);

        // act
        final HttpResponse httpResponse = this.subject.createFileResponse(this.httpRequest, this.testFile);

        // assert
        assertNotNull(httpResponse);
        assertStatus(HttpStatusCodes.OK, httpResponse);
        assertHeaderField(expectedLastModifiedDate, httpResponse, LAST_MODIFIED);

    }

    @Test
    public void testCreateFileResponse_ifModifiedBeforeSystemDate_and_ifModifiedAfterModifiedDate() throws Exception {
        this.testFile = Files.createTempFile("test", ".txt");
        final long now = System.currentTimeMillis();
        Files.setLastModifiedTime(this.testFile, FileTime.fromMillis(now - 8000));
        final String ifModifiedDate = this.toHttpDate(now - 4000);
        this.toHttpDate(Files.getLastModifiedTime(this.testFile).toMillis());

        // setup the request
        this.setupCommand(HttpCommands.GET);
        this.setupResource(this.testFile);
        this.setupHeaderField(IF_MODIFIED_SINCE, ifModifiedDate);

        // act
        final HttpResponse httpResponse = this.subject.createFileResponse(this.httpRequest, this.testFile);

        // assert
        assertNotNull(httpResponse);
        assertStatus(HttpStatusCodes.NOT_MODIFIED, httpResponse);
    }

}
