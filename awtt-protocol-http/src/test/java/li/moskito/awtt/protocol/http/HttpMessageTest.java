package li.moskito.awtt.protocol.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import li.moskito.awtt.protocol.BinaryBody;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class HttpMessageTest {
    @Mock
    private BinaryBody body;

    private HttpHeader header;

    private TestHttpMessage httpMessage;

    public static class TestHttpMessage extends HttpMessage {

        /**
         * @param header
         */
        public TestHttpMessage(final HttpHeader header) {
            super(header);
        }

    }

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        this.header = new HttpHeader(HttpStatusCodes.OK);
        this.httpMessage = new TestHttpMessage(this.header);
    }

    @Test
    public void testAddField() throws Exception {
        this.httpMessage.addField(RequestHeaders.ACCEPT, "aValue");
        assertEquals("aValue", this.httpMessage.getHeader().getField(RequestHeaders.ACCEPT).getValue());
    }

    @Test
    public void testGetHeader() throws Exception {
        assertNotNull(this.httpMessage.getHeader());
        assertEquals(this.header, this.httpMessage.getHeader());
    }

    @Test
    public void testGetBody() throws Exception {
        assertNull(this.httpMessage.getBody());
    }

    @Test
    public void testSetBody() throws Exception {
        this.httpMessage.setBody(this.body);
        assertEquals(this.body, this.httpMessage.getBody());
    }

}
