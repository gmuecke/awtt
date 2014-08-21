/**
 * 
 */
package li.moskito.awtt.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import li.moskito.awtt.protocol.MessageChannel;
import li.moskito.awtt.protocol.MessageChannelOptions;
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
     * Scheduler for monitoring connection timeouts
     */
    private final ScheduledExecutorService scheduler;

    private final long keepAliveTimeout;

    /**
     * @param clientChannel
     *            a socket channel from the connecting client
     * @param serverChannel
     *            a message channel to interpret and process messages received from the client
     */
    public MessageWorker(final SocketChannel clientChannel, final MessageChannel serverChannel) {

        this.clientChannel = clientChannel;
        this.serverChannel = serverChannel;

        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.keepAliveTimeout = serverChannel.getOption(MessageChannelOptions.KEEP_ALIVE_TIMEOUT).longValue();
    }

    @Override
    public void run() {

        // create a reusable timeou task for the current thread
        final ConnectionTimeout timeout = new ConnectionTimeout(Thread.currentThread(), this.keepAliveTimeout);

        try {
            LOG.info("Connection from {}", this.clientChannel.getRemoteAddress());
            while (this.channelsOpen() && this.receiveMessage(timeout)) {
                // process the messages
                this.serverChannel.processMessages();
                // and send responses
                Channels.stream(this.serverChannel, this.clientChannel);
            }

        } catch (final ClosedByInterruptException e) {
            LOG.debug("Connection terminated by timeout", e);
        } catch (final IOException e) {
            LOG.error("Error processing request", e);
        } finally {
            this.closeChannels();
        }

        LOG.debug("Connection closed");
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
     * @param timeout
     *            Task that interrupts the current thread if the timeout is reached
     * @return
     * @throws IOException
     */
    private boolean receiveMessage(final ConnectionTimeout timeout) throws IOException {

        // TODO read buffer size from port config
        final ByteBuffer buffer = ByteBuffer.allocateDirect(16 * 1024);

        while (!this.serverChannel.hasMessage()) {

            // TODO support partial messages
            if (!this.receiveData(buffer, timeout)) {
                return false;
            }
        }
        return true;
    }

    /**
     * This blocking method read from the clientChannel into the buffer and writes from the buffer to the server
     * channel.
     * 
     * @param buffer
     *            the buffer to be used for the transfer of the received data
     * @param timeout
     *            the task that is performed once the timeout is reached. The task interrupts the read operation
     * @return <code>true</code> if data was read or <code>false</code> if the client sent EOF
     * @throws IOException
     * @throws ClosedByInterruptException
     *             if the blocking read operation was interruped
     */
    private boolean receiveData(final ByteBuffer buffer, final ConnectionTimeout timeout) throws IOException {
        /*
         * The blocking read operation that needs to be monitored. Once the timeout is reached, this thread will be
         * interrupted, stopping the read operation
         */
        final ScheduledFuture<?> timer = this.startTimer(timeout);

        final boolean result;
        if (this.clientChannel.read(buffer) != -1) {
            buffer.flip();
            this.serverChannel.write(buffer);
            buffer.compact();
            result = true;
        } else {
            result = false;
        }
        // stop the timer if data has been read
        this.stopTimer(timer);
        return result;
    }

    /**
     * Starts the timer that ivokes the ConnectionTimeout if the time is up.
     * 
     * @param timeout
     *            the timeout task that is executed if the time is up.
     * @return the future for cancelling the timer or <code>null</code> if the timeout value of the timeout task is 0 or
     *         negative.
     */
    @SuppressWarnings("rawtypes")
    private ScheduledFuture startTimer(final ConnectionTimeout timeout) {
        final ScheduledFuture<?> future;
        if (timeout.getTimeout() > 0) {
            future = this.scheduler.schedule(timeout, timeout.getTimeout(), TimeUnit.SECONDS);
        } else {
            future = null;
        }
        return future;
    }

    /**
     * Cancells the timer task from execution
     * 
     * @param future
     */
    private void stopTimer(final ScheduledFuture<?> future) {
        if (future != null) {
            future.cancel(true);
        }
    }

    /**
     * Runnable that is executed if the connection timed out and interrupts the thread that is assigned to the instance
     * 
     * @author Gerald
     */
    private static final class ConnectionTimeout implements Runnable {

        private final Thread monitoredThread;
        private final long timeout;

        public ConnectionTimeout(final Thread monitoredThread, final long timeout) {
            this.monitoredThread = monitoredThread;
            this.timeout = timeout;
        }

        @Override
        public void run() {
            this.monitoredThread.interrupt();
        }

        public long getTimeout() {
            return this.timeout;
        }

    }

}
