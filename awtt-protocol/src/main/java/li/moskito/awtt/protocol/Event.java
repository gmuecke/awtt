package li.moskito.awtt.protocol;

/**
 * An event that may occur during processing of messages in a message channel
 * 
 * @author Gerald
 * @param <T>
 *            the type of the payload of the event
 */
public interface Event<T> {
    /**
     * The type of the event. Event subscriptions should be associated with the type.
     * 
     * @return
     */
    Type getType();

    /**
     * The payload of the event. May be <code>null</code> if the event has no payload
     * 
     * @return
     */
    T getEventData();

    /**
     * Marker interface for event types. Should be implemented by enums
     * 
     * @author Gerald
     */
    public interface Type {

    }
}