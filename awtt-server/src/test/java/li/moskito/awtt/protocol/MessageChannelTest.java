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
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

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
    public void testRead_ByteBuffer() throws Exception {
        final String expectedMessage = "TestMessage";
        when(this.channel.serializeHeader(any(Header.class))).thenReturn(CharBuffer.wrap(expectedMessage));
        // write a message to the channel
        this.subject.write(this.outMessage);
        // read the message in the channel to byteBuffer
        this.subject.read(this.buffer);
        this.buffer.flip();

        final String actualMessage = StandardCharsets.ISO_8859_1.decode(this.buffer).toString();
        assertEquals(actualMessage, expectedMessage);
    }

    @Test
    public void testRead_ByteBuffer_resume() throws Exception {

        final String expectedMessage = "TestMessage";
        final String expectedContent = "TestContent";
        // create a binary body using a file
        final ByteChannel srcData = this.createBinaryChannel(expectedContent);
        final Path tempFile = Files.createTempFile("binaryChannelData", "txt");
        // create a target to stream to (which is a file too)
        final FileChannel dest = FileChannel.open(tempFile, StandardOpenOption.WRITE);

        // prepare message & mocked part of the channel
        when(this.channel.serializeHeader(any(Header.class))).thenReturn(CharBuffer.wrap(expectedMessage));
        final BinaryBody binBody = mock(BinaryBody.class);
        when(this.outMessage.getBody()).thenReturn(binBody);
        when(binBody.getByteChannel()).thenReturn(srcData);

        // write a message to the channel
        this.subject.write(this.outMessage);

        // copy from the subject to the dest (this invokes the read method)
        this.copy(this.subject, dest, ByteBuffer.allocate(16));
        srcData.close();
        dest.close();

        assertEquals(expectedMessage + expectedContent, new String(Files.readAllBytes(tempFile)));
    }

    @Test(expected = BufferOverflowException.class)
    public void testRead_ByteBuffer_Overflow() throws Exception {
        final String expectedMessage = "TestMessage";
        // prepare message & mocked part of the channel
        when(this.channel.serializeHeader(any(Header.class))).thenReturn(CharBuffer.wrap(expectedMessage));

        // write a message to the channel
        this.subject.write(this.outMessage);

        // read into a buffer that is too small to take the entire header
        this.subject.read(ByteBuffer.allocate(5));
    }

    /**
     * Copies the data from the source channel to the dest channel using the specified buffer as transfer buffer
     * 
     * @param src
     * @param dest
     * @param smallBuffer
     * @throws IOException
     */
    private void copy(final ReadableByteChannel src, final WritableByteChannel dest, final ByteBuffer smallBuffer)
            throws IOException {
        while (src.read(smallBuffer) != -1) {
            smallBuffer.flip();
            dest.write(smallBuffer);
            smallBuffer.compact();
        }
        smallBuffer.flip();
        while (smallBuffer.hasRemaining()) {
            dest.write(smallBuffer);
        }
    }

    @Test(expected = IOException.class)
    public void testRead_ByteBuffer_channelClosed() throws Exception {
        this.subject.close();
        this.subject.read(this.buffer);
        this.buffer.flip();
    }

    @Test
    public void testWrite_ByteBuffer() throws Exception {
        when(this.channel.parseMessage(this.buffer)).thenReturn(this.inMessage);

        // write from byteBuffer
        this.subject.write(this.buffer);

        verify(this.channel).parseMessage(this.buffer);
        assertTrue(this.subject.hasMessage());
    }

    @Test(expected = IOException.class)
    public void testWrite_ByteBuffer_channelClosed() throws Exception {
        this.subject.close();

        // write from byteBuffer
        this.subject.write(this.buffer);
    }

    @Test
    public void testWriteBody_noBinaryBody() throws IOException {
        final Body body = mock(Body.class);
        final int initialPosition = this.buffer.position();
        assertEquals(-1, this.subject.writeBody(body, this.buffer));
        assertEquals(initialPosition, this.buffer.position());
    }

    @Test
    public void testWriteBody_binaryBody_noContent() throws IOException {
        final Body body = mock(BinaryBody.class);
        final int initialPosition = this.buffer.position();
        assertEquals(-1, this.subject.writeBody(body, this.buffer));
        assertEquals(initialPosition, this.buffer.position());
    }

    @Test
    public void testWriteBody_binaryBody_binaryContent_complete() throws IOException {
        // prepare binary channel (from file)
        final String testData = "TestContent";
        final ByteChannel ch = this.createBinaryChannel(testData);
        // prepare body
        final BinaryBody body = mock(BinaryBody.class);
        when(body.getByteChannel()).thenReturn(ch);

        final int initialPosition = this.buffer.position();
        assertEquals(testData.getBytes().length, this.subject.writeBody(body, this.buffer)); // eof
        assertEquals(initialPosition + testData.getBytes().length, this.buffer.position());
        this.buffer.flip();
        final String writtenBody = StandardCharsets.ISO_8859_1.decode(this.buffer).toString();
        assertEquals(testData, writtenBody);
    }

    /**
     * Creates a BinaryChannel delivering the testContent. The method creates a temporary file with the content and
     * opens a file channel on that file
     * 
     * @param testContent
     * @return
     * @throws IOException
     */
    private ByteChannel createBinaryChannel(final String testContent, final OpenOption... openOptions)
            throws IOException {
        final byte[] testBinaryContent = testContent.getBytes();
        final Path tempFile = Files.createTempFile("binaryChannelData", "txt");
        Files.write(tempFile, testBinaryContent);
        final FileChannel ch = FileChannel.open(tempFile, openOptions);
        return ch;
    }

    @Test
    public void testWriteBody_binaryBody_binaryContent_partial() throws IOException {
        // prepare binary channel (from file)
        final String testcontent = "TestContent";
        final byte[] testContent = testcontent.getBytes();
        final Path tempFile = Files.createTempFile("bodyDate", "txt");
        Files.write(tempFile, testContent);
        final FileChannel ch = FileChannel.open(tempFile);
        // prepare body
        final BinaryBody body = mock(BinaryBody.class);
        when(body.getByteChannel()).thenReturn(ch);
        // prepare buffer
        final int bufferSize = testcontent.length() - 1; // smaller than input!
        final ByteBuffer smallBuffer = ByteBuffer.allocate(bufferSize);
        // act
        assertEquals(bufferSize, this.subject.writeBody(body, smallBuffer));

        // assert
        assertEquals(bufferSize, smallBuffer.position());
        smallBuffer.flip();
        final String writtenBody = StandardCharsets.ISO_8859_1.decode(smallBuffer).toString();
        assertEquals(testcontent.substring(0, bufferSize), writtenBody);
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
