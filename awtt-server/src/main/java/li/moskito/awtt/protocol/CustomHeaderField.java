/**
 * 
 */
package li.moskito.awtt.protocol;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Declares a header that is not part of a protocol specification. This field type can be used for protocol extensions.
 * 
 * @author Gerald
 */
public class CustomHeaderField implements HeaderFieldDefinition {

    private final String name;

    /**
     * Map to keep instances of the custom header field to prevent multiple instantiation of immutable instances. A
     * WeakHashMap is used to allow the disposable of unneeded instances (otherwise the server might be flooded with
     * custom headers until memory is full)
     */
    private static final Map<String, CustomHeaderField> KNOWN_HEADER_FIELD = new WeakHashMap<>();

    public static CustomHeaderField forName(final String name) {
        if (!KNOWN_HEADER_FIELD.containsKey(name)) {
            final CustomHeaderField field = new CustomHeaderField(name);
            KNOWN_HEADER_FIELD.put(name, field);

        }
        return KNOWN_HEADER_FIELD.get(name);
    }

    /**
     * @param name
     */
    private CustomHeaderField(final String name) {
        super();
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }

}
