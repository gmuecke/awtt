package li.moskito.awtt.protocol.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import li.moskito.awtt.protocol.BinaryBody;

import org.junit.Before;
import org.junit.Test;

public class HttpMessageTest {

    private HttpMessage responseMessage;

    @Before
    public void setUp() throws Exception {
        this.responseMessage = new HttpMessage(HttpVersion.HTTP_1_0) {};
    }

    @Test
    public void testGetVersion() throws Exception {
        assertEquals(HttpVersion.HTTP_1_0, this.responseMessage.getVersion());
    }

    @Test
    public void testAddFields_GetFields() throws Exception {

        final HttpHeaderField<ResponseHeaders> field1 = new HttpHeaderField<>(
                ResponseHeaders.CONNECTION, "Keep-Alive");
        final HttpHeaderField<ResponseHeaders> field2 = new HttpHeaderField<>(
                ResponseHeaders.CONTENT_LENGTH, "200");
        final List<HttpHeaderField<?>> expected = new ArrayList<>();
        expected.add(field1);
        expected.add(field2);
        this.responseMessage.addFields(expected);

        final List<HttpHeaderField<?>> actual = this.responseMessage.getFields();
        assertEquals(2, actual.size());
        assertTrue(actual.contains(field1));
        assertTrue(actual.contains(field2));
    }

    @Test
    public void testAddField_GetFieldString() throws Exception {
        this.responseMessage.addField(ResponseHeaders.AUTHORIZATION, "someValue");
        this.responseMessage.addField(ResponseHeaders.RETRY_AFTER, "5");

        assertNotNull(this.responseMessage.getField(ResponseHeaders.AUTHORIZATION));
        assertEquals("someValue", this.responseMessage.getField(ResponseHeaders.AUTHORIZATION)
                .getValue());
        assertNotNull(this.responseMessage.getField(ResponseHeaders.RETRY_AFTER));
        assertEquals("5", this.responseMessage.getField(ResponseHeaders.RETRY_AFTER).getValue());

        assertTrue(this.responseMessage.getFieldNames().contains(ResponseHeaders.AUTHORIZATION));
        assertTrue(this.responseMessage.getFieldNames().contains(ResponseHeaders.RETRY_AFTER));
    }

    @Test
    public void testAddFieldObject_GetFieldString() throws Exception {

        final HttpHeaderField<ResponseHeaders> field1 = new HttpHeaderField<>(
                ResponseHeaders.AUTHORIZATION, "someValue");
        final HttpHeaderField<ResponseHeaders> field2 = new HttpHeaderField<>(
                ResponseHeaders.RETRY_AFTER, "5");

        this.responseMessage.addField(field1);
        this.responseMessage.addField(field2);

        assertNotNull(this.responseMessage.getField(ResponseHeaders.AUTHORIZATION));
        assertEquals("someValue", this.responseMessage.getField(ResponseHeaders.AUTHORIZATION)
                .getValue());
        assertNotNull(this.responseMessage.getField(ResponseHeaders.RETRY_AFTER));
        assertEquals("5", this.responseMessage.getField(ResponseHeaders.RETRY_AFTER).getValue());

        assertTrue(this.responseMessage.getFields().contains(field1));
        assertTrue(this.responseMessage.getFields().contains(field2));

        assertTrue(this.responseMessage.getFieldNames().contains(ResponseHeaders.AUTHORIZATION));
        assertTrue(this.responseMessage.getFieldNames().contains(ResponseHeaders.RETRY_AFTER));
    }

    @Test
    public void testGetFieldNames() throws Exception {
        this.responseMessage.addField(ResponseHeaders.AUTHORIZATION, "someValue");
        this.responseMessage.addField(ResponseHeaders.RETRY_AFTER, "5");

        final Set<?> fieldNames = this.responseMessage.getFieldNames();
        assertNotNull(fieldNames);
        assertTrue(fieldNames.contains(ResponseHeaders.AUTHORIZATION));
        assertTrue(fieldNames.contains(ResponseHeaders.RETRY_AFTER));
    }

    @Test
    public void testHasEntity_noEntity() {
        assertFalse(this.responseMessage.hasEntity());
    }

    @Test
    public void testHasEntity_withEntity() {
        final BinaryBody entity = mock(BinaryBody.class);
        this.responseMessage.setEntity(entity);
        assertTrue(this.responseMessage.hasEntity());
    }

    @Test
    public void testGetSetEntity() {
        final BinaryBody entity = mock(BinaryBody.class);
        this.responseMessage.setEntity(entity);
        assertEquals(entity, this.responseMessage.getEntity());
    }
}
