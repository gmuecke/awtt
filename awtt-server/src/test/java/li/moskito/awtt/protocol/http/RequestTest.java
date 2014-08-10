package li.moskito.awtt.protocol.http;

import static org.junit.Assert.assertEquals;

import java.net.URI;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RequestTest {

    private Request request;

    @Before
    public void setUp() throws Exception {
        this.request = new Request(Commands.GET, new URI("test"), Version.HTTP_1_1);
    }

    @Test
    public void testGetCommand() throws Exception {
        assertEquals(Commands.GET, this.request.getCommand());
    }

    @Test
    public void testGetResource() throws Exception {
        assertEquals(new URI("test"), this.request.getResource());
    }

}
