package li.moskito.awtt.protocol;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Before;
import org.junit.Test;

public class CustomHeaderFieldTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testForName() throws Exception {
        final CustomHeaderField field1 = CustomHeaderField.forName("test");
        final CustomHeaderField field2 = CustomHeaderField.forName("test");
        assertSame(field1, field2);
    }

    @Test
    public void testGetName() throws Exception {
        final CustomHeaderField field = CustomHeaderField.forName("test");
        assertEquals("test", field.getName());
    }

}
