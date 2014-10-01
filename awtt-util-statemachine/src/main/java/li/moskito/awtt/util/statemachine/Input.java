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
     * @return <code>true</code> if the input contains more data to be reached or <code>false</code> if the input has
     *         reached it's end. If this method returns <code>false</code>, the read() mehtod may throw an exception
     */
    boolean hasMore();

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
