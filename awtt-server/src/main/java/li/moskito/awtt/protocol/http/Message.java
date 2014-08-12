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
public abstract class Message {

    /**
     * Comparator for sorting header fields
     * 
     * @author Gerald
     */
    private final class HeaderFieldComparator implements Comparator<HeaderField<?>> {
        @Override
        public int compare(final HeaderField<?> o1, final HeaderField<?> o2) {
            return o1.getFieldName().getName().compareTo(o2.getFieldName().getName());
        }
    }

    private final Version version;
    private final Map<HeaderFieldDefinition, HeaderField<?>> fields;
    private Entity entity;

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

    public List<HeaderField<?>> getFields() {
        final List<HeaderField<?>> result = new ArrayList<>();
        result.addAll(this.fields.values());

        Collections.sort(result, new HeaderFieldComparator());

        return result;
    }

    public void addFields(final List<HeaderField<?>> fields) {
        for (final HeaderField<?> field : fields) {
            this.addField(field);
        }
    }

    public <T extends HeaderFieldDefinition> void addField(final HeaderField<T> field) {
        this.fields.put(field.getFieldName(), field);
    }

    public void addField(final HeaderFieldDefinition name, final Object value) {
        this.addField(new HeaderField<HeaderFieldDefinition>(name, value.toString()));
    }

    public Set<HeaderFieldDefinition> getFieldNames() {
        return this.fields.keySet();
    }

    public HeaderField<?> getField(final HeaderFieldDefinition fieldName) {
        return this.fields.get(fieldName);
    }

    /**
     * @param fieldName
     * @return
     */
    public boolean hasField(final HeaderFieldDefinition fieldName) {
        return this.fields.containsKey(fieldName);
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
