/**
 * 
 */
package li.moskito.awtt.util.statemachine;

/**
 * Input source for the simple parser
 * 
 * @author Gerald
 */
public interface Input {

    /**
     * Reads the character on the current position
     * 
     * @return
     */
    char read();

    /**
     * Moves the curser on the data
     * 
     * @param delta
     *            number of positions to move. Negative values 'rewind' the data (move to left)
     */
    void moveCursor(int delta);
}
