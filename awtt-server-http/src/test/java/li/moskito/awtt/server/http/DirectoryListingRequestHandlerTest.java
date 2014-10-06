package li.moskito.awtt.server.http;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import li.moskito.awtt.protocol.http.HttpCommands;
import li.moskito.awtt.protocol.http.HttpVersion;

import org.junit.Before;
import org.junit.Test;

public class DirectoryListingRequestHandlerTest extends FileRequestHandlerBaseTest {

    private DirectoryListingRequestHandler subject;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        this.subject = new DirectoryListingRequestHandler();
        this.config.setProperty("indexFile", "");
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
    public void testAccepts_directoryWithoutIndex_accept() throws Exception {
        this.setupCommand(HttpCommands.GET);
        this.setupResource("/" + this.pathWithNoIndex.getFileName() + "/");
        assertTrue(this.subject.accepts(this.httpRequest));
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
    public void testOnGet() throws Exception {
        throw new RuntimeException("not yet implemented");
    }

    @Test
    public void testDirectoryListingRequestHandler() throws Exception {
        throw new RuntimeException("not yet implemented");
    }

    @Test
    public void testCreateHtmlResponse() throws Exception {
        throw new RuntimeException("not yet implemented");
    }

}
