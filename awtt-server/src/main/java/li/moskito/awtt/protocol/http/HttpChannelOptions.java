/**
 * 
 */
package li.moskito.awtt.protocol.http;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import li.moskito.awtt.protocol.MessageChannelOption;
import li.moskito.awtt.protocol.StandardConverters;

/**
 * Standard options for the HttpChannel
 * 
 * @author Gerald
 */
public final class HttpChannelOptions<T> implements MessageChannelOption<T> {

    /**
     * Timeout in seconds after which the connection to the client will be closed
     */
    public static final HttpChannelOptions<Integer> KEEP_ALIVE_TIMEOUT = new HttpChannelOptions<>("keepAliveTimeout",
            Integer.class, 5, StandardConverters.INTEGER_CONVERTER);

    /**
     * Number of messages received by a server after which the connection to the client will be closed.
     */
    public static final HttpChannelOptions<Integer> KEEP_ALIVE_MAX_MESSAGES = new HttpChannelOptions<>(
            "maxMessagesPerConnection", Integer.class, 100, StandardConverters.INTEGER_CONVERTER);

    /**
     * Set of the options supported by HTTP
     */
    @SuppressWarnings("rawtypes")
    static final Set<MessageChannelOption> SUPPORTED_OPTIONS;

    static {
        @SuppressWarnings("rawtypes")
        final Set<MessageChannelOption> set = new HashSet<>();
        set.add(KEEP_ALIVE_TIMEOUT);
        set.add(KEEP_ALIVE_MAX_MESSAGES);
        SUPPORTED_OPTIONS = Collections.unmodifiableSet(set);
    }

    private final String name;
    private final Class<T> type;

    private final T defaultValue;

    private final ValueConverter<T> converter;

    private HttpChannelOptions(final String name, final Class<T> type, final T defaultValue,
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
