/**
 * 
 */
package li.moskito.awtt.server;

/**
 * Exception that is thrown when the AWTT server can not be started
 * 
 * @author Gerald
 */
public class StartupException extends Exception {

    private static final long serialVersionUID = 1870057938181777816L;

    /**
     * @param cause
     */
    public StartupException(final Exception cause) {
        super(cause);
    }

}
