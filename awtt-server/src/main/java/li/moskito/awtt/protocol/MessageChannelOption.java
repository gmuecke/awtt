/**
 * 
 */
package li.moskito.awtt.protocol;

/**
 * MessageChannelOptions can be used to configure the behavior of a MessageChannel.
 * 
 * @author Gerald
 */
public interface MessageChannelOption<T> {

    /**
     * Interface for a value converter for converting string values of message channel options to their type-safe values
     * 
     * @author Gerald
     * @param <T>
     */
    static interface ValueConverter<T> {

        T convert(String value);
    }

    /**
     * The name of the option
     * 
     * @return
     */
    String name();

    /**
     * The type of the option.
     * 
     * @return
     */
    Class<T> type();

    /**
     * The default value of the option if no value was specified
     * 
     * @return
     */
    T getDefault();

    /**
     * Converts the specified stringValue to the type of the option
     * 
     * @param valueAsString
     *            the value of the option as string
     * @return the value in the type of the option
     * @throws IllegalArgumentException
     *             if the string value could not be converted
     */
    T fromString(String valueAsString);
}
