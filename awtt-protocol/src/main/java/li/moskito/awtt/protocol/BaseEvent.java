package li.moskito.awtt.protocol;

/**
 * Basic implementation of an event
 * 
 * @author Gerald
 * @param <T>
 */
public class BaseEvent<T> implements Event<T> {
    private final Event.Type type;
    private final T eventData;

    /**
     * @param type
     */
    public BaseEvent(final Event.Type type, final T eventData) {
        super();
        this.type = type;
        this.eventData = eventData;
    }

    @Override
    public Event.Type getType() {
        return this.type;
    }

    @Override
    public T getEventData() {
        return this.eventData;
    }
}