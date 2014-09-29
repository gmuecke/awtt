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

    private final Map<Character, Transition> transitions;
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
            this.transitions.put(t.getTriggerChar(), t);
        }
        return this;
    }

    public Transition getTransitionForChar(final char c) {
        return this.transitions.get(c);
    }

    public boolean isFinal() {
        return this.finalState;
    }

}