/**
 * 
 */
package li.moskito.awtt.protocol;

import java.nio.channels.ReadableByteChannel;


/**
 * Denotes a partial message that provides binary data. This message is used when data from a specific source should be
 * read.
 * 
 * @author Gerald
 */
public class BinaryDataMessage extends Message {

    private final ReadableByteChannel dataChannel;

    /**
     * @param header
     */
    private BinaryDataMessage(final ReadableByteChannel dataChannel) {
        super(null);
        this.dataChannel = dataChannel;
    }

    public ReadableByteChannel getDataChannel() {
        return this.dataChannel;
    }

}
