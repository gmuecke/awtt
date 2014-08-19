/**
 * 
 */
package li.moskito.awtt.protocol.http;

import li.moskito.awtt.protocol.HeaderField;
import li.moskito.awtt.protocol.HeaderFieldDefinition;

/**
 * A field of either a response or a request header. The http header allows {@link String} values only.
 * 
 * @author Gerald
 */
public class HttpHeaderField extends HeaderField {

    /**
     * @param headerFieldDefinition
     */
    public HttpHeaderField(final HeaderFieldDefinition headerFieldDefinition) {
        super(headerFieldDefinition);
    }

    /**
     * @param headerFieldDefinition
     * @param value
     */
    public HttpHeaderField(final HeaderFieldDefinition headerFieldDefinition, final String value) {
        super(headerFieldDefinition, value);
    }

    @Override
    public String toString() {
        return String.format("%s: %s", this.getHeaderFieldDefinition().getName(), this.getValue());
    }

    @Override
    public String getValue() {
        return (String) super.getValue();
    }

    public void setValue(final String value) {
        super.setValue(value);
    }

    @Override
    public void setValue(final Object value) {
        this.setValue((String) value);
    }
}
