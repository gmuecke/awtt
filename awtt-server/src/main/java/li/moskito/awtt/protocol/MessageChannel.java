/**
 * 
 */
package li.moskito.awtt.protocol;

import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import li.moskito.awtt.protocol.http.HttpProtocolException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A channel to read or write messages.
 * 
 * @author Gerald
 */
public abstract class MessageChannel implements ByteChannel {

    /**
     * SLF4J Logger for this class
     */
    private static final Logger LOG = LoggerFactory.getLogger(MessageChannel.class);

    /**
     * The queue in which messages are kept for being written to ByteBuffer upon invocation of the read method.
     */
    private final Queue<Message> outMessageQueue;
    /**
     * The queue in which messages are kept from being read from a ByteBuffer upon invocation of the write method.
     */
    private final Queue<Message> inMessageQueue;
    /**
     * The last partialWrittenMessage
     */
    private Message partialWrittenMessage;
    /**
     * The last partialReadMessage
     */
    private Message partialReadMessage;
    /**
     * Flag to indicate whether the channel is still open
     */
    private final AtomicBoolean open;

    private final OnMessageReadCallback newMessageCallback;

    /**
     * Mode to be used for writing or reading.
     * 
     * @author Gerald
     */
    protected static enum Mode {
        /**
         * A Read or Write operation should be performed from the beginning
         */
        BEGIN,
        /**
         * A previously started read or write operation should be continued. The message passed was processed partially.
         */
        CONTINUE;
    }

    /**
     * Callback that should be used by implementing classes when a message was parsed from a ByteBuffer in the write
     * method.
     * 
     * @author Gerald
     */
    protected class OnMessageReadCallback {

        private final MessageChannel channel;

        private OnMessageReadCallback(final MessageChannel channel) {
            this.channel = channel;
        }

        /**
         * Invoke this method when a new message has been parsed.
         * 
         * @param message
         *            the new message
         */
        public void messageParsed(final Message message) {
            this.channel.inMessageQueue.offer(message);
        }
    }

    /**
     * 
     */
    public MessageChannel() {
        this.open = new AtomicBoolean(true);
        this.outMessageQueue = new ConcurrentLinkedQueue<>();
        this.inMessageQueue = new ConcurrentLinkedQueue<>();
        this.newMessageCallback = new OnMessageReadCallback(this);

    }

    @Override
    public int read(final ByteBuffer byteBuffer) throws IOException {

        if (!this.open.get()) {
            throw new IOException("MessageChannel already closed");
        }

        final Message message;
        final Mode mode;
        if (this.partialWrittenMessage != null) {
            message = this.partialWrittenMessage;
            this.partialWrittenMessage = null;
            mode = Mode.CONTINUE;
        } else if (this.outMessageQueue.peek() != null) {
            message = this.outMessageQueue.remove();
            mode = Mode.BEGIN;
        } else {
            return -1;
        }
        final int dataLength = this.writeMessageToBuffer(message, byteBuffer, mode);

        if (dataLength != -1) {
            this.suspendWriteToBuffer(message);
        }

        return dataLength;
    }

    @Override
    public int write(final ByteBuffer byteBuffer) throws IOException {
        if (!this.open.get()) {
            throw new IOException("MessageChannel already closed");
        }
        final Mode mode;

        if (this.partialReadMessage != null) {
            mode = Mode.CONTINUE;
        } else if (byteBuffer.hasRemaining()) {
            mode = Mode.BEGIN;
        } else {
            return -1;
        }
        return this.readMessageFromBuffer(byteBuffer, this.newMessageCallback, mode);
    }

    @Override
    public boolean isOpen() {
        return this.open.get();
    }

    @Override
    public void close() throws IOException {
        this.open.set(false);
    }

    /**
     * The {@link Protocol} for this message channel.
     * 
     * @return
     */
    public abstract Protocol<?, ?, ?> getProtocol();

