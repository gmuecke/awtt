package li.moskito.awtt.protocol.http;

import static org.junit.Assert.assertEquals;

import java.nio.channels.ByteChannel;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class EntityTest {

    @Mock
    private ByteChannel channel;

    @InjectMocks
    private Entity subject;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

    }

    @Test
    public void testGetByteChannel() throws Exception {
        assertEquals(this.channel, this.subject.getByteChannel());
    }

}
