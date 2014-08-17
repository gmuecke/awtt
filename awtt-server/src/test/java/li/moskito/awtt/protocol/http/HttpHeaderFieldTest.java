package li.moskito.awtt.protocol.http;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class HttpHeaderFieldTest {

    private HttpHeaderField<ResponseHeaders> field_contentLength200;
    private HttpHeaderField<ResponseHeaders> field_contentLengthNull;

    @Before
    public void setUp() {
        this.field_contentLength200 = new HttpHeaderField<>(ResponseHeaders.CONTENT_LENGTH, "200");
        this.field_contentLengthNull = new HttpHeaderField<>(ResponseHeaders.CONTENT_LENGTH);
    }

    @Test
    public void testToString() throws Exception {
        assertEquals("Content-Length: 200", this.field_contentLength200.toString());
        assertEquals("Content-Length: null", this.field_contentLengthNull.toString());
    }

}
