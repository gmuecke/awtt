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
     * Reads the character on the current position
     * 
     * @return
     */
    T read();

    /**
     * Moves the curser on the data
     * 
     * @param delta
     *            number of positions to move. Negative values 'rewind' the data (move to left)
     */
    void moveCursor(int delta);
}
