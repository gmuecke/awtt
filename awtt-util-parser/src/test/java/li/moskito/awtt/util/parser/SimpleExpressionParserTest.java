package li.moskito.awtt.util.parser;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;

public class SimpleExpressionParserTest {

    private SimpleExpressionParser subject;

    @Before
    public void setUp() throws Exception {
        this.subject = new SimpleExpressionParser();
    }

    @Test
    public void testParse_expressionLessString() throws Exception {
        final String input = "123";
        this.subject.parse(input);

        verify(this.subject).onText('1');
        verify(this.subject).onText('2');
        verify(this.subject).onText('3');
        verify(this.subject).onTextEnd();
    }

    @Test
    public void testParse_validExpressionString() throws Exception {
        final String input = "1${abc}";
        final SimpleExpressionParser spy = spy(this.subject);
        spy.parse(input);

        verify(spy).onText(Character.valueOf('1'));
        verify(spy).onExpressionBegin();
        verify(spy).onExpressionText(Character.valueOf('a'));
        verify(spy).onExpressionText(Character.valueOf('b'));
        verify(spy).onExpressionText(Character.valueOf('c'));
        verify(spy).onExpressionEnd();
        verify(spy).onTextEnd();
    }

    @Test
    public void testParse_invalidExpressionString() throws Exception {
        final String input = "1$abc";
        this.subject.parse(input);

        verify(this.subject).onText('1');
        verify(this.subject, times(0)).onText('$');
        verify(this.subject).onText('a');
        verify(this.subject).onText('b');
        verify(this.subject).onText('c');
        verify(this.subject).onTextEnd();
    }

}
