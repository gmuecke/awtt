package li.moskito.awtt.protocol.http;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class HeaderFieldTest {

    private HeaderField<ResponseHeaderFieldDefinitions> field_contentLength200;

    @Before
    public void setUp() {
        this.field_contentLength200 = new HeaderField<>(ResponseHeaderFieldDefinitions.CONTENT_LENGTH, "200");
    }

    @Test
    public void testToString() throws Exception {
        assertEquals("Content-Length: 200", this.field_contentLength200.toString());
    }

    @Test
    public void testGetFieldName() throws Exception {
        assertEquals(ResponseHeaderFieldDefinitions.CONTENT_LENGTH, this.field_contentLength200.getFieldName());
    }

    @Test
    public void testGetValue() throws Exception {
        assertEquals("200", this.field_contentLength200.getValue());
    }

}
