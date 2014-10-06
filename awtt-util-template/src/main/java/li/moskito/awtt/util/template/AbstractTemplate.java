/**
 * 
 */
package li.moskito.awtt.util.template;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Gerald
 * @param <T>
 */
public abstract class AbstractTemplate<T> implements Template<T> {

    private final List<TemplateFragment> fragments;

    /**
     * 
     */
    public AbstractTemplate() {
        this.fragments = new ArrayList<>();
    }

    @Override
    public void compile() {
        // TODO create fragments

    }
}
