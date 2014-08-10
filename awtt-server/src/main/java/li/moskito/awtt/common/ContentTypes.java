/**
 * 
 */
package li.moskito.awtt.common;

/**
 * Content Types
 * 
 * @author Gerald
 */
public enum ContentTypes implements ContentType {

    TEXT_PLAIN("text/plain", "txt"),
    TEXT_HTML("text/html", "htm", "html", "stm"),
    TEXT_CSS("text/css", "css"),
    IMAGE_BMP("image/bmp", "bmp"),
    IMAGE_GIF("image/gif", "gif"),
    IMAGE_PNG("image/png", "png"),
    IMAGE_JPG("image/jpeg", "jpg", "jpeg", "jpe"),
    IMAGE_TIFF("image/tiff", "tif", "tiff"),

    APPLICATION_PDF("application/pdf", "pdf"),
    APPLICATION_OCTETSTREAM("application/octet-stream"), ;

    private String contentType;
    private String[] fileExtension;

    private ContentTypes(final String contentType, final String... fileExtension) {
        this.contentType = contentType;
        this.fileExtension = fileExtension;
    }

    @Override
    public String getMIMEType() {
        return this.contentType;
    }

    @Override
    public String toString() {
        return this.contentType;
    }

    /**
     * Retrieves the ContentType for a given filename. The content type is determined by the file extension of the file.
     * If the ContentType cannot be determined, the ContentType application/octet-stream is returned
     * 
     * @param filename
     * @return
     */
    public static ContentTypes byFileName(final String filename) {
        final int extensionSeparator = filename.lastIndexOf('.');

        if (extensionSeparator != -1) {
            final String extension = filename.substring(extensionSeparator + 1);
            return byFileExtension(extension);
        }

        return APPLICATION_OCTETSTREAM;
    }

    /**
     * Retrieves the content type by a file extension. If the ContentType cannot be determined, the ContentType
     * application/octet-stream is returned
     * 
     * @param extension
     * @return
     */
    public static ContentTypes byFileExtension(final String extension) {
        for (final ContentTypes type : values()) {
            if (matches(extension, type)) {
                return type;
            }
        }
        return APPLICATION_OCTETSTREAM;
    }

    /**
     * Tests if the extension matches the given content type
     * 
     * @param extension
     * @param type
     * @return
     */
    private static boolean matches(final String extension, final ContentTypes type) {
        for (final String fileExtension : type.fileExtension) {
            if (extension.equals(fileExtension)) {
                return true;
            }
        }
        return false;
    }
}
