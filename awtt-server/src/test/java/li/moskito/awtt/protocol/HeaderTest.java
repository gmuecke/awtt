package li.moskito.awtt.protocol;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class HeaderTest {

    @Mock
    private HeaderFieldDefinition fieldDef1;
    @Mock
    private HeaderFieldDefinition fieldDef2;

    @Mock
    private HeaderField<HeaderFieldDefinition, ?> field1;
    @Mock
    private HeaderField<HeaderFieldDefinition, ?> field2;

    @Mock
    private ProtocolVersion version;

    private TestHeader header;

    public static class TestHeader extends Header {

        /**
         * @param version
         */
        public TestHeader(final ProtocolVersion version) {
            super(version);
        }
    }

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        this.header = new TestHeader(this.version);
        when(this.fieldDef1.getName()).thenReturn("field1");
        when(this.fieldDef2.getName()).thenReturn("field2");
        when(this.field1.getHeaderFieldDefinition()).thenReturn(this.fieldDef1);
        when(this.field2.getHeaderFieldDefinition()).thenReturn(this.fieldDef2);

    }

    @Test
    public void testGetVersion() throws Exception {
        assertEquals(this.version, this.header.getVersion());
    }

    @Test
    public void testGetFields() throws Exception {
        assertNotNull(this.header.getFields());
        assertTrue(this.header.getFields().isEmpty());
    }

    @Test
    public void testAddFields() throws Exception {

        final List<HeaderField<HeaderFieldDefinition, ?>> newFields = new ArrayList<>();
        newFields.add(this.field1);
        newFields.add(this.field2);
        newFields.add(this.field2); // test for removing of duplicates

        this.header.addFields(newFields);
        assertEquals(2, this.header.getFields().size());
        assertTrue(this.header.getFields().contains(this.field1));
        assertTrue(this.header.getFields().contains(this.field2));

    }

    @Test
    public void testAddField() throws Exception {
        this.header.addField(this.field1);
        assertEquals(1, this.header.getFields().size());
        assertTrue(this.header.getFields().contains(this.field1));
        this.header.getField(this.field1.getHeaderFieldDefinition());
    }

    @Test
    public void testGetHeaderFieldDefinitions() throws Exception {
        this.header.addField(this.field1);
        this.header.addField(this.field2);

        assertEquals(2, this.header.getHeaderFieldDefinitions().size());
        assertTrue(this.header.getHeaderFieldDefinitions().contains(this.field1.getHeaderFieldDefinition()));
        assertTrue(this.header.getHeaderFieldDefinitions().contains(this.field2.getHeaderFieldDefinition()));
    }

    @Test
    public void testGetField() throws Exception {
        this.header.addField(this.field1);
        assertEquals(this.field1, this.header.getField(this.field1.getHeaderFieldDefinition()));
    }

    @Test
    public void testHasField() throws Exception {
        assertFalse(this.header.hasField(this.field1.getHeaderFieldDefinition()));
        this.header.addField(this.field1);
        assertTrue(this.header.hasField(this.field1.getHeaderFieldDefinition()));
    }

}
