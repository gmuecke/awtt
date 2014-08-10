/**
 * 
 */
package li.moskito.awtt.protocol.http;

/**
 * A field of either a response or a request header.
 * 
 * @author Gerald
 */
public class HeaderField<T extends HeaderFieldDefinition> {

    private final T      fieldName;
    private final String value;

    /**
     * @param field
     *            the identifier of the the field
     * @param value
     *            the value
     */
    public HeaderField(final T field, final String value) {
        super();
        this.fieldName = field;
        this.value = value;

        // TODO add validation to value
    }

    public T getFieldName() {
        return this.fieldName;
    }

    public String getValue() {
        return this.value;
    }

    @Override
    public String toString() {
        final StringBuffer buf = new StringBuffer(32);
        buf.append(this.fieldName.getName()).append(": ").append(this.value);
        return buf.toString();
    }
}
