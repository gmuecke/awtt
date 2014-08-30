package li.moskito.awtt.protocol.http;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class ContentTypeTest {

    private ContentType subject;

    @Before
    public void setUp() throws Exception {
        this.subject = new ContentType("text/plain");
    }

    @Test
    public void testGetMIMEType() throws Exception {
        assertEquals("text/plain", this.subject.getMIMEType());
    }

    @Test
    public void testToString() throws Exception {
        assertEquals("text/plain", this.subject.toString());
    }

}
