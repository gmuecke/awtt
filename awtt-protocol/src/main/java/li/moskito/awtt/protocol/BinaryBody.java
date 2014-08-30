/**
 * 
 */
package li.moskito.awtt.protocol;

import java.nio.channels.ByteChannel;
import java.nio.channels.ReadableByteChannel;

/**
 * An body consisting of binary data.
 * 
 * @author Gerald
 */
public class BinaryBody implements Body {

    private final ByteChannel byteChannel;

    /**
     * @param byteChannel
     *            the channel containing or accepting binary data of the body
     */
    public BinaryBody(final ByteChannel byteChannel) {
        this.byteChannel = byteChannel;
    }

    /**
     * The channel for reading or writing binary data of the body.
     */
    public ReadableByteChannel getByteChannel() {
        return this.byteChannel;
    }

}
