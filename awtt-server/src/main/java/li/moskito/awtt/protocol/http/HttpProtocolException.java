/**
 * 
 */
package li.moskito.awtt.protocol.http;

/**
 * Exception that is thrown when the protocol was not applied correctly
 * 
 * @author Gerald
 */
public class HttpProtocolException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 6330199493104663309L;
    private final String      originalInput;

    /**
     * @param message
     * @param cause
     */
    public HttpProtocolException(final String message, final String originalInput, final Throwable cause) {
        super(message, cause);
        this.originalInput = originalInput;
    }

    /**
     * @param message
     * @param cause
     */
    public HttpProtocolException(final String message, final Throwable cause) {
        super(message, cause);
        this.originalInput = null;
    }

    /**
     * @param message
     */
    public HttpProtocolException(final String message) {
        super(message);
        this.originalInput = null;
    }

    public String getOriginalInput() {
        return this.originalInput;
    }

    /**
     * @param cause
     */
    public HttpProtocolException(final Throwable cause) {
        super(cause);
        this.originalInput = null;
    }

}