    /**
     * Suspends the write processes and puts the partially written message on hold. Upon the next read call the message
     * will be resumed and transferred to the target buffer.
     * 
     * @param partialWrittenMessage
     *            message that has been partially written to the buffer.
     */
    protected void suspendWriteToBuffer(final Message partialWrittenMessage) {
        this.partialWrittenMessage = partialWrittenMessage;
    }

    /**
     * @return <code>true</code> when the previous read operation has not been completely fulfilled
     */
    public boolean hasPartiallyWrittenMessage() {
        return this.partialWrittenMessage != null;
    }

    /**
     * Discards the last partially read message. The message will be lost.
     * 
     * @return the partial message.
     */
    protected <T extends Message> T discardPartiallyWrittenMessage() {
        @SuppressWarnings("unchecked")
        final T partialMessage = (T) this.partialWrittenMessage;
        this.partialWrittenMessage = null;
        return partialMessage;
    }

    /**
     * Suspends the read processes and puts the partially read message on hold. Upon the next write call the message
     * will be resumed.
     * 
     * @param partialReadMessage
     *            the message that has been partially read from the input buffer.
     */
    protected void suspendReadFromBuffer(final Message partialReadMessage) {
        this.partialReadMessage = partialReadMessage;
    }

    /**
     * @return <code>true</code> when the previous write operation has not been completely fulfilled
     */
    public boolean hasPartiallyReadMessage() {
        return this.partialReadMessage != null;
    }

    /**
     * Discards the last partially written message. The message will be lost.
     * 
     * @return the partial message.
     */
    protected Message discardPartiallyReadMessage() {
        final Message partialMessage = this.partialReadMessage;
        this.partialReadMessage = null;
        return partialMessage;
    }

    /**
     * Puts a message on the output queue. The next read operation will trigger the serialization of the message to a
     * {@link ByteBuffer}
     * 
     * @param message
     *            the message to be written to the channel
     */
    public MessageChannel write(final Message message) {
        this.outMessageQueue.offer(message);
        return this;
    }

    /**
     * Checks if there are messages in the input queue that were read from buffer. Prior to this call a message as bytes
     * has to be written to the channel using the write(ByteBuffer) method. using the write method
     * 
     * @return <code>true</code> if there are messages available
     */
    public boolean hasMessage() {
        return this.inMessageQueue.peek() != null;

    }

    /**
     * Reads the next message from the input queue. Prior to this call a Message has to be written to the channel using
     * the write method.
     * 
     * @return the next message from the queue or <code>null</code> if no message is in the queue
     */
    public Message readMessage() {
        return this.inMessageQueue.poll();
    }

    /**
     * Reads and parses a message from the given buffer and notifies the callback once the message has been read.
     * 
     * @param byteBuffer
     *            the bytebuffer to write the serialized message to
     * @param newMessageCallback
     *            the callback that should be invoked when the parsing of a new message has been completed.
     * @param mode
     *            the mode in which the operation should be performed. See {@link Mode}. If the mode is CONTINUE the
     *            implementor should check if the are partiallyReadMessages() and continue the parsing of the message.
     *            The message can be obtained by the discardPartiallyReadMessage()
     * @return the number of bytes written to the buffer
     */
    protected int readMessageFromBuffer(final ByteBuffer src, final OnMessageReadCallback callback, final Mode mode)
            throws IOException {
        switch (mode) {
            case BEGIN:
                return this.readMessage(src, callback);
            case CONTINUE:
                break;
            default:
                break;
        }
        return 0;
    }

    /**
     * Reads a message from the byte buffer. Upon finishing the read operation the callback is notified that a message
     * has been read. <br>
     * The method does not support reading of large data (yet).
     * 
     * @param src
     *            the src buffer from which to read the data
     * @param callback
     *            the callback that is notified when a message has been read
     * @return the number of bytes read or -1 if the entire data has been read
     * @throws IOException
     */
    protected int readMessage(final ByteBuffer src, final OnMessageReadCallback callback) throws IOException {
        final int dataLength = src.limit();
        try {
            final Message message = this.parseMessage(src);
            callback.messageParsed(message);
        } catch (final ProtocolException e) {
            throw new IOException(e);
        }
        return src.hasRemaining()
                ? dataLength
                : -1;
    }

