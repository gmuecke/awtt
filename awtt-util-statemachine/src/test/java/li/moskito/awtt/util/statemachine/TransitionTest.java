package li.moskito.awtt.util.statemachine;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TransitionTest {

    private final int cursorMovement = 13;

    @Mock
    private State next;

    @Mock
    private Object trigger;

    @Mock
    private TransitionAction transitionAction;

    private Transition transition;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        this.transition = new Transition(this.trigger, this.cursorMovement, this.next, this.transitionAction);
    }

    @Test
    public void testTransitionObjectIntState() throws Exception {
        final Transition t = new Transition(this.trigger, this.cursorMovement, this.next);
        assertEquals(this.trigger, t.getTrigger());
        assertEquals(this.cursorMovement, t.getCursorMovement());
        assertEquals(this.next, t.getNextState());

    }

    @Test
    public void testTransitionObjectStateTransitionAction() throws Exception {
        final Transition t = new Transition(this.trigger, this.next, this.transitionAction);
        assertEquals(this.trigger, t.getTrigger());
        assertEquals(1, t.getCursorMovement());
        assertEquals(this.next, t.getNextState());

        t.fire("123");
        verify(this.transitionAction).execute("123");
    }

    @Test
    public void testTransitionObjectState() throws Exception {
        final Transition t = new Transition(this.trigger, this.next);
        assertEquals(this.trigger, t.getTrigger());
        assertEquals(1, t.getCursorMovement());
        assertEquals(this.next, t.getNextState());
    }

    @Test
    public void testTransitionStateTransitionAction() throws Exception {
        final Transition t = new Transition(this.next, this.transitionAction);
        assertEquals(StandardTrigger.ANY, t.getTrigger());
        assertEquals(1, t.getCursorMovement());
        assertEquals(this.next, t.getNextState());

        t.fire("123");
        verify(this.transitionAction).execute("123");
    }

    @Test
    public void testTransitionState() throws Exception {
        final Transition t = new Transition(this.next);
        assertEquals(StandardTrigger.ANY, t.getTrigger());
        assertEquals(1, t.getCursorMovement());
        assertEquals(this.next, t.getNextState());
    }

    @Test
    public void testGetTrigger() throws Exception {
        assertEquals(this.trigger, this.transition.getTrigger());
    }

    @Test
    public void testGetNextState() throws Exception {
        assertEquals(this.next, this.transition.getNextState());
    }

    @Test
    public void testGetCursorMovement() throws Exception {
        assertEquals(this.cursorMovement, this.transition.getCursorMovement());
    }

    @Test
    public void testFire() throws Exception {
        this.transition.fire("123");
        verify(this.transitionAction).execute("123");
    }

}
