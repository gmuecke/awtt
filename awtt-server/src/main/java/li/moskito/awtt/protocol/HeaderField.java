/**
 * 
 */
package li.moskito.awtt.protocol;


/**
 * Denotes a HeaderField of a Message. The header field is associated with a {@link HeaderFieldDefinition} and a value.
 * 
 * @author Gerald
 * @param <N>
 *            type of the {@link HeaderFieldDefinition}
 * @param <V>
 *            type of the value of the header
 */
public class HeaderField<N, V> {

    private final N headerFieldDefinition;
    private V value;

    /**
     * Constructor to create a header field without a value. The value can be set later
     * 
     * @param headerFieldDefinition
     *            the headerFieldDefinition to which this header field provides a concrete instance
     */
    public HeaderField(final N headerFieldDefinition) {
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
    public HeaderField(final N headerFieldDefinition, final V value) {
        this(headerFieldDefinition);
        this.value = value;
    }

    /**
     * The value of the header field
     * 
     * @return
     */
    public V getValue() {
        return this.value;
    }

    /**
     * Sets the value of the header field
     * 
     * @param value
     */
    public void setValue(final V value) {
        this.value = value;
    }

    /**
     * The headerFieldDefinition that is associated with this headerfield
     * 
     * @return
     */
    public N getHeaderFieldDefinition() {
        return this.headerFieldDefinition;
    }

}
