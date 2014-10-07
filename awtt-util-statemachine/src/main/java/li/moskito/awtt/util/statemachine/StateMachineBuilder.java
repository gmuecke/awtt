/**
 * 
 */
package li.moskito.awtt.util.statemachine;

import static li.moskito.awtt.util.statemachine.Transition.EMPTY_ACTION;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.emory.mathcs.backport.java.util.Collections;

/**
 * @author Gerald
 */
public final class StateMachineBuilder {

    private final Map<String, StateDeclaration> stateDeclarationMap;

    private StateMachineBuilder() {
        this.stateDeclarationMap = new HashMap<>();
    }

    public static StateMachineBuilder newInstance() {
        return new StateMachineBuilder();
    }

    @SuppressWarnings("rawtypes")
    public StateMachine build() {
        final HashMap<String, State> stateMap = new HashMap<>();
        String initialState = null;
        // at first create a state for every state name and determine initial and final states
        for (final Map.Entry<String, StateDeclaration> sdEntry : this.stateDeclarationMap.entrySet()) {

            final StateDeclaration sd = sdEntry.getValue();
            final State state = new State(sd.isFinalState());

            if (sd.isInitialState()) {
                if (initialState == null) {
                    initialState = sdEntry.getKey();
                } else {
                    throw new IllegalArgumentException("Multiple initial states found, only one is allowed");
                }
            }
            stateMap.put(sdEntry.getKey(), state);
        }
        // second, add transitions to each state and resolve the transitions next states to existing states
        for (final Map.Entry<String, StateDeclaration> sdEntry : this.stateDeclarationMap.entrySet()) {

            final Set<TransitionDeclaration> tds = sdEntry.getValue().getTransitions();
            final State state = stateMap.get(sdEntry.getKey());

            for (final TransitionDeclaration td : tds) {
                final State nextState = stateMap.get(td.getNextState());
                state.addTransition(new Transition(td.trigger, td.cursorMovement, nextState, td.transitionAction));
            }
        }
        return new StateMachine(stateMap.get(initialState));
    }

    public StateDeclaration addState(final String statename) {
        if (this.stateDeclarationMap.containsKey(statename)) {
            throw new IllegalArgumentException("State with name " + statename + " already defined");
        }
        final StateDeclaration sd = new StateDeclaration(statename);
        this.stateDeclarationMap.put(statename, sd);
        return sd;
    }

    public final class TransitionDeclaration {
        private final Object trigger;
        private final int cursorMovement;
        private final String nextState;
        private final Runnable transitionAction;

        /**
         * @param trigger
         *            the trigger for which the transition should be fired
         * @param cursorMovement
         *            the delta of how many positions the cursor should be moved.
         * @param nextState
         *            the name of the next state after the transition has been fired
         * @param transitionAction
         *            the action to be performed upon firing the transition
         */
        TransitionDeclaration(final Object trigger, final int cursorMovement, final String nextState,
                final Runnable transitionAction) {
            super();

            this.trigger = trigger;
            this.cursorMovement = cursorMovement;
            this.nextState = nextState;
            this.transitionAction = transitionAction;
        }

        public Object getTrigger() {
            return this.trigger;
        }

        public int getCursorMovement() {
            return this.cursorMovement;
        }

        public String getNextState() {
            return this.nextState;
        }

        public Runnable getTransitionAction() {
            return this.transitionAction;
        }

    }

    public final class StateDeclaration {

        private final Set<TransitionDeclaration> transitions;
        private boolean initialState;

        /**
         * @param statename
         */
        StateDeclaration(final String statename) {
            this.transitions = new HashSet<>();
            this.initialState = false;
        }

        public StateDeclaration withTransition(final Object trigger, final int cursorMovement, final String nextState,
                final Runnable transitionAction) {
            this.transitions.add(new TransitionDeclaration(trigger, cursorMovement, nextState, transitionAction));
            return this;
        }

        public StateDeclaration withTransition(final Object trigger, final int cursorMovement, final String nextState) {
            return this.withTransition(trigger, cursorMovement, nextState, EMPTY_ACTION);
        }

        public StateDeclaration withTransition(final Object trigger, final String nextState,
                final Runnable transitionAction) {
            return this.withTransition(trigger, 1, nextState, transitionAction);
        }

        public StateDeclaration withTransition(final Object trigger, final String nextState) {
            return this.withTransition(trigger, 1, nextState, EMPTY_ACTION);
        }

        public StateDeclaration withTransition(final int cursorMovement, final String nextState) {
            return this.withTransition(StandardTrigger.ANY, cursorMovement, nextState, EMPTY_ACTION);
        }

        public StateDeclaration withTransition(final int cursorMovement, final String nextState,
                final Runnable transitionAction) {
            return this.withTransition(StandardTrigger.ANY, cursorMovement, nextState, transitionAction);
        }

        public StateDeclaration withTransition(final String nextState) {
            return this.withTransition(StandardTrigger.ANY, 1, nextState, EMPTY_ACTION);
        }

        public StateDeclaration asInitialState() {
            this.initialState = true;
            return this;
        }

        @SuppressWarnings("unchecked")
        Set<TransitionDeclaration> getTransitions() {
            return Collections.unmodifiableSet(this.transitions);
        }

        boolean isFinalState() {
            return this.transitions.isEmpty();
        }

        boolean isInitialState() {
            return this.initialState;
        }
    }
}
