/**
 * 
 */
package li.moskito.awtt.util.statemachine;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * A simple state machine that is based on a Turing Machine algorithm. The machine works on an input that allows reading
 * of a single character and the movement of the cursor. On every transition the machine may perform an action in
 * addition to the movemenent of the cursor.
 * 
 * @author Gerald
 */
public class StateMachine<T> {

    private final Set<State> states;
    private State currentState;

    /**
     * Creates a new state machine with an initial state. The initial state is mandatory as it is also the first current
     * state.
     * 
     * @param initialState
     */
    public StateMachine(final State initialState) {
        this.states = new HashSet<>();
        this.states.add(initialState);
        this.currentState = initialState;
    }

    /**
     * Adds one or more states to the statemachine.
     * 
     * @param state
     * @return
     */
    public StateMachine addState(final State... state) {
        this.states.addAll(Arrays.asList(state));
        return this;
    }

    /**
     * @return the current state of the machine
     */
    public State getCurrentState() {
        return this.currentState;
    }

    /**
     * Runs the statemachine until a final state has been reached or endlessly if the state machine does not terminate
     * 
     * @param input
     *            the input for the statemachine
     */
    public void run(final Input<T> input) {
        while (!this.currentState.isFinal()) {
            this.step(input);
        }
    }

    /**
     * Performs single transition based on the inputs current character.
     * 
     * @param input
     *            the input for the state machine
     */
    public void step(final Input<T> input) {
        final Object trigger = this.getTrigger(input);
        final Transition t = this.currentState.getTransitionForTrigger(trigger);
        t.fire();
        input.moveCursor(t.getCursorMovement());
        this.currentState = t.getNextState();
    }

    /**
     * Retrieves the trigger for the current input. If the input has reached its end, an EOF trigger is returned
     * otherwise the data on the inputs current cursor position
     * 
     * @param input
     *            the input that provides the trigger
     * @return the Trigger from the input
     */
    private Object getTrigger(final Input<T> input) {
        final Object trigger;
        if (input.hasData()) {
            trigger = input.read();
        } else {
            trigger = Transition.EOF_TRIGGER;
        }
        return trigger;
    }

}
