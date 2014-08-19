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
public class CustomHeaderFieldDefinition implements HeaderFieldDefinition {

    private final String name;

    /**
     * Map to keep instances of the custom header field to prevent multiple instantiation of immutable instances. A
     * WeakHashMap is used to allow the disposable of unneeded instances (otherwise the server might be flooded with
     * custom headers until memory is full)
     */
    private static final Map<String, CustomHeaderFieldDefinition> KNOWN_HEADER_FIELD = new WeakHashMap<>();

    /**
     * Creates a new custom header field. Because header fields are immutable in their nature as they only define a name
     * for the field, this method creates a new instance only if the header field has not been created before or wasn't
     * in use any more.
     * 
     * @param name
     *            the name of the custom header field to be created.
     * @return an instance of a custom header field for the specified name
     */
    public static CustomHeaderFieldDefinition forName(final String name) {
        if (!KNOWN_HEADER_FIELD.containsKey(name)) {
            final CustomHeaderFieldDefinition field = new CustomHeaderFieldDefinition(name);
            KNOWN_HEADER_FIELD.put(name, field);

        }
        return KNOWN_HEADER_FIELD.get(name);
    }

    /**
     * @param name
     *            name of the hader field
     */
    private CustomHeaderFieldDefinition(final String name) {
        super();
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }

}
