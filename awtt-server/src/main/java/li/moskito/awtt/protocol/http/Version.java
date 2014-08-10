/**
 * 
 */
package li.moskito.awtt.protocol.http;

/**
 * Constant for the HTTP Version
 * 
 * @author Gerald
 */
public enum Version {

    HTTP_1_1("1.1"),
    HTTP_1_0("1.0"), ;

    final static String PROTOCOL_PREFIX = "HTTP/";

    private String      version;

    Version(final String version) {
        this.version = version;
    }

    public String getVersion() {
        return this.version;
    }

    @Override
    public String toString() {
        return PROTOCOL_PREFIX + this.version;
    }

    public static Version fromString(final String string) {
        for (final Version version : values()) {
            if (version.toString().equals(string)) {
                return version;
            }
        }
        return valueOf(string);
    }

}
