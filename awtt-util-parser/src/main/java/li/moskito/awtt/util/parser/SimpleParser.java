/**
 * 
 */
package li.moskito.awtt.util.parser;

import java.util.HashMap;
import java.util.Map;

import li.moskito.awtt.util.statemachine.State;
import li.moskito.awtt.util.statemachine.StateMachine;

/**
 * @author Gerald
 */
public class SimpleParser extends StateMachine<Character> {

    /**
     * @param initialState
     */
    private SimpleParser(final State initialState) {
        super(initialState);
    }

    /**
     * Syntax:
     * 
     * <pre>
     *      {null, 
     *          "INITIAL_STATE_ID" 
     *      },
     *      {"STATE_ID", 
     *          {'c', 1, "STATE_ID", Runnable()},
     *          {'c', 1, "STATE_ID", Runnable()},
     *      },
     *      {"FINAL_STATE_ID", 
     *          null
     *      }
     * 
     * </pre>
     * 
     * @return
     */
    public static SimpleParser build(final Object[][] statemachineDefinition) {
        final Map<String, State> stateMap = new HashMap<>();
        String initialStateName = null;
        for (final Object[] stateDef : statemachineDefinition) {
            final boolean isFinal = false;
            if (stateDef.length == 2 && stateDef[0] == null) {
                // get initialStateName
                initialStateName = (String) stateDef[1];
                continue;
            }

            if (stateDef.length == 2 && stateDef[1] == null) {
                stateMap.put((String) stateDef[0], new State(true));
            } else {
                final String stateName = (String) stateDef[0];
                final Object[][] transitions = (Object[][]) stateDef[1];

            }
        }
        return new SimpleParser();
    }
}
