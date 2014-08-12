/**
 * 
 */
package li.moskito.awtt.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.List;

import li.moskito.awtt.protocol.http.CustomHeaderField;
import li.moskito.awtt.protocol.http.HTTP;
import li.moskito.awtt.protocol.http.HttpProtocolException;
import li.moskito.awtt.protocol.http.Request;
import li.moskito.awtt.protocol.http.Response;
import li.moskito.awtt.protocol.http.ResponseHeaderFieldDefinitions;
import li.moskito.awtt.protocol.http.StatusCodes;
import li.moskito.awtt.server.handler.MessageHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gerald
 */
public class RequestWorker implements Runnable {

    /**
     * SLF4J Logger for this class
     */
    private static final Logger LOG = LoggerFactory.getLogger(RequestWorker.class);

    private final SocketChannel channel;

    private final ByteBuffer readBuffer;
    private final ByteBuffer writeBuffer;

    private final List<MessageHandler<?, ?>> handlers;

    private long timeout;
    private int numMessages;

    private final ConnectionControl connectionControl;

    /**
     * @param client
     * @param messageHandlers
     * @param connectionControl
     */
    public RequestWorker(final SocketChannel channel, final List<MessageHandler<?, ?>> handlers,
            final ConnectionControl connectionControl) {
        this.channel = channel;
        this.handlers = handlers;
        this.connectionControl = connectionControl;
        // TODO make buffer sizes configurable
        this.readBuffer = ByteBuffer.allocateDirect(1024);
        this.writeBuffer = ByteBuffer.allocateDirect(1024);
    }

    @Override
    public void run() {

        try {
            LOG.debug("Processing connection request from {}", this.channel.getRemoteAddress());

            // set the initial keep alive timer
            this.updateTimeout();
            this.updateMessageCount();

            while (this.processBuffer(this.readBuffer)) {
                // do nothing
            }

        } catch (final IOException e) {
            LOG.error("Error processing request", e);
        } finally {
            try {
                this.channel.close();
            } catch (final IOException e) {
                LOG.warn("Closing channel failed", e);
            }
        }

    }

    /**
     * Sets or Resets the message count before the connection terminates
     */
    private void updateMessageCount() {
        if (this.connectionControl.getMaxMessagesPerConnection() != ConnectionControl.UNLIMITED) {
            if (this.numMessages > 0) {
                this.numMessages--;
            } else {
                this.numMessages = this.connectionControl.getMaxMessagesPerConnection();
            }
        }
    }

    /**
     * Updates the time when the current connection times out.
     */
    private void updateTimeout() {
        this.timeout = System.currentTimeMillis() + this.connectionControl.getKeepAliveTimeout() * 1000;
    }

    /**
     * Processes the content of the last read operation on the buffer. The method parses the buffer content and responds
     * accordingly. The read data is removed after reading. The buffer does not have to be flipped before being
     * processed.
     * 
     * @param buffer
     *            the buffer to be read from
     * @return <code>true</code> if further read operations are accepted or <code>false</code> if the connection should
     *         be terminated.
     * @throws IOException
     */
    private boolean processBuffer(final ByteBuffer buffer) throws IOException {
        if (this.channel.read(this.readBuffer) == -1) {
            return false;
        }
        this.readBuffer.flip();
        // TODO parse request from channel and keep the while loop open if there are more requests
        try {

            final Request request = HTTP.parseRequest(this.readBuffer);
            final boolean keepAlive = this.handleRequest(request);

            // close connection on timeout of if connection is to be closed
            if (!keepAlive
                    || this.timeout + this.connectionControl.getKeepAliveTimeout() * 1000 < System.currentTimeMillis()) {
                return false;
            }

        } catch (final HttpProtocolException e) {
            LOG.error("Could not process request", e);
            HTTP.sendResponse(HTTP.createResponse(StatusCodes.BAD_REQUEST), this.channel);
        }
        this.readBuffer.compact(); // clear the buffer for the next read
        return true;

    }

    /**
     * Handles the request by dispatching it to one of the handlers and determines whether to keep the connection alive
     * or not.
     * 
     * @param request
     *            the request to be handled. MAY be <code>null</code>
     * @return <code>true</code> if the connection is to be left open, <code>false</code> if the connection has to be
     *         closed.
     * @throws IOException
     */
    private boolean handleRequest(final Request request) throws IOException {
        boolean keepAlive = true;
        if (request != null) {
            final Response response = this.dispatchRequest(request);

            // TODO handle disabled keep alive handling
            keepAlive = this.isKeepAlive(request);
            if (keepAlive) {
                this.addKeepAliveHeaders(response);
            }
            HTTP.sendResponse(response, this.channel);
            // reset timer
            this.updateTimeout();
            this.updateMessageCount();

        }
        return keepAlive;
    }

    /**
     * Adds the Connections and the Keep-Alive header (with the timeout and max values)
     * 
     * @param response
     *            the response to which the keep alive header should be set
     */
    private void addKeepAliveHeaders(final Response response) {
        response.addField(ResponseHeaderFieldDefinitions.CONNECTION, "Keep-Alive");
        response.addField(CustomHeaderField.forName("Keep-Alive"), String.format("timeout=%s, max=%s",
                this.connectionControl.getKeepAliveTimeout() == ConnectionControl.UNLIMITED
                        ? 5
                        : this.connectionControl.getKeepAliveTimeout(),
                this.connectionControl.getMaxMessagesPerConnection()));
    }

    /**
     * Determines whether to keep alive the connection
     * 
     * @param request
     * @return
     */
    private boolean isKeepAlive(final Request request) {
        final boolean messagesLeft = this.numMessages > 0
                || this.connectionControl.getMaxMessagesPerConnection() == ConnectionControl.UNLIMITED;
        final boolean timeLeft = this.timeout > System.currentTimeMillis()
                || this.connectionControl.getKeepAliveTimeout() == ConnectionControl.UNLIMITED;
        return messagesLeft && timeLeft && HTTP.isKeepAlive(request);
    }

    /**
     * Processes the request by dispatching it to the handlers. The handlers are processed in order of configuration.
     * The first handler that accepts the request will break the chain. All following handlers are skipped.
     * 
     * @param request
     *            the request to be dispatched
     * @throws IOException
     */
    private Response dispatchRequest(final Request request) throws IOException {
        LOG.info("Processing Request {}", request);
        for (final MessageHandler<?, ?> handler : this.handlers) {
            // TODO refactor request worker
            @SuppressWarnings("unchecked")
            final MessageHandler<Request, Response> httpHandler = (MessageHandler<Request, Response>) handler;
            if (httpHandler.accepts(request)) {
                return httpHandler.process(request);
            }
        }
        return HTTP.createResponse(StatusCodes.NOT_IMPLEMENTED);
    }

}
