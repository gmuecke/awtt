/**
 * 
 */
package li.moskito.awtt.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import li.moskito.awtt.protocol.Message;
import li.moskito.awtt.protocol.MessageChannel;
import li.moskito.awtt.protocol.Protocol;
import li.moskito.awtt.util.Channels;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gerald
 */
public class MessageWorker implements Runnable {

    /**
     * SLF4J Logger for this class
     */
    private static final Logger LOG = LoggerFactory.getLogger(MessageWorker.class);

    private final SocketChannel clientChannel;

    private final Port port;
    private final ConnectionHandlerParameters connectionParams;

    private final MessageChannel serverChannel;

    private long timeout;

    private int numMessages;

    /**
     * @param client
     * @param messageHandlers
     * @param connectionParams
     */
    public MessageWorker(final SocketChannel channel, final Port port,
            final ConnectionHandlerParameters connectionParams) {
        this.clientChannel = channel;
        this.port = port;
        this.serverChannel = port.getProtocol().openChannel();
        this.connectionParams = connectionParams;
    }

    /**
     * Returns the connection control element providing connection specific parameters that may influence the behavior
     * of the message processing
     * 
     * @return the {@link ConnectionHandlerParameters} bean
     */
    public ConnectionHandlerParameters getConnectionControl() {
        return this.connectionParams;
    }

    @Override
    public void run() {

        this.initializeProcessing();

        try {
            LOG.debug("Processing connection from {}", this.clientChannel.getRemoteAddress());

            while (this.clientChannel.isConnected() && this.receiveMessage()) {

                this.updateMessageCount();
                this.updateTimeout();

                final Message request = this.serverChannel.readMessage();
                final boolean keepAlive = this.processMessage(request);

                if (!keepAlive) {
                    break;
                }
            }

        } catch (final IOException e) {
            LOG.error("Error processing request", e);
        } finally {
            this.closeChannels();
        }
    }

    /**
     * Processes the request using the specified protocol.
     * 
     * @param request
     *            the message to be processed
     * @return flag to indicate whether to close the connection after the processing (<code>false</code>) or to keep it
     *         open (<code>true</code>).
     * @throws IOException
     */
    private boolean processMessage(final Message request) throws IOException {

        @SuppressWarnings("unchecked")
        final Protocol<Message, Message, MessageChannel> protocol = (Protocol<Message, Message, MessageChannel>) this.port
                .getProtocol();

        final Message response = protocol.process(request);

        final boolean keepAlive = this.isKeepAlive(protocol, request);

        if (response != null) {

            if (keepAlive) {
                response.getHeader().addFields(protocol.getKeepAliverHeaders(this.connectionParams));
            }

            this.serverChannel.write(response);
            Channels.stream(this.serverChannel, this.clientChannel);
        }

        return keepAlive;
    }

    /**
     * Determines if the connection should be kept alive after the processing of a message or not.
     * 
     * @param protocol
     *            the protocol that is used to interpret the requestor information in the request
     * @param request
     *            the request containing possibly information from the information how to handle the connection
     * @return <code>true</code> if the connection should be kept alive
     */
    private boolean isKeepAlive(final Protocol<Message, Message, MessageChannel> protocol, final Message request) {
        final boolean keepAlive = !protocol.isCloseChannelsAfterProcess(request);
        final boolean timeoutReached = this.timeout < System.currentTimeMillis();
        final boolean messageLimitReached = this.numMessages == 0;
        return keepAlive && !timeoutReached && !messageLimitReached;
    }

    /**
     * Closes client and server channel
     */
    private void closeChannels() {
        try {
            this.clientChannel.close();
        } catch (final IOException e) {
            LOG.warn("Closing client channel failed", e);
        }
        try {
            this.serverChannel.close();
        } catch (final IOException e) {
            LOG.warn("Closing http channel failed", e);
        }
    }

    private void initializeProcessing() {
        // set the initial keep alive timer
        this.updateTimeout();
        this.updateMessageCount();
    }

    private boolean receiveMessage() throws IOException {

        final ByteBuffer buffer = ByteBuffer.allocateDirect(16 * 1024); // TODO read buffer size from port config

        while (!this.serverChannel.hasMessage()) {

            // TODO add timeout!
            if (this.clientChannel.read(buffer) != -1) {
                buffer.flip();
                this.serverChannel.write(buffer);
                buffer.compact();
            } else {
                return false;
            }

        }
        return true;
    }

    /**
     * Sets or Resets the message count before the connection terminates
     */
    private void updateMessageCount() {
        if (this.connectionParams.getMaxMessagesPerConnection() != ConnectionHandlerParameters.UNLIMITED) {
            if (this.numMessages > 0) {
                this.numMessages--;
            } else {
                this.numMessages = this.connectionParams.getMaxMessagesPerConnection();
            }
        }
    }

    /**
     * Updates the time when the current connection times out.
     */
    private void updateTimeout() {
        this.timeout = System.currentTimeMillis() + this.connectionParams.getKeepAliveTimeout() * 1000;
    }

}
