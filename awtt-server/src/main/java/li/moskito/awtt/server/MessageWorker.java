/**
 * 
 */
package li.moskito.awtt.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import li.moskito.awtt.protocol.MessageChannel;
import li.moskito.awtt.util.Channels;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Message worker receives data from a client socket and passes it to a channel of the protocol of the port is
 * 
 * @author Gerald
 */
public class MessageWorker implements Runnable {

    /**
     * SLF4J Logger for this class
     */
    private static final Logger LOG = LoggerFactory.getLogger(MessageWorker.class);

    private final SocketChannel clientChannel;

    private final MessageChannel serverChannel;

    /**
     * @param clientChannel
     *            a socket channel from the connecting client
     * @param serverChannel
     *            a message channel to interpret and process messages received from the client
     */
    public MessageWorker(final SocketChannel clientChannel, final MessageChannel serverChannel) {

        this.clientChannel = clientChannel;
        this.serverChannel = serverChannel;
    }

    @Override
    public void run() {
        try {
            while (this.channelsOpen() && this.receiveMessage()) {
                // process the messages
                this.serverChannel.processMessages();
                // and send responses
                Channels.stream(this.serverChannel, this.clientChannel);
            }

        } catch (final IOException e) {
            LOG.error("Error processing request", e);
        } finally {
            this.closeChannels();
        }
    }

    /**
     * Checks if both channels are open
     * 
     * @return
     */
    private boolean channelsOpen() {
        return this.clientChannel.isConnected() && this.serverChannel.isOpen();
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

    /**
     * Reads a message from the client channel (receive incoming) and writes them to the server channel
     * 
     * @return
     * @throws IOException
     */
    private boolean receiveMessage() throws IOException {

        // TODO read buffer size from port config
        final ByteBuffer buffer = ByteBuffer.allocateDirect(16 * 1024);

        while (!this.serverChannel.hasMessage()) {
            // TODO add timeout!
            // TODO support partial messages
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

}
