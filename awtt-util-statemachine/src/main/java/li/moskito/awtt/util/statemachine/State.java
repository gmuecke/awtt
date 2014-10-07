/**
 * 
 */
package li.moskito.awtt.util.statemachine;

import java.util.HashMap;
import java.util.Map;

/**
 * A state is defined by the transitions that are possible from it
 * 
 * @author Gerald
 */
public class State {

    private final Map<Object, Transition> transitions;
    private final boolean finalState;

    public State(final boolean isFinal) {
        this.finalState = isFinal;
        this.transitions = new HashMap<>();

    }

    public State() {
        this(false);
    }

    public State addTransition(final Transition... transitions) {
        for (final Transition t : transitions) {
            this.transitions.put(t.getTrigger(), t);
        }
        return this;
    }

    public Transition getTransitionForTrigger(final Object trigger) {
        if (!this.transitions.containsKey(trigger) && this.transitions.containsKey(StandardTrigger.ANY)) {
            return this.transitions.get(StandardTrigger.ANY);
        }
        return this.transitions.get(trigger);
    }

    public boolean isFinal() {
        return this.finalState;
    }

}