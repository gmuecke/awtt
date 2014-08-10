package li.moskito.awtt.common;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class CustomContentTypeTest {

    private CustomContentType subject;

    @Before
    public void setUp() throws Exception {
        this.subject = new CustomContentType("text/plain");
    }

    @Test
    public void testGetMIMEType() throws Exception {
        assertEquals("text/plain", this.subject.getMIMEType());
    }

}
