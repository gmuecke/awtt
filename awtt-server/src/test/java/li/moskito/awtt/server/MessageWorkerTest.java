package li.moskito.awtt.server;

import java.nio.channels.SocketChannel;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MessageWorkerTest {
    @Mock
    private SocketChannel channel;

    @Mock
    private ConnectionHandlerParameters connectionParams;

    @Mock
    private Port port;
    @InjectMocks
    private MessageWorker messageWorker;

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testMessageWorker() throws Exception {
        throw new RuntimeException("not yet implemented");
    }

    @Test
    public void testGetConnectionControl() throws Exception {
        throw new RuntimeException("not yet implemented");
    }

    @Test
    public void testRun() throws Exception {
        throw new RuntimeException("not yet implemented");
    }

}
