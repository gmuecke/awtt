/**
 * 
 */
package li.moskito.awtt.protocol.http;

import java.net.URI;

import li.moskito.awtt.protocol.HeaderField;

/**
 * @author Gerald
 */
public class HttpRequest extends HttpMessage {

    /**
     * @param command
     *            the Command for the request
     * @param resource
     *            the request on which the command should be performed
     * @param version
     *            the version of the HTTP Standard
     */
    public HttpRequest(final HttpCommands command, final URI resource, final HttpVersion version) {
        super(new HttpHeader(version, command, resource));

    }

    /**
     * Convenience method for getHeader().getCommand();
     * 
     * @return
     */
    public HttpCommands getCommand() {
        return this.getHeader().getCommand();
    }

    /**
     * Convenience method for getHeader().getResource();
     * 
     * @return
     */
    public URI getResource() {
        return this.getHeader().getResource();
    }

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder(128);
        buf.append(this.getCommand()).append(' ').append(this.getResource()).append(' ')
                .append(this.getHeader().getVersion()).append(HTTP.CRLF);

        for (final HeaderField field : this.getHeader().getFields()) {
            buf.append(field).append(HTTP.CRLF);
        }
        buf.append(HTTP.CRLF);
        return buf.toString();
    }
}
