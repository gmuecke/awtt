/**
 * 
 */
package li.moskito.awtt.util.template;

import java.util.Map;

/**
 * Interface for a template
 * 
 * @author Gerald
 */
public interface Template<T> {

    /**
     * Compiles the template so that parameters can be applied
     */
    void compile();

    /**
     * Applies the parameters to the template and creates an output of the specified type
     * 
     * @param params
     *            the parameters to be applied to the template
     */
    T apply(Map<String, Object> params);
}
