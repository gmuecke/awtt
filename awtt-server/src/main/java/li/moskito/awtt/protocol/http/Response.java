/**
 * 
 */
package li.moskito.awtt.protocol.http;

/**
 * @author Gerald
 */
public class Response extends Message<ResponseHeaderFieldDefinitions> {

    private final StatusCodes status;

    /**
     * @param version
     */
    public Response(final Version version, final StatusCodes status) {
        super(version);
        this.status = status;
    }

    /**
     * Creates a Response for HTTP/1.1
     * 
     * @param version
     */
    public Response(final StatusCodes status) {
        this(Version.HTTP_1_1, status);
    }

    public StatusCodes getStatus() {
        return this.status;
    }

}
