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

    /**
     * Default constructor
     * 
     * @param newByteChannel
     */
    public Entity(final ByteChannel byteChannel) {
        this.byteChannel = byteChannel;
    }

    public ReadableByteChannel getByteChannel() {
        return this.byteChannel;
    }

}
