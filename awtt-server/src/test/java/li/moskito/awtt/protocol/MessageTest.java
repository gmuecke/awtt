package li.moskito.awtt.protocol;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MessageTest {
    @Mock
    private Body body;

    @Mock
    private Charset charset;

    @Mock
    private Header header;
    @InjectMocks
    private TestMessage message;

    public static class TestMessage extends Message {

        /**
         * @param header
         */
        public TestMessage(final Header header) {
            super(header);
        }

    }

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        this.message = new TestMessage(this.header);
    }

    @Test
    public void testGetHeader() throws Exception {
        assertEquals(this.header, this.message.getHeader());
    }

    @Test
    public void testGetBody() throws Exception {
        assertNull(this.message.getBody()); // default body is null
    }

    @Test
    public void testSetBody() throws Exception {
        final Body body = mock(Body.class);
        this.message.setBody(body);
        assertEquals(body, this.message.getBody());
    }

    @Test
    public void testHasBody() throws Exception {
        assertFalse(this.message.hasBody());
        this.message.setBody(this.body);
        assertTrue(this.message.hasBody());
    }

    @Test
    public void testGetCharset() throws Exception {
        assertEquals(StandardCharsets.UTF_8, this.message.getCharset()); // default
    }

    @Test
    public void testSetCharset() throws Exception {
        this.message.setCharset(StandardCharsets.ISO_8859_1);
        assertEquals(StandardCharsets.ISO_8859_1, this.message.getCharset());
    }

}
