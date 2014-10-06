/**
 * 
 */
package li.moskito.awtt.util.template;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple Template Engine that allows simple replace operations with single and multivalue parameters
 * 
 * @author Gerald
 */
public class SimpleTemplateEngine {

    private final Map<String, Object> env;

    /**
     * 
     */
    public SimpleTemplateEngine(final Template template) {
        this.env = new HashMap<>();
    }

    public static SimpleTemplateEngine forTemplate(final String template) {
        return new SimpleTemplateEngine(new StringTemplate(template));
    }

    public SimpleTemplateEngine withParam(final String name, final Object value) {
        this.env.put(name, value);
        return this;
    }

    public String apply() {

        return null;
    }

    public SimpleTemplateEngine reset() {
        this.env.clear();
        return this;
    }

}
