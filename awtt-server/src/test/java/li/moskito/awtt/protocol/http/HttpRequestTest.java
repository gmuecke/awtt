package li.moskito.awtt.protocol.http;

import static org.junit.Assert.assertEquals;

import java.net.URI;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class HttpRequestTest {

    private HttpCommands command;

    private URI resource;

    private HttpVersion version;

    private HttpRequest httpRequest;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        this.version = HttpVersion.HTTP_1_0;
        this.command = HttpCommands.GET;
        this.resource = new URI("/testResource");
        this.httpRequest = new HttpRequest(this.command, this.resource, this.version);
    }

    @Test
    public void testGetCommand() throws Exception {
        assertEquals(this.command, this.httpRequest.getCommand());
    }

    @Test
    public void testGetResource() throws Exception {
        assertEquals(this.resource, this.httpRequest.getResource());
    }

    @Test
    public void testToString() throws Exception {
        final String strRequest = "GET /testResource HTTP/1.0\r\n\r\n";
        assertEquals(strRequest, this.httpRequest.toString());
    }

}
