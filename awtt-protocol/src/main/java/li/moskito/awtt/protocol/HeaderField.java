/**
 * 
 */
package li.moskito.awtt.protocol;

/**
 * Denotes a HeaderField of a Message. The header field is associated with a {@link HeaderFieldDefinition} and a value.
 * 
 * @author Gerald
 */
public class HeaderField {

    private final HeaderFieldDefinition headerFieldDefinition;
    private Object value;

    /**
     * Constructor to create a header field without a value. The value can be set later
     * 
     * @param headerFieldDefinition
     *            the headerFieldDefinition to which this header field provides a concrete instance
     */
    public HeaderField(final HeaderFieldDefinition headerFieldDefinition) {
        super();
        this.headerFieldDefinition = headerFieldDefinition;
    }

    /**
     * Constructor to create a header field with a value.
     * 
     * @param headerFieldDefinition
     *            the headerFieldDefinition to which this header field provides a concrete instance
     * @param value
     *            the initial value of the header field.
     */
    public HeaderField(final HeaderFieldDefinition headerFieldDefinition, final Object value) {
        this(headerFieldDefinition);
        this.value = value;
    }

    /**
     * The value of the header field
     * 
     * @return
     */
    public Object getValue() {
        return this.value;
    }

    /**
     * Sets the value of the header field
     * 
     * @param value
     */
    public void setValue(final Object value) {
        this.value = value;
    }

    /**
     * The headerFieldDefinition that is associated with this headerfield
     * 
     * @return
     */
    public HeaderFieldDefinition getHeaderFieldDefinition() {
        return this.headerFieldDefinition;
    }

}
