/**
 * 
 */
package li.moskito.awtt.protocol.http;

import java.net.URI;

/**
 * @author Gerald
 */
public class Request extends Message {

    private final Commands command;
    private final URI resource;

    /**
     * @param command
     *            the Command for the request
     * @param resource
     *            the request on which the command should be performed
     * @param version
     *            the version of the HTTP Standard
     */
    public Request(final Commands command, final URI resource, final Version version) {
        super(version);
        this.command = command;
        this.resource = resource;

    }

    public Commands getCommand() {
        return this.command;
    }

    public URI getResource() {
        return this.resource;
    }

    @Override
    public String toString() {
        final StringBuffer buf = new StringBuffer(128);
        buf.append(this.command).append(' ');
        buf.append(this.resource).append(' ');
        buf.append(this.getVersion()).append('\n');
        for (final HeaderField<?> field : this.getFields()) {
            buf.append(' ').append(field.getFieldName()).append(": ").append(field.getValue()).append('\n');
        }
        return buf.toString();
    }
}
