package li.moskito.awtt.protocol;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class MessageChannelTest {

    public static class TestMessageChannel extends MessageChannel {

        private static MessageChannel mock;

        @Override
        public Protocol getProtocol() {
            return mock.getProtocol();
        }

        @Override
        protected Message parseMessage(final ByteBuffer src) throws ProtocolException, IOException {
            return mock.parseMessage(src);
        }

        @Override
        protected CharBuffer serializeHeader(final Header header) {
            return mock.serializeHeader(header);
        }

    }
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Message inMessage;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Message outMessage;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private MessageChannel channel;

    private ByteBuffer buffer;

    @InjectMocks
    private TestMessageChannel subject;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        TestMessageChannel.mock = this.channel;
        this.buffer = ByteBuffer.allocate(16 * 1024);
        when(this.outMessage.getCharset()).thenReturn(StandardCharsets.ISO_8859_1);
    }

    @Test
    public void testRead() throws Exception {
        final String expectedMessage = "TestMessage";
        when(this.channel.serializeHeader(any(Header.class))).thenReturn(CharBuffer.wrap(expectedMessage));
        this.subject.write(this.outMessage);
        // read to byteBuffer
        this.subject.read(this.buffer);
        this.buffer.flip();

        final String actualMessage = StandardCharsets.ISO_8859_1.decode(this.buffer).toString();
        assertEquals(actualMessage, expectedMessage);

        // TODO add tests for partial writes
    }

    @Test
    public void testWriteByteBuffer() throws Exception {
        when(this.channel.parseMessage(this.buffer)).thenReturn(this.inMessage);

        // write from byteBuffer
        this.subject.write(this.buffer);

        verify(this.channel).parseMessage(this.buffer);
        assertTrue(this.subject.hasMessage());
    }

    @Test
    public void testIsOpen() throws Exception {
        assertTrue(this.subject.isOpen()); // default is true
    }

    @Test
    public void testClose() throws Exception {
        this.subject.close();
        assertFalse(this.subject.isOpen());
    }

    @Test
    public void testSuspend_and_Discard_WriteToBuffer() throws Exception {
        assertFalse(this.subject.hasPartiallyWrittenMessage());

        final Message partialMessage = mock(Message.class);
        this.subject.suspendWriteToBuffer(partialMessage);

        assertTrue(this.subject.hasPartiallyWrittenMessage());

        assertEquals(partialMessage, this.subject.discardPartiallyWrittenMessage());
        assertFalse(this.subject.hasPartiallyWrittenMessage());
    }

    @Test
    public void testHasPartiallyWrittenMessage_emptyQueue() throws Exception {
        assertFalse(this.subject.hasPartiallyWrittenMessage());
    }

    @Test
    public void testSuspend_and_Discard_ReadFromBuffer() throws Exception {
        assertFalse(this.subject.hasPartiallyReadMessage());

        final Message partialMessage = mock(Message.class);
        this.subject.suspendReadFromBuffer(partialMessage);

        assertTrue(this.subject.hasPartiallyReadMessage());

        assertEquals(partialMessage, this.subject.discardPartiallyReadMessage());
        assertFalse(this.subject.hasPartiallyReadMessage());
    }

    @Test
    public void testHasPartiallyReadMessage_emptyQueue() throws Exception {
        assertFalse(this.subject.hasPartiallyReadMessage());
    }

    @Test
    public void testHasMessage() throws Exception {
        assertFalse(this.subject.hasMessage()); // default is it doesnt have a message

    }

    @Test
    public void testReadMessage_noMessage() throws Exception {
        assertNull(this.subject.readMessage());
    }

    @Test
    public void testReadMessage_messageAvailable() throws Exception {
        when(this.channel.parseMessage(this.buffer)).thenReturn(this.inMessage);

        // write from byteBuffer
        this.subject.write(this.buffer);

        verify(this.channel).parseMessage(this.buffer);

        assertEquals(this.inMessage, this.subject.readMessage());
    }

}
