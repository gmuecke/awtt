/**
 * 
 */
package li.moskito.awtt.protocol;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * A message that can be transfered between communication partners.
 * 
 * @author Gerald
 */
public abstract class Message {

    /**
     * The default charset for messages is UTF-8
     */
    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    private final Header header;
    private Body body;
    private Charset charset = DEFAULT_CHARSET;

    /**
     * Creates a message with the specified header.
     * 
     * @param header
     *            the header of the message
     */
    public Message(final Header header) {
        super();
        this.header = header;
    }

    /**
     * The header of the message
     * 
     * @return
     */
    public Header getHeader() {
        return this.header;
    }

    /**
     * The body of the message containing the payload or <code>null</code> if the message has no payload
     * 
     * @return
     */
    public Body getBody() {
        return this.body;
    }

    /**
     * Sets the message body
     * 
     * @param body
     */
    public void setBody(final Body body) {
        this.body = body;
    }

    /**
     * Returns the information if this message has a body or not
     * 
     * @return <code>true</code> if the message has a body
     */
    public boolean hasBody() {
        return this.body != null;
    }

    /**
     * The charset used to decode or encode binary data from this message
     * 
     * @return
     */
    public Charset getCharset() {
        return this.charset;
    }

    /**
     * Sets the charset used to decode or encode binary data from this message
     * 
     * @param charset
     */
    public void setCharset(final Charset charset) {
        this.charset = charset;
    }

}
