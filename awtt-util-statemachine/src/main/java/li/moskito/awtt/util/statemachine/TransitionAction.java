/**
 * 
 */
package li.moskito.awtt.util.statemachine;

/**
 * Interface to be implemented by transition action that are executed when the transition is fired.
 * 
 * @author Gerald
 */
public interface TransitionAction {

    /**
     * Performs the action.
     * 
     * @param triggerInput
     *            the input of the current trigger
     */
    public void execute(Object triggerInput);
}
