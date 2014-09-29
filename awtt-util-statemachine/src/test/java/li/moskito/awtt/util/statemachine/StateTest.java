package li.moskito.awtt.util.statemachine;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class StateTest {

    @Mock
    private Transition transition;
    private State subject;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        this.subject = new State();

    }

    @Test
    public void testStateBoolean() throws Exception {
        assertTrue(new State(true).isFinal());
        assertFalse(new State(false).isFinal());
    }

    @Test
    public void testGetTransitionForChar_knownChar() throws Exception {

        when(this.transition.getTrigger()).thenReturn('c');
        assertSame(this.subject, this.subject.addTransition(this.transition));
        assertSame(this.transition, this.subject.getTransitionForTrigger('c'));
    }

    @Test
    public void testGetTransitionForChar_unknownChar_null() throws Exception {

        when(this.transition.getTrigger()).thenReturn('c');
        assertSame(this.subject, this.subject.addTransition(this.transition));
        assertNull(this.subject.getTransitionForTrigger('b'));
    }
}
