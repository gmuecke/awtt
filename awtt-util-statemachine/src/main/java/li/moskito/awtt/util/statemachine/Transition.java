package li.moskito.awtt.util.statemachine;

/**
 * Defines a transition that is triggered by the detection of a specific character
 * 
 * @author Gerald
 */
public class Transition {

    /**
     * Empty Action
     */
    static final Runnable EMPTY_ACTION = new Runnable() {

        @Override
        public void run() {
        }
    };

    private final Object trigger;
    private final State nextState;
    private final int cursorMovement;
    private final Runnable transitionAction;

    /**
     * Creates a new transition
     * 
     * @param c
     *            the char that triggers the transition
     * @param cursorMovement
     *            the number of positions the curser should be moved. Negative values move the cursor to previous
     *            positions
     * @param next
     *            the next state after the transition
     * @param transitionAction
     *            action to be performed when transitioning
     */
    public Transition(final Object trigger, final int cursorMovement, final State next, final Runnable transitionAction) {
        this.trigger = trigger;
        this.nextState = next;
        this.cursorMovement = cursorMovement;
        this.transitionAction = transitionAction;
    }

    /**
     * Creates a new transition
     * 
     * @param trigger
     *            the char that triggers the transition
     * @param cursorMovement
     *            the number of positions the curser should be moved. Negative values move the cursor to previous
     *            positions
     * @param next
     *            the next state after the transition
     */
    public Transition(final Object trigger, final int cursorMovement, final State next) {
        this(trigger, cursorMovement, next, EMPTY_ACTION);
    }

    /**
     * Creates a new transition that moves the cursor 1 position ahead and performs no action
     * 
     * @param c
     *            the char that triggers the transition
     * @param next
     *            the next state after the transition
     * @param transitionAction
     *            action to be performed when transitioning
     */
    public Transition(final Object c, final State next, final Runnable transitionAction) {
        this(c, 1, next, transitionAction);
    }

    /**
     * Creates a new transition that moves the cursor 1 position ahead and performs no action
     * 
     * @param c
     *            the char that triggers the transition
     * @param next
     *            the next state after the transition
     */
    public Transition(final Object c, final State next) {
        this(c, next, EMPTY_ACTION);
    }

    /**
     * Creates a new transition that fires on any character and moves the curser 1 position ahead and performs no action
     * 
     * @param next
     *            the next state after the transition
     * @param transitionAction
     *            action to be performed when transitioning
     */
    public Transition(final State next, final Runnable transitionAction) {
        this(StandardTrigger.ANY, 1, next, transitionAction);
    }

    /**
     * Creates a new transition that fires on any character and moves the curser 1 position ahead and performs no action
     * 
     * @param next
     *            the next state after the transition
     */
    public Transition(final State next) {
        this(next, EMPTY_ACTION);
    }

    /**
     * @return the trigger char
     */
    public Object getTrigger() {
        return this.trigger;
    }

    /**
     * @return the next state
     */
    public State getNextState() {
        return this.nextState;
    }

    /**
     * The number of index positions the cursor should be moved
     * 
     * @return
     */
    public int getCursorMovement() {
        return this.cursorMovement;
    }

    /**
     * Performs the transition action
     * 
     * @return
     * @throws Exception
     */
    public void fire() {
        this.transitionAction.run();
    }
}