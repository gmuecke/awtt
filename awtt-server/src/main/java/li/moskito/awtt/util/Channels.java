/**
 * 
 */
package li.moskito.awtt.util;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bmsi.util.Base64CharsetProvider;

/**
 * @author Gerald
 */
public class Channels {

    /**
     * SLF4J Logger for this class
     */
    private static final Logger LOG = LoggerFactory.getLogger(Channels.class);

    private static final Base64CharsetProvider base64charsetProvider = new Base64CharsetProvider();

    public static void stream(final ReadableByteChannel src, final WritableByteChannel dest) throws IOException {
        // streaming implementation from
        // http://thomaswabner.wordpress.com/2007/10/09/fast-stream-copy-using-javanio-channels/
        // BEGIN COPIED CODE
        final ByteBuffer buffer = ByteBuffer.allocateDirect(16 * 1024);
        while (src.read(buffer) != -1) {
            // prepare the buffer to be drained
            buffer.flip();
            // write to the channel, may block
            dest.write(buffer);
            // If partial transfer, shift remainder down
            // If buffer is empty, same as doing clear()
            buffer.compact();
        }
        // EOF will leave buffer in fill state
        buffer.flip();
        // make sure the buffer is fully drained.
        while (buffer.hasRemaining()) {
            dest.write(buffer);
        }
        // END COPIED CODE
    }

    // /**
    // * Streams the content from the readable byte channel to the destination using the given charset encoding.
    // *
    // * @param src
    // * @param dest
    // * @param targetCharset
    // * @throws IOException
    // */
    // public static void streamBase64(final ReadableByteChannel src, final WritableByteChannel dest) throws IOException
    // {
    //
    // final Charset base64 = base64charsetProvider.charsetForName("BASE64");
    // // streaming implementation from
    // // http://thomaswabner.wordpress.com/2007/10/09/fast-stream-copy-using-javanio-channels/
    // // BEGIN COPIED CODE
    // final ByteBuffer buffer = ByteBuffer.allocateDirect(16 * 1024);
    // while (src.read(buffer) != -1) {
    // // prepare the buffer to be drained
    // buffer.flip();
    // // BEGIN ADDITION Base64 encoding
    // // write to the channel, may block
    // final CharBuffer cb = base64.decode(buffer);
    // LOG.info("streaming encoded data[\n{}\n]", cb.toString());
    // dest.write(StandardCharsets.ISO_8859_1.encode(cb));
    // // END ADDITION
    // // If partial transfer, shift remainder down
    // // If buffer is empty, same as doing clear()
    // buffer.compact();
    // }
    // // EOF will leave buffer in fill state
    // buffer.flip();
    // // make sure the buffer is fully drained.
    // while (buffer.hasRemaining()) {
    // dest.write(buffer);
    // }
    // // END COPIED CODE
    // }
}