    /**
     * Writes a message to the buffer.
     * 
     * @param message
     *            the message to be written to the buffer
     * @param byteBuffer
     *            the bytebuffer to write the serialized message to
     * @param mode
     *            the mode in which the operation should be performed. See {@link Mode}
     * @return the number of bytes written to the buffer
     */
    protected int writeMessageToBuffer(final Message message, final ByteBuffer dst, final Mode mode) throws IOException {
        switch (mode) {
            case BEGIN:
                return this.writeMessage(message, dst);
            case CONTINUE:
                return this.writeBody(message.getBody(), dst);
            default:
                break;
        }
        return 0;
    }

    /**
     * Writes the given message to the destination buffer. The method first writes the header of the message. The buffer
     * must be capable of taking the entire header, otherwise a {@link BufferUnderflowException} will occur. If the body
     * of the message is larger than the space left (remaining) in the buffer, it is written partially and continued
     * upon the next invocation of read (see superclass). <br>
     * The default implementation writes the header of the message completely and streams the content of an optional
     * {@link BinaryBody}.
     * 
     * @param message
     *            the message to be sent
     * @param dst
     *            the destination buffer for the data to be sent
     * @return the data written or -1 if all data has been written
     * @throws IOException
     */
    protected int writeMessage(final Message message, final ByteBuffer dst) throws IOException {

        final int headerLength = this.writeHeader(message.getHeader(), dst, message.getCharset());

        final int bodyLength = this.writeBody(message.getBody(), dst);

        final int dataLength;
        if (bodyLength != -1) {
            dataLength = headerLength + bodyLength;
        } else {
            dataLength = -1;
        }
        return dataLength;
    }

    /**
     * Writes the remaining data of the channel into the buffer. If there is more data available than remaining space in
     * the buffer, the number of bytes written is returned. If the channel reached its end -1 is returned.<br>
     * The default implementation only writes bodies of type {@link BinaryBody}. Override this method for writing custom
     * body types.
     * 
     * @param partialResponse
     *            the partial response whose body should be written
     * @param dst
     *            the destination buffer to write the data to
     * @return the number of bytes written to the buffer or -1 if the channel of the body reached its end.
     * @throws IOException
     */
    protected int writeBody(final Body body, final ByteBuffer dst) throws IOException {

        if (body instanceof BinaryBody) {
            final ReadableByteChannel ch = ((BinaryBody) body).getByteChannel();
            if (ch != null) {
                final int dataLength = ch.read(dst);
                LOG.debug("Body written, {} Bytes", dataLength == -1
                        ? "until EOF"
                        : dataLength);
                if (dataLength == -1) {
                    ch.close();
                }

                return dataLength;
            }
        }
        return -1;
    }

    /**
     * Writes the header to the dst buffer. The buffer is supposed to accept the entire header, otherwise a
     * {@link BufferUnderflowException} occurs.
     * 
     * @param header
     *            the header to be written
     * @param dst
     *            the destination of the write operation
     * @param charset
     * @return the number of bytes written to the buffer (aka the header length)
     */
    protected int writeHeader(final Header header, final ByteBuffer dst, final Charset charset) {
        final ByteBuffer buf = charset.encode(this.serializeHeader(header));
        final int headerLength = buf.limit();
        // write the header
        dst.put(buf);
        LOG.debug("Header written, {} Bytes", headerLength);
        return headerLength;
    }

    /**
     * Parses an entire request from the given byteBuffer.
     * 
     * @param src
     *            the buffer containing the data that should be parsed
     * @return the parsed message
     * @throws ProtocolException
     *             if the buffer contained data that were not parseable by the underlying protocol
     * @throws IOException
     * @throws HttpProtocolException
     */
    protected abstract Message parseMessage(ByteBuffer src) throws ProtocolException, IOException;

    /**
     * Serializes a response into a CharBuffer.
     * 
     * @param httpResponse
     *            the response to be serialized
     * @return a CharBuffer containing the response in character representation
     */
    protected abstract CharBuffer serializeHeader(Header header);

}
