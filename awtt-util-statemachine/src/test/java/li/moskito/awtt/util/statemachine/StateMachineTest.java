package li.moskito.awtt.util.statemachine;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class StateMachineTest {

    @Mock
    private Input input;

    @Mock
    private Transition transition;

    @Mock
    private State initialState;

    @InjectMocks
    private StateMachine statemachine;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetCurrentState() throws Exception {
        assertEquals(this.initialState, this.statemachine.getCurrentState());
    }

    @Test
    public void testRun_twoStateMachine() throws Exception {

        final State finalState = mock(State.class);
        when(finalState.isFinal()).thenReturn(true);

        when(this.input.read()).thenReturn('c');
        when(this.initialState.getTransitionForTrigger('c')).thenReturn(this.transition);
        when(this.transition.getNextState()).thenReturn(finalState);
        when(this.transition.getCursorMovement()).thenReturn(3);

        this.statemachine.addState(this.initialState);
        this.statemachine.step(this.input);

        assertEquals(finalState, this.statemachine.getCurrentState());

        verify(this.transition).fire();
        final ArgumentCaptor<Integer> captor = ArgumentCaptor.forClass(Integer.class);
        verify(this.input).moveCursor(captor.capture());
        assertEquals(Integer.valueOf(3), captor.getValue());

    }

    @Test
    public void testStep() throws Exception {
        final State finalState = mock(State.class);
        when(finalState.isFinal()).thenReturn(true);
        when(this.input.read()).thenReturn('c');
        when(this.initialState.getTransitionForTrigger('c')).thenReturn(this.transition);
        when(this.transition.getNextState()).thenReturn(finalState);
        when(this.transition.getCursorMovement()).thenReturn(3);

        this.statemachine.addState(this.initialState);
        this.statemachine.addState(finalState);
        this.statemachine.step(this.input);

        assertEquals(finalState, this.statemachine.getCurrentState());

        verify(this.transition).fire();
        final ArgumentCaptor<Integer> captor = ArgumentCaptor.forClass(Integer.class);
        verify(this.input).moveCursor(captor.capture());
        assertEquals(Integer.valueOf(3), captor.getValue());
    }

}
