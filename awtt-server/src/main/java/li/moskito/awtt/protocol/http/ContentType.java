/**
 * 
 */
package li.moskito.awtt.protocol.http;

/**
 * A implementation of the ContentType interface for creating custom content Types
 * 
 * @author Gerald
 */
public class ContentType {

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
