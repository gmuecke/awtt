package li.moskito.awtt.server;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;

import java.nio.channels.SocketChannel;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MessageWorkerTest {
    @Mock
    private SocketChannel channel;

    @Mock
    private ConnectionHandlerParameters connectionParams;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Port port;

    @InjectMocks
    private MessageWorker messageWorker;

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void testGetConnectionControl() throws Exception {
        assertEquals(this.connectionParams, this.messageWorker.getConnectionControl());
    }

    @Test
    public void testRun() throws Exception {
        this.messageWorker.run();

        verify(this.channel).isConnected();
        verify(this.channel).close();
    }

}
