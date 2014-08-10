/**
 * 
 */
package li.moskito.awtt.common;

/**
 * A implementation of the ContentType interface for creating custom content Types
 * 
 * @author Gerald
 */
public class CustomContentType implements ContentType {

    private final String mimeType;

    /**
     * Constructor for non-binary content types
     * 
     * @param mimeType
     */
    public CustomContentType(final String mimeType) {
        this.mimeType = mimeType;
    }

    @Override
    public String getMIMEType() {
        return this.mimeType;
    }

    @Override
    public String toString() {
        return this.mimeType;
    }

}
