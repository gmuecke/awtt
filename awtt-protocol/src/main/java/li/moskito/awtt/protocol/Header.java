/**
 * 
 */
package li.moskito.awtt.protocol;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The header of a {@link Protocol} {@link Message}. The header carries the {@link ProtocolVersion} of the protocol and
 * a list of {@link HeaderField}s.
 * 
 * @author Gerald
 */
public abstract class Header {

    private final ProtocolVersion version;

    private final Map<HeaderFieldDefinition, HeaderField> fields;

    /**
     * Comparator for sorting header fields
     * 
     * @author Gerald
     */
    private static final class HeaderFieldComparator implements Comparator<HeaderField>, Serializable {

        private static final long serialVersionUID = 2432933502724826836L;

        @Override
        public int compare(final HeaderField paramT1, final HeaderField paramT2) {
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
    public List<HeaderField> getFields() {
        final List<HeaderField> result = new ArrayList<>();
        result.addAll(this.fields.values());
        Collections.sort(result, COMPARATOR);
        return result;
    }

    /**
     * Adds a list of fields to this header. Existing fields will be overriden
     * 
     * @param fields
     */
    public void addFields(final List<HeaderField> fields) {
        for (final HeaderField field : fields) {
            this.addField(field);
        }
    }

    public void addField(final HeaderField field) {
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
    public HeaderField getField(final HeaderFieldDefinition headerFieldDefinition) {
        return this.fields.get(headerFieldDefinition);
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
