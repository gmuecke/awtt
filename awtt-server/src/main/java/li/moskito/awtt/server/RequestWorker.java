/**
 * 
 */
package li.moskito.awtt.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

import li.moskito.awtt.protocol.http.HTTP;
import li.moskito.awtt.protocol.http.HttpProtocolException;
import li.moskito.awtt.protocol.http.Request;
import li.moskito.awtt.protocol.http.Response;
import li.moskito.awtt.protocol.http.StatusCodes;
import li.moskito.awtt.protocol.http.Version;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gerald
 */
public class RequestWorker implements Runnable {

    /**
     * SLF4J Logger for this class
     */
    private static final Logger  LOG = LoggerFactory.getLogger(RequestWorker.class);

    private final SocketChannel  channel;

    private final ByteBuffer     readBuffer;
    private final ByteBuffer     writeBuffer;
    private final Charset        charset;

    private final RequestHandler handler;

    private final long           keepAliveTimeout;

    /**
     * @param channel
     */
    public RequestWorker(final SocketChannel channel, final RequestHandler handler) {
        this.channel = channel;
        this.handler = handler;
        // TODO make buffer sizes configurable
        this.readBuffer = ByteBuffer.allocateDirect(1024);
        this.writeBuffer = ByteBuffer.allocateDirect(1024);
        this.charset = Charset.forName("ISO-8859-1"); // TODO make charset configurable
        this.keepAliveTimeout = 5000L;
    }

    @Override
    public void run() {

        try {

            LOG.info("Processing connection request from {}", this.channel.getRemoteAddress());
            // TODO add a timeout to terminate kept-alive connection
            final long start = System.currentTimeMillis();
            while (this.channel.read(this.readBuffer) != -1
                    || start + this.keepAliveTimeout > System.currentTimeMillis()) {
                this.readBuffer.flip();

                final Request request = HTTP.parseRequest(this.charset.decode(this.readBuffer));
                final Response response = this.handleRequest(request);
                HTTP.sendResponse(response, this.channel, this.charset);
                this.readBuffer.compact(); // clear the buffer for the next read
                return;
            }

        } catch (final IOException e) {
            LOG.error("Error processing request", e);
        } catch (final HttpProtocolException e) {
            LOG.error("Could not process request", e);
            this.writeBuffer.put((Version.HTTP_1_1 + " " + StatusCodes.CLIENT_ERR_400_BAD_REQUEST + "\r\n").getBytes());
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
        if (this.handler.accepts(request)) {
            return this.handler.process(request);
        }
        return null;
    }

}
