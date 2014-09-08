package li.moskito.awtt.server.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.junit.Before;
import org.junit.Test;

public class FileResourceRequestHandlerTest {

    private FileResourceRequestHandler subject;
    private Path contentRoot;

    @Before
    public void setUp() throws Exception {
        this.subject = new FileResourceRequestHandler();
        final HierarchicalConfiguration config = new HierarchicalConfiguration();
        this.contentRoot = Paths.get(System.getProperty("java.io.tmpdir"));
        config.addProperty("contentRoot", this.contentRoot.toUri().toString());
        config.addProperty("indexFile", "index.txt");
        this.subject.configure(config);
    }

    @Test
    public void testIsFileResource_isExistingFile_true() throws Exception {
        final Path tempFile = Files.createTempFile("test", ".txt");
        final Path requestPath = this.contentRoot.relativize(tempFile);
        assertTrue(this.subject.isFileResource(new URI(requestPath.toString())));
    }

    @Test
    public void testIsFileResource_isExistingDirectory_false() throws Exception {
        final Path tempDir = Files.createTempDirectory("test");
        final Path requestPath = this.contentRoot.relativize(tempDir);
        assertFalse(this.subject.isFileResource(new URI(requestPath.toString() + "/")));
    }

    @Test
    public void testIsFileResource_isExistingDirectoryWithIndexFile_true() throws Exception {
        final Path tempDir = Files.createTempDirectory("test");
        Files.createFile(Paths.get(tempDir.toString(), "index.txt"));
        final Path requestPath = this.contentRoot.relativize(tempDir);
        assertTrue(this.subject.isFileResource(new URI(requestPath.toString() + "/")));
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
    public void testResolveFileResource() throws Exception {
        final Path tempFile = Files.createTempFile("test", ".txt");
        final Path requestPath = this.contentRoot.relativize(tempFile);
        final Path actualFile = this.subject.resolveFileResource(new URI(requestPath.toString()));
        assertEquals(tempFile, actualFile);
    }

    @Test
    public void testCreateFileResponse() throws Exception {
        throw new RuntimeException("not yet implemented");
    }

}
