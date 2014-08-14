package li.moskito.awtt.protocol.http;

import static org.junit.Assert.assertEquals;

import java.net.URI;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class HttpRequestTest {

    private HttpRequest httpRequest;

    @Before
    public void setUp() throws Exception {
        this.httpRequest = new HttpRequest(HttpCommands.GET, new URI("test"), HttpVersion.HTTP_1_1);
    }

    @Test
    public void testGetCommand() throws Exception {
        assertEquals(HttpCommands.GET, this.httpRequest.getCommand());
    }

    @Test
    public void testGetResource() throws Exception {
        assertEquals(new URI("test"), this.httpRequest.getResource());
    }

}
