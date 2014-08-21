/**
 * 
 */
package li.moskito.awtt.protocol;


/**
 * Event listener that can be attached to a {@link MessageChannel} to be notified upon occurrence of specific events.
 * 
 * @author Gerald
 */
public interface ChannelEventListener {

    /**
     * Is invoked if an event was fired to which the listener has a subscription
     * 
     * @param event
     */
    void onEvent(Event<?> event);
}
