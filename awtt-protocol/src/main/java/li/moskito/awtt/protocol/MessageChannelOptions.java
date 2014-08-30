/**
 * 
 */
package li.moskito.awtt.protocol;

/**
 * Standard options for MessageChannels
 * 
 * @author Gerald
 */
public final class MessageChannelOptions<T> implements MessageChannelOption<T> {

    /**
     * Timeout in seconds after which the connection to the client will be closed
     */
    public static final MessageChannelOptions<Integer> KEEP_ALIVE_TIMEOUT = new MessageChannelOptions<>(
            "keepAliveTimeout", Integer.class, 5, StandardConverters.INTEGER_CONVERTER);

    /**
     * Number of messages received by a server after which the connection to the client will be closed.
     */
    public static final MessageChannelOptions<Integer> KEEP_ALIVE_MAX_MESSAGES = new MessageChannelOptions<>(
            "maxMessagesPerConnection", Integer.class, 100, StandardConverters.INTEGER_CONVERTER);

    private final String name;
    private final Class<T> type;
    private final T defaultValue;
    private final ValueConverter<T> converter;

    private MessageChannelOptions(final String name, final Class<T> type, final T defaultValue,
            final ValueConverter<T> converter) {
        this.name = name;
        this.type = type;
        this.defaultValue = defaultValue;
        this.converter = converter;
    }

    @Override
    public Class<T> type() {
        return this.type;
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public T getDefault() {
        return this.defaultValue;
    }

    @Override
    public T fromString(final String valueAsString) {
        return this.converter.convert(valueAsString);
    }

}
