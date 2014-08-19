/**
 * 
 */
package li.moskito.awtt.server;

/**
 * Unchecked Exception that may be thrown by an AWTT Server during runtime
 * 
 * @author Gerald
 */
public class ServerRuntimeException extends RuntimeException {

    private static final long serialVersionUID = -4278615279853066006L;

    /**
     * @param paramThrowable
     */
    public ServerRuntimeException(final Throwable paramThrowable) {
        super(paramThrowable);
    }

    /**
     * @param message
     * @param cause
     */
    public ServerRuntimeException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
