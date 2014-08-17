package li.moskito.awtt.server.http;

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

import li.moskito.awtt.protocol.http.HttpCommands;
import li.moskito.awtt.protocol.http.HttpRequest;
import li.moskito.awtt.protocol.http.HttpResponse;
import li.moskito.awtt.protocol.http.HttpVersion;
import li.moskito.awtt.protocol.http.ResponseHeaders;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.tree.xpath.XPathExpressionEngine;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class StaticFileContentRequestHandlerTest {

    @Mock
    private HttpRequest httpRequest;

    private StaticFileContentRequestHandler subject;

    private HierarchicalConfiguration config;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        this.config = new HierarchicalConfiguration();
        this.config.setExpressionEngine(new XPathExpressionEngine());
        this.subject = new StaticFileContentRequestHandler();

        when(this.httpRequest.getHeader().getVersion()).thenReturn(HttpVersion.HTTP_1_1);
    }

    @Test
    public void testAccepts_supportedCommand() throws Exception {
        when(this.httpRequest.getCommand()).thenReturn(HttpCommands.GET);
        assertTrue(this.subject.accepts(this.httpRequest));
    }

    @Test
    public void testAccepts_unsupportedCommand() throws Exception {
        when(this.httpRequest.getCommand()).thenReturn(HttpCommands.POST);
        assertFalse(this.subject.accepts(this.httpRequest));
        when(this.httpRequest.getCommand()).thenReturn(HttpCommands.PUT);
        assertFalse(this.subject.accepts(this.httpRequest));
        when(this.httpRequest.getCommand()).thenReturn(HttpCommands.DELETE);
        assertFalse(this.subject.accepts(this.httpRequest));
        when(this.httpRequest.getCommand()).thenReturn(HttpCommands.HEAD);
        assertFalse(this.subject.accepts(this.httpRequest));
        when(this.httpRequest.getCommand()).thenReturn(HttpCommands.OPTIONS);
        assertFalse(this.subject.accepts(this.httpRequest));
        when(this.httpRequest.getCommand()).thenReturn(HttpCommands.TRACE);
        assertFalse(this.subject.accepts(this.httpRequest));
        when(this.httpRequest.getCommand()).thenReturn(HttpCommands.CONNECT);
        assertFalse(this.subject.accepts(this.httpRequest));
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
        when(this.httpRequest.getCommand()).thenReturn(HttpCommands.GET);
        when(this.httpRequest.getResource()).thenReturn(new URI(testFile.getFileName().toString()));

        // act
        final HttpResponse httpResponse = this.subject.process(this.httpRequest);

        // assert
        assertNotNull(httpResponse);
        assertEquals(expectedLastModifiedDate, httpResponse.getHeader().getField(ResponseHeaders.LAST_MODIFIED)
                .getValue());
        assertEquals("text/plain", httpResponse.getHeader().getField(ResponseHeaders.CONTENT_TYPE).getValue());

    }
}
