/**
 * 
 */
package li.moskito.awtt.protocol.http;

import li.moskito.awtt.protocol.HeaderField;
import li.moskito.awtt.protocol.HeaderFieldDefinition;

/**
 * A field of either a response or a request header.
 * 
 * @author Gerald
 */
public class HttpHeaderField<T extends HeaderFieldDefinition> extends HeaderField<T, String> {

    /**
     * @param headerFieldDefinition
     */
    public HttpHeaderField(final T headerFieldDefinition) {
        super(headerFieldDefinition);
    }

    /**
     * @param headerFieldDefinition
     * @param value
     */
    public HttpHeaderField(final T headerFieldDefinition, final String value) {
        super(headerFieldDefinition, value);
    }

    @Override
    public String toString() {
        return String.format("%s: %s", this.getHeaderFieldDefinition().getName(), this.getValue());
    }
}
