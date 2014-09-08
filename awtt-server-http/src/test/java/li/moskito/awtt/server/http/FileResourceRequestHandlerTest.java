package li.moskito.awtt.server.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.junit.Before;
import org.junit.Test;

public class FileResourceRequestHandlerTest {

    private FileResourceRequestHandler subject;

    @Before
    public void setUp() throws Exception {
        this.subject = new FileResourceRequestHandler();
        final HierarchicalConfiguration config = new HierarchicalConfiguration();
        config.addProperty("contentRoot", "file:///");
        this.subject.configure(config);
    }

    @Test
    public void testIsFileResource_isFile_true() throws Exception {
        final Path tempFile = Files.createTempFile("test", ".txt");
        assertTrue(this.subject.isFileResource(tempFile.toUri()));
    }

    @Test
    public void testIsFileResource_isDirectory_false() throws Exception {
        final Path tempDir = Files.createTempDirectory("test");
        assertFalse(this.subject.isFileResource(tempDir.toUri()));
    }

    @Test
    public void testIsFileResource_isNoFile_false() throws Exception {
        final Path tempDir = Paths.get("a", "b", "c");
        assertFalse(this.subject.isFileResource(tempDir.toUri()));
    }

    @Test
    public void testResolveFileResource() throws Exception {
        final Path tempFile = Files.createTempFile("test", ".txt");
        final Path actualFile = this.subject.resolveFileResource(tempFile.toUri());
        assertEquals(tempFile, actualFile);
    }

    @Test
    public void testCreateFileResponse() throws Exception {
        throw new RuntimeException("not yet implemented");
    }

}
