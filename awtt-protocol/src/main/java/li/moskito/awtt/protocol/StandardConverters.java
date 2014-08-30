/**
 * 
 */
package li.moskito.awtt.protocol;

import li.moskito.awtt.protocol.MessageChannelOption.ValueConverter;

/**
 * Standard convertes for {@link MessageChannelOption}s
 * 
 * @author Gerald
 */
public final class StandardConverters {

    /**
     * Returns the input string as output
     */
    public static final ValueConverter<String> IDENTITY_CONVERTER = new ValueConverter<String>() {
        @Override
        public String convert(final String value) {
            return value;
        }
    };
    /**
     * Converts the string to integer
     */
    public static final ValueConverter<Integer> INTEGER_CONVERTER = new ValueConverter<Integer>() {
        @Override
        public Integer convert(final String value) {
            return Integer.valueOf(value);
        }
    };

    /**
     * Converts the string to boolan
     */
    public static final ValueConverter<Boolean> BOOLEAN_CONVERTER = new ValueConverter<Boolean>() {
        @Override
        public Boolean convert(final String value) {
            return Boolean.valueOf(value);
        }
    };

    private StandardConverters() {

    }
}
