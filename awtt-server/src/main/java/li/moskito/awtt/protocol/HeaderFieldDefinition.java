/**
 * 
 */
package li.moskito.awtt.protocol;

/**
 * A header field definition defines the name of the header field of a procotol message.
 * 
 * @author Gerald
 */
public interface HeaderFieldDefinition {

    /**
     * @return the name of the header field according to the standard.
     */
    String getName();

    // TODO add information regarding the java type of the value of the corresponding header field
    // TODO add information for validating a value for a given header field
}
