package li.moskito.awtt.protocol;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class HeaderFieldTest {
    @Mock
    private Object headerFieldDefinition;

    @Mock
    private Object value;

    @InjectMocks
    private HeaderField<Object, Object> headerField;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testHeaderField_Name() throws Exception {
        this.headerField = new HeaderField<>(this.headerFieldDefinition);
        assertEquals(this.headerFieldDefinition, this.headerField.getHeaderFieldDefinition());
        assertNull(this.headerField.getValue());

    }

    @Test
    public void testHeaderField_NameAndValue() throws Exception {
        this.headerField = new HeaderField<>(this.headerFieldDefinition, this.value);
        assertEquals(this.headerFieldDefinition, this.headerField.getHeaderFieldDefinition());
        assertEquals(this.value, this.headerField.getValue());
    }

    @Test
    public void testGetValue() throws Exception {
        this.headerField.setValue(this.value);
        assertEquals(this.value, this.headerField.getValue());
    }

}
