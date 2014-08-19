package li.moskito.awtt.protocol;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Before;
import org.junit.Test;

public class CustomHeaderFieldDefinitionTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testForName() throws Exception {
        final CustomHeaderFieldDefinition field1 = CustomHeaderFieldDefinition.forName("test");
        final CustomHeaderFieldDefinition field2 = CustomHeaderFieldDefinition.forName("test");
        assertSame(field1, field2);
    }

    @Test
    public void testGetName() throws Exception {
        final CustomHeaderFieldDefinition field = CustomHeaderFieldDefinition.forName("test");
        assertEquals("test", field.getName());
    }

}
