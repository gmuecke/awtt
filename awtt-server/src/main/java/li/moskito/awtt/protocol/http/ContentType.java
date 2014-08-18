/**
 * 
 */
package li.moskito.awtt.protocol.http;

/**
 * A content type according to the definition of it in HTTP. The ContentType is associated with a MIME type.
 * 
 * @author Gerald
 */
public class ContentType {

    /**
     * ContentType that should be used when the MIME type can not be determined.
     */
    public final static ContentType APPLICATION_OCTETSTREAM = new ContentType("application/octet-stream");

    private final String mimeType;

    /**
     * Constructor for non-binary content types
     * 
     * @param mimeType
     */
    public ContentType(final String mimeType) {
        this.mimeType = mimeType;
    }

    /**
     * Returns the MIME Type for the content type in the format type/subtype
     * 
     * @return
     */
    public String getMIMEType() {
        return this.mimeType;
    }

    @Override
    public String toString() {
        return this.mimeType;
    }

}
