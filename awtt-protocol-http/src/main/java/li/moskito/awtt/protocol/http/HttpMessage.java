/**
 * 
 */
package li.moskito.awtt.protocol.http;

import li.moskito.awtt.protocol.BinaryBody;
import li.moskito.awtt.protocol.HeaderFieldDefinition;
import li.moskito.awtt.protocol.Message;

/**
 * A message is a structured piece of information that is exchanged between client and server
 * 
 * @author Gerald
 */
public abstract class HttpMessage extends Message {

    /**
     * Creates a HttpMessage with the specified header
     * 
     * @param header
     */
    public HttpMessage(final HttpHeader header) {
        super(header);
    }

    /**
     * Convenience method to add a field to the header of the HttpMessage.
     * 
     * @param name
     *            the header field definition of the message
     * @param value
     *            the value to be set
     */
    public void addField(final HeaderFieldDefinition name, final Object value) {
        this.getHeader().addField(new HttpHeaderField(name, value.toString()));
    }

    @Override
    public HttpHeader getHeader() {
        return (HttpHeader) super.getHeader();
    }

    @Override
    public BinaryBody getBody() {
        return (BinaryBody) super.getBody();
    }
}
