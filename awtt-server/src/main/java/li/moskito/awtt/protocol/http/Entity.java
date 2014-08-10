/**
 * 
 */
package li.moskito.awtt.protocol.http;

import java.nio.channels.ByteChannel;
import java.nio.channels.ReadableByteChannel;

/**
 * @author Gerald
 */
public class Entity {

    private final ByteChannel byteChannel;
    private final boolean binary;

    /**
     * Constructor to be used for non-binary content (i.e. text files)
     * 
     * @param newByteChannel
     */
    public Entity(final ByteChannel newByteChannel) {
        this(newByteChannel, false);
    }

    /**
     * Constructor to be used for binary content.
     * 
     * @param newByteChannel
     * @param binary
     *            flag to indicate the content is binary content
     */
    public Entity(final ByteChannel newByteChannel, final boolean binary) {
        this.byteChannel = newByteChannel;
        this.binary = binary;
    }

    public ReadableByteChannel getByteChannel() {
        return this.byteChannel;
    }

    /**
     * @return
     */
    public boolean isBinary() {
        return this.binary;
    }

}
