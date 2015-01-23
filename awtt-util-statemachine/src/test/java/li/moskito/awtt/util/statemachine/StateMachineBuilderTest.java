package li.moskito.awtt.util.statemachine;

import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

public class StateMachineBuilderTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testBuild_simpleStateMachine() throws Exception {

        final StateMachineBuilder smb = StateMachineBuilder.newInstance();
        smb.addState("01").asInitialState().withTransition(1, "01", new TransitionAction() {

            @Override
            public void execute(final Object triggerInput) {
                System.out.print("." + triggerInput);

            }
        }).withTransition(StandardTrigger.EOF, "02", new TransitionAction() {

            @Override
            public void execute(final Object triggerInput) {
                System.out.print("!" + triggerInput);
            }
        });
        smb.addState("02");

        final StateMachine sm = smb.build();
        assertNotNull(sm);
        final CharacterInput in = new CharacterInput("123");
        sm.run(in);

    }
}