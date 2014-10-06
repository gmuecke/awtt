package li.moskito.awtt.util.statemachine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class CharacterInputTest {

    private CharacterInput subject;

    @Before
    public void setUp() throws Exception {
        this.subject = new CharacterInput("123");
    }

    @Test
    public void testRead() throws Exception {
        assertEquals(Character.valueOf('1'), this.subject.read());
        // repeated call returns the same value
        assertEquals(Character.valueOf('1'), this.subject.read());
    }

    @Test
    public void testMoveCursor_withinBoundaries() throws Exception {
        this.subject.moveCursor(1);
        assertEquals(Character.valueOf('2'), this.subject.read());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testMoveCursor_beyondLowerBoundary_IllegalArgumentException() throws Exception {
        this.subject.moveCursor(-1);
        this.subject.read();
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testMoveCursor_beyondUpperBoundary_andRead_IllegalArgumentException() throws Exception {
        this.subject.moveCursor(5);
        this.subject.read();
    }

    @Test
    public void testHasData_withinData_true() throws Exception {
        assertTrue(this.subject.hasData());
    }

    @Test
    public void testHasData_endOfData_false() throws Exception {
        this.subject.moveCursor(3);
        assertFalse(this.subject.hasData());
    }

}
