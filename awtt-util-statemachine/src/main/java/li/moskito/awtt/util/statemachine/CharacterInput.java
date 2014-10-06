/**
 * 
 */
package li.moskito.awtt.util.statemachine;

/**
 * @author Gerald
 */
public class CharacterInput implements Input<Character> {

    private final String data;
    private int index;

    public CharacterInput(final String data) {
        this.data = data;
        this.index = 0;
    }

    @Override
    public Character read() {
        if (!this.hasData()) {
            throw new IndexOutOfBoundsException("Invalid Cursor Position " + this.index);
        }
        return this.data.charAt(this.index);
    }

    @Override
    public void moveCursor(final int delta) {
        this.index += delta;

    }

    @Override
    public boolean hasData() {
        return this.index >= 0 && this.index < this.data.length();
    }

}
