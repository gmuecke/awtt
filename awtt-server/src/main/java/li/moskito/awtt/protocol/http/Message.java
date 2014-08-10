/**
 * 
 */
package li.moskito.awtt.protocol.http;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A message is a structured piece of information that is exchanged between client and server
 * 
 * @author Gerald
 */
public abstract class Message<T extends HeaderFieldDefinition> {

    /**
     * Comparator for sorting header fields
     * 
     * @author Gerald
     */
    private final class HeaderFieldComparator implements Comparator<HeaderField<T>> {
        @Override
        public int compare(final HeaderField<T> o1, final HeaderField<T> o2) {
            return o1.getFieldName().getName().compareTo(o2.getFieldName().getName());
        }
    }

    private final Version                version;
    private final Map<T, HeaderField<T>> fields;
    private Entity                       entity;

    /**
     * @param version
     */
    public Message(final Version version) {
        super();
        this.version = version;
        this.fields = new HashMap<>();
    }

    public Version getVersion() {
        return this.version;
    }

    public List<HeaderField<T>> getFields() {
        final List<HeaderField<T>> result = new ArrayList<>();
        result.addAll(this.fields.values());

        Collections.sort(result, new HeaderFieldComparator());

        return result;
    }

    public void addFields(final List<HeaderField<T>> fields) {
        for (final HeaderField<T> field : fields) {
            this.addField(field);
        }
    }

    public void addField(final HeaderField<T> field) {
        this.fields.put(field.getFieldName(), field);
    }

    public void addField(final T name, final Object value) {
        this.addField(new HeaderField<T>(name, value.toString()));
    }

    public Set<T> getFieldNames() {
        return this.fields.keySet();
    }

    public HeaderField<T> getField(final T fieldName) {
        return this.fields.get(fieldName);
    }

    public boolean hasEntity() {
        return this.entity != null;
    }

    /**
     * @param entity
     */
    public void setEntity(final Entity entity) {
        this.entity = entity;
    }

    public Entity getEntity() {
        return this.entity;
    }
}
