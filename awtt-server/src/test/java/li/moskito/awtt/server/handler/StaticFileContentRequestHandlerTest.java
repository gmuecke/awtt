package li.moskito.awtt.server.handler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import li.moskito.awtt.protocol.http.Commands;
import li.moskito.awtt.protocol.http.Request;
import li.moskito.awtt.protocol.http.Response;
import li.moskito.awtt.protocol.http.ResponseHeaderFieldDefinitions;
import li.moskito.awtt.protocol.http.Version;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.tree.xpath.XPathExpressionEngine;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class StaticFileContentRequestHandlerTest {

    @Mock
    private Request request;

    private StaticFileContentRequestHandler subject;

    private HierarchicalConfiguration config;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        this.config = new HierarchicalConfiguration();
        this.config.setExpressionEngine(new XPathExpressionEngine());
        this.subject = new StaticFileContentRequestHandler();

        when(this.request.getVersion()).thenReturn(Version.HTTP_1_1);
    }

    @Test
    public void testAccepts_supportedCommand() throws Exception {
        when(this.request.getCommand()).thenReturn(Commands.GET);
        assertTrue(this.subject.accepts(this.request));
    }

    @Test
    public void testAccepts_unsupportedCommand() throws Exception {
        when(this.request.getCommand()).thenReturn(Commands.POST);
        assertFalse(this.subject.accepts(this.request));
        when(this.request.getCommand()).thenReturn(Commands.PUT);
        assertFalse(this.subject.accepts(this.request));
        when(this.request.getCommand()).thenReturn(Commands.DELETE);
        assertFalse(this.subject.accepts(this.request));
        when(this.request.getCommand()).thenReturn(Commands.HEAD);
        assertFalse(this.subject.accepts(this.request));
        when(this.request.getCommand()).thenReturn(Commands.OPTIONS);
        assertFalse(this.subject.accepts(this.request));
        when(this.request.getCommand()).thenReturn(Commands.TRACE);
        assertFalse(this.subject.accepts(this.request));
        when(this.request.getCommand()).thenReturn(Commands.CONNECT);
        assertFalse(this.subject.accepts(this.request));
    }

    @Test
    public void testProcess() throws Exception {
        // prepare

        // configure the subject
        final Path tempContentRoot = Files.createTempDirectory("contentRoot");
        this.config.addProperty("contentRoot", tempContentRoot.toUri().toString());
        this.config.addProperty("contentTypes", "");
        this.config.addProperty("contentTypes/type", "");
        this.config.addProperty("contentTypes/type/@fileExtension", "txt");
        this.config.addProperty("contentTypes/type/@mimeType", "text/plain");
        this.subject.configure(this.config);

        // create a test resource
        final Path testFile = Files.createTempFile(tempContentRoot, "testFile", ".txt");
        final String expectedLastModifiedDate = new SimpleDateFormat("EEE, d MMM yyy HH:mm:ss zzz", Locale.ENGLISH)
                .format(new Date(Files.getLastModifiedTime(testFile).toMillis()));

        // setup the request
        when(this.request.getCommand()).thenReturn(Commands.GET);
        when(this.request.getResource()).thenReturn(new URI(testFile.getFileName().toString()));

        // act
        final Response response = this.subject.process(this.request);

        // assert
        assertNotNull(response);
        assertEquals(expectedLastModifiedDate, response.getField(ResponseHeaderFieldDefinitions.LAST_MODIFIED)
                .getValue());
        assertEquals("text/plain", response.getField(ResponseHeaderFieldDefinitions.CONTENT_TYPE).getValue());

    }
}
