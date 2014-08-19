package li.moskito.awtt.protocol;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ProtocolRegistryTest {

    @Mock
    private Protocol protocol;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetInstance() throws Exception {
        final ProtocolRegistry registry = ProtocolRegistry.getInstance();
        assertNotNull(registry);
        assertSame(registry, ProtocolRegistry.getInstance());
    }

    @Test
    public void testIsAvailable() throws Exception {
        assertFalse(ProtocolRegistry.getInstance().isAvailable("http"));
    }

    @Test
    public void testForName() throws Exception {
        assertNull(ProtocolRegistry.getInstance().forName("http"));

    }

    @Test
    public void testRegisterProtocol() throws Exception {
        final ProtocolRegistry registry = ProtocolRegistry.getInstance();
        registry.registerProtocol("test", this.protocol);
        assertTrue(registry.isAvailable("test"));
        assertEquals(this.protocol, registry.forName("test"));
    }

    @Test(expected = IllegalStateException.class)
    public void testRegisterProtocol_alreadyRegistered() throws Exception {
        final ProtocolRegistry registry = ProtocolRegistry.getInstance();
        registry.registerProtocol("test2", this.protocol);
        registry.registerProtocol("test2", this.protocol);

    }

}
