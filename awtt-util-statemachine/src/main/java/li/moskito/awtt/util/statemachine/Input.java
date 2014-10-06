/**
 * 
 */
package li.moskito.awtt.util.statemachine;

/**
 * Input source for the simple parser
 * 
 * @author Gerald
 */
public interface Input<T> {

    /**
     * @return <code>true</code> if the input contains data at the current cursor position or <code>false</code> if the
     *         cursor has reached a positon that is beyond the range of the input.
     */
    boolean hasData();

    /**
     * Reads the character on the current position
     * 
     * @return
     */
    T read();

    /**
     * Moves the curser on the data. The new position may be out of the range of the data. A following cursor movement
     * have to place cursor back into the range otherwise a IllegalArgumentException is thrown.
     * 
     * @param delta
     *            number of positions to move. Negative values 'rewind' the data (move to left)
     * @throws IllegalArgumentException
     *             if the cursor is at a position out of the range and the new position is still out of range
     */
    void moveCursor(int delta);
}
