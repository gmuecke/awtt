/**
 * 
 */
package li.moskito.awtt.protocol;

/**
 * @author Gerald
 */
public class ProtocolException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 5150389782182922712L;

    /**
     * @param message
     * @param cause
     */
    public ProtocolException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * @param message
     */
    public ProtocolException(final String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public ProtocolException(final Throwable cause) {
        super(cause);
    }

}
