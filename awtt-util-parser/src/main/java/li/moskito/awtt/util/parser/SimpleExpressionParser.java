/**
 * 
 */
package li.moskito.awtt.util.parser;

import static li.moskito.awtt.util.statemachine.StandardTrigger.EOF;
import li.moskito.awtt.util.statemachine.CharacterInput;
import li.moskito.awtt.util.statemachine.StateMachine;
import li.moskito.awtt.util.statemachine.StateMachineBuilder;
import li.moskito.awtt.util.statemachine.TransitionAction;

/**
 * A simple parser that parses ${} expressions in a text
 * 
 * @author Gerald
 */
public class SimpleExpressionParser {

    private final StateMachine<Character> statemachine;

    /**
     * @param initialState
     */
    @SuppressWarnings("unchecked")
    public SimpleExpressionParser() {
        final StateMachineBuilder smb = StateMachineBuilder.newInstance();

        //@formatter:off
        smb.addState("0").asInitialState().
            withTransition("0", new TransitionAction() {
                @Override
                public void execute(final Object triggerInput) {
                    SimpleExpressionParser.this.onText(triggerInput);
                    
                }
            }).
            withTransition(EOF, "final", new TransitionAction() {
                @Override
                public void execute(final Object triggerInput) {
                    SimpleExpressionParser.this.onTextEnd();
                    
                }
            }).
            withTransition('$', "exprInit");
        
        smb.addState("exprInit").
            withTransition('{', "expr", new TransitionAction() {
                @Override
                public void execute(final Object triggerInput) {
                    SimpleExpressionParser.this.onExpressionBegin();
                    
                }
            }).
            withTransition("0", new TransitionAction() {
                @Override
                public void execute(final Object triggerInput) {
                    SimpleExpressionParser.this.onText(triggerInput);
                    
                }
            });
    
        smb.addState("expr").
            withTransition('}', "0", new TransitionAction() {
                @Override
                public void execute(final Object triggerInput) {
                    SimpleExpressionParser.this.onExpressionEnd();
                    
                }
            }).
            withTransition("expr", new TransitionAction() {
                @Override
                public void execute(final Object triggerInput) {
                    SimpleExpressionParser.this.onExpressionText(triggerInput);
                    
                }
            });        
        smb.addState("final");
        // @formatter:on
        this.statemachine = smb.build();

    }

    public void parse(final String input) {
        final CharacterInput cin = new CharacterInput(input);
        this.statemachine.run(cin);
    }

    protected void onText(final Object c) {
        System.out.println("TXT" + c);
    }

    protected void onExpressionBegin() {
        System.out.println("EXP>");
    }

    protected void onExpressionText(final Object c) {
        System.out.println("XXX" + c);
    }

    protected void onExpressionEnd() {
        System.out.println("EXP<");
    }

    protected void onTextEnd() {
        System.out.println("EOF");
    }
}
