/**
 * 
 */
package li.moskito.awtt.protocol.http;

import li.moskito.awtt.protocol.ProtocolVersion;

/**
 * Constant for the HTTP Version
 * 
 * @author Gerald
 */
public enum HttpVersion implements ProtocolVersion {

    HTTP_1_1("1.1"),
    HTTP_1_0("1.0"),
    HTTP_0_9("0.9"), ;

    final static String PROTOCOL_PREFIX = "HTTP/";

    private String version;

    HttpVersion(final String version) {
        this.version = version;
    }

    @Override
    public String getVersion() {
        return this.version;
    }

    @Override
    public String toString() {
        return PROTOCOL_PREFIX + this.version;
    }

    public static HttpVersion fromString(final String string) {
        for (final HttpVersion version : values()) {
            if (version.toString().equals(string)) {
                return version;
            }
        }
        return valueOf(string);
    }

}
