/**
 * 
 */
package li.moskito.awtt.protocol;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import junit.runner.Version;

/**
 * The header of a {@link Protocol} {@link Message}. The header carries the {@link Version} of the protocol and a list
 * of {@link HeaderField}s.
 * 
 * @author Gerald
 */
public abstract class Header {

    private final ProtocolVersion version;

    private final Map<HeaderFieldDefinition, HeaderField<? extends HeaderFieldDefinition, ?>> fields;

    /**
     * Comparator for sorting header fields
     * 
     * @author Gerald
     */
    private static final class HeaderFieldComparator implements
            Comparator<HeaderField<? extends HeaderFieldDefinition, ?>> {
        @Override
        public int compare(final HeaderField<? extends HeaderFieldDefinition, ?> paramT1,
                final HeaderField<? extends HeaderFieldDefinition, ?> paramT2) {
            return paramT1.getHeaderFieldDefinition().getName().compareTo(paramT2.getHeaderFieldDefinition().getName());
        }
    }

    private static final HeaderFieldComparator COMPARATOR = new HeaderFieldComparator();

    public Header(final ProtocolVersion version) {
        this.version = version;
        this.fields = new ConcurrentHashMap<>();
    }

    public ProtocolVersion getVersion() {
        return this.version;
    }

    /**
     * Returns all the header fields in alphabetical order.
     * 
     * @return a list of header fields
     */
    @SuppressWarnings("unchecked")
    public <T extends HeaderField<? extends HeaderFieldDefinition, ?>> List<T> getFields() {
        final List<T> result = new ArrayList<>();
        result.addAll((Collection<? extends T>) this.fields.values());
        Collections.sort(result, COMPARATOR);
        return result;
    }

    /**
     * Adds a list of fields to this header. Existing fields will be overriden
     * 
     * @param fields
     */
    public <T extends HeaderFieldDefinition> void addFields(final List<HeaderField<T, ?>> fields) {
        for (final HeaderField<T, ?> field : fields) {
            this.addField(field);
        }
    }

    public <T extends HeaderFieldDefinition> void addField(final HeaderField<T, ?> field) {
        this.fields.put(field.getHeaderFieldDefinition(), field);
    }

    /**
     * Retrieves the headerFieldDefinition of this header
     * 
     * @return
     */
    public Set<HeaderFieldDefinition> getHeaderFieldDefinitions() {
        return this.fields.keySet();
    }

    /**
     * Retrieves a field for a specific headerFieldDefinition
     * 
     * @param headerFieldDefinition
     *            the headerFieldDefinition for which the header field should be retrieved
     * @return the corresponding HeaderField or <code>null</code> if no such field was found
     */
    @SuppressWarnings("unchecked")
    public <T extends HeaderFieldDefinition> HeaderField<T, ?> getField(final T headerFieldDefinition) {
        return (HeaderField<T, ?>) this.fields.get(headerFieldDefinition);
    }

    /**
     * Checks if the header contains a field with the specified {@link HeaderFieldDefinition}
     * 
     * @param headerFieldDefinition
     *            the headerFieldDefinition that should be searched
     * @return
     */
    public boolean hasField(final HeaderFieldDefinition headerFieldDefinition) {
        return this.fields.containsKey(headerFieldDefinition);
    }

}
