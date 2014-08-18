package li.moskito.awtt.protocol;

import static org.junit.Assert.assertEquals;

import java.nio.channels.ByteChannel;

import li.moskito.awtt.protocol.BinaryBody;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class BinaryBodyTest {

    @Mock
    private ByteChannel channel;

    @InjectMocks
    private BinaryBody subject;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

    }

    @Test
    public void testGetByteChannel() throws Exception {
        assertEquals(this.channel, this.subject.getByteChannel());
    }

}
