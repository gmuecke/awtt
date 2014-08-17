package li.moskito.awtt.protocol;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

public class MessageChannelTest {

    public static class TestMessageChannel extends MessageChannel {

        private Protocol<?, ?, ?> protocol;

        @Override
        public Protocol<?, ?, ?> getProtocol() {
            return this.protocol;
        }

        @Override
        protected Message parseMessage(final ByteBuffer src) throws ProtocolException, IOException {
            return null;
        }

        @Override
        protected CharBuffer serializeHeader(final Header header) {
            return null;
        }

    }

    @Spy
    private TestMessageChannel channel;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testRead() throws Exception {
        throw new RuntimeException("not yet implemented");
    }

    @Test
    public void testWriteByteBuffer() throws Exception {
        throw new RuntimeException("not yet implemented");
    }

    @Test
    public void testIsOpen() throws Exception {
        throw new RuntimeException("not yet implemented");
    }

    @Test
    public void testClose() throws Exception {
        throw new RuntimeException("not yet implemented");
    }

    @Test
    public void testSuspendWriteToBuffer() throws Exception {
        throw new RuntimeException("not yet implemented");
    }

    @Test
    public void testHasPartiallyWrittenMessage() throws Exception {
        throw new RuntimeException("not yet implemented");
    }

    @Test
    public void testDiscardPartiallyWrittenMessage() throws Exception {
        throw new RuntimeException("not yet implemented");
    }

    @Test
    public void testSuspendReadFromBuffer() throws Exception {
        throw new RuntimeException("not yet implemented");
    }

    @Test
    public void testHasPartiallyReadMessage() throws Exception {
        throw new RuntimeException("not yet implemented");
    }

    @Test
    public void testDiscardPartiallyReadMessage() throws Exception {
        throw new RuntimeException("not yet implemented");
    }

    @Test
    public void testWriteMessage() throws Exception {
        throw new RuntimeException("not yet implemented");
    }

    @Test
    public void testHasMessage() throws Exception {
        throw new RuntimeException("not yet implemented");
    }

    @Test
    public void testReadMessage() throws Exception {
        throw new RuntimeException("not yet implemented");
    }

}
