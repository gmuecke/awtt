package li.moskito.awtt.protocol;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Test;

public class StandardConvertersTest {

    @Test
    public void testIdentityConverter() throws Exception {

        final String string = "test";

        assertSame(string, StandardConverters.IDENTITY_CONVERTER.convert(string));
    }

    @Test
    public void testIntegerConverter() throws Exception {

        final String string = "5";

        assertEquals(Integer.valueOf(string), StandardConverters.INTEGER_CONVERTER.convert(string));
    }

    @Test
    public void testBooleanConverter() throws Exception {

        final String string = "true";

        assertEquals(Boolean.valueOf(string), StandardConverters.BOOLEAN_CONVERTER.convert(string));
    }

}
