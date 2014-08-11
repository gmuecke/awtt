/**
 * 
 */
package li.moskito.awtt.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.List;

import li.moskito.awtt.protocol.http.HTTP;
import li.moskito.awtt.protocol.http.HttpProtocolException;
import li.moskito.awtt.protocol.http.Request;
import li.moskito.awtt.protocol.http.Response;
import li.moskito.awtt.protocol.http.StatusCodes;
import li.moskito.awtt.protocol.http.Version;
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

    /**
     * @param channel
     */
    public RequestWorker(final SocketChannel channel, final List<MessageHandler<?, ?>> handlers) {
        this.channel = channel;
        this.handlers = handlers;
        // TODO make buffer sizes configurable
        this.readBuffer = ByteBuffer.allocateDirect(1024);
        this.writeBuffer = ByteBuffer.allocateDirect(1024);
    }

    @Override
    public void run() {

        try {
            LOG.info("Processing connection request from {}", this.channel.getRemoteAddress());

            // final long timeout = 5000L;
            // long start = System.currentTimeMillis();

            while (this.channel.read(this.readBuffer) != -1) {
                this.readBuffer.flip();

                // TODO parse request from channel and keep the while loop open if there are more requests
                final Request request = HTTP.parseRequest(this.readBuffer);
                if (request != null) {
                    final Response response = this.handleRequest(request);
                    HTTP.sendResponse(response, this.channel);
                    // start = System.currentTimeMillis();
                }
                this.readBuffer.compact(); // clear the buffer for the next read

                // if (start + timeout < System.currentTimeMillis()) {
                // return;
                // }
                return;
            }

        } catch (final IOException e) {
            LOG.error("Error processing request", e);
        } catch (final HttpProtocolException e) {
            LOG.error("Could not process request", e);
            this.writeBuffer.put((Version.HTTP_1_1 + " " + StatusCodes.BAD_REQUEST + "\r\n").getBytes());
            this.writeBuffer.flip();
            try {
                this.channel.write(this.writeBuffer);
            } catch (final IOException iox) {
                LOG.error("Could not send response", iox);
            }
        } finally {
            try {
                this.channel.close();
            } catch (final IOException e) {
                LOG.warn("Closing channel failed", e);
            }
        }

    }

    /**
     * @param request
     * @throws IOException
     */
    private Response handleRequest(final Request request) throws IOException {
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
