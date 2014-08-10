package li.moskito.awtt.protocol.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

public class MessageTest {

    private Message<ResponseHeaderFieldDefinitions> responseMessage;

    @Before
    public void setUp() throws Exception {
        this.responseMessage = new Message<ResponseHeaderFieldDefinitions>(Version.HTTP_1_0) {
        };
    }

    @Test
    public void testGetVersion() throws Exception {
        assertEquals(Version.HTTP_1_0, this.responseMessage.getVersion());
    }

    @Test
    public void testAddFields_GetFields() throws Exception {

        final HeaderField<ResponseHeaderFieldDefinitions> field1 = new HeaderField<>(
                ResponseHeaderFieldDefinitions.CONNECTION, "Keep-Alive");
        final HeaderField<ResponseHeaderFieldDefinitions> field2 = new HeaderField<>(
                ResponseHeaderFieldDefinitions.CONTENT_LENGTH, "200");
        final List<HeaderField<ResponseHeaderFieldDefinitions>> expected = new ArrayList<>();
        expected.add(field1);
        expected.add(field2);
        this.responseMessage.addFields(expected);

        final List<HeaderField<ResponseHeaderFieldDefinitions>> actual = this.responseMessage.getFields();
        assertEquals(2, actual.size());
        assertTrue(actual.contains(field1));
        assertTrue(actual.contains(field2));
    }

    @Test
    public void testAddField_GetFieldString() throws Exception {
        this.responseMessage.addField(ResponseHeaderFieldDefinitions.AUTHORIZATION, "someValue");
        this.responseMessage.addField(ResponseHeaderFieldDefinitions.RETRY_AFTER, "5");

        assertNotNull(this.responseMessage.getField(ResponseHeaderFieldDefinitions.AUTHORIZATION));
        assertEquals("someValue", this.responseMessage.getField(ResponseHeaderFieldDefinitions.AUTHORIZATION)
                .getValue());
        assertNotNull(this.responseMessage.getField(ResponseHeaderFieldDefinitions.RETRY_AFTER));
        assertEquals("5", this.responseMessage.getField(ResponseHeaderFieldDefinitions.RETRY_AFTER).getValue());

        assertTrue(this.responseMessage.getFieldNames().contains(ResponseHeaderFieldDefinitions.AUTHORIZATION));
        assertTrue(this.responseMessage.getFieldNames().contains(ResponseHeaderFieldDefinitions.RETRY_AFTER));
    }

    @Test
    public void testAddFieldObject_GetFieldString() throws Exception {

        final HeaderField<ResponseHeaderFieldDefinitions> field1 = new HeaderField<>(
                ResponseHeaderFieldDefinitions.AUTHORIZATION, "someValue");
        final HeaderField<ResponseHeaderFieldDefinitions> field2 = new HeaderField<>(
                ResponseHeaderFieldDefinitions.RETRY_AFTER, "5");

        this.responseMessage.addField(field1);
        this.responseMessage.addField(field2);

        assertNotNull(this.responseMessage.getField(ResponseHeaderFieldDefinitions.AUTHORIZATION));
        assertEquals("someValue", this.responseMessage.getField(ResponseHeaderFieldDefinitions.AUTHORIZATION)
                .getValue());
        assertNotNull(this.responseMessage.getField(ResponseHeaderFieldDefinitions.RETRY_AFTER));
        assertEquals("5", this.responseMessage.getField(ResponseHeaderFieldDefinitions.RETRY_AFTER).getValue());

        assertTrue(this.responseMessage.getFields().contains(field1));
        assertTrue(this.responseMessage.getFields().contains(field2));

        assertTrue(this.responseMessage.getFieldNames().contains(ResponseHeaderFieldDefinitions.AUTHORIZATION));
        assertTrue(this.responseMessage.getFieldNames().contains(ResponseHeaderFieldDefinitions.RETRY_AFTER));
    }

    @Test
    public void testGetFieldNames() throws Exception {
        this.responseMessage.addField(ResponseHeaderFieldDefinitions.AUTHORIZATION, "someValue");
        this.responseMessage.addField(ResponseHeaderFieldDefinitions.RETRY_AFTER, "5");

        final Set<ResponseHeaderFieldDefinitions> fieldNames = this.responseMessage.getFieldNames();
        assertNotNull(fieldNames);
        assertTrue(fieldNames.contains(ResponseHeaderFieldDefinitions.AUTHORIZATION));
        assertTrue(fieldNames.contains(ResponseHeaderFieldDefinitions.RETRY_AFTER));
    }

    @Test
    public void testHasEntity_noEntity() {
        assertFalse(this.responseMessage.hasEntity());
    }

    @Test
    public void testHasEntity_withEntity() {
        final Entity entity = mock(Entity.class);
        this.responseMessage.setEntity(entity);
        assertTrue(this.responseMessage.hasEntity());
    }

    @Test
    public void testGetSetEntity() {
        final Entity entity = mock(Entity.class);
        this.responseMessage.setEntity(entity);
        assertEquals(entity, this.responseMessage.getEntity());
    }
}
