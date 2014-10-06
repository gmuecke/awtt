/**
 * 
 */
package li.moskito.awtt.util.template;

import java.util.Map;

/**
 * @author Gerald
 */
public class StringTemplate implements Template<String> {

    private String template;

    /**
     * @param template
     */
    public StringTemplate(final String template) {
        this.template = template;
    }

    @Override
    public void compile() {
        for (int i = 0; i < this.template.length(); i++) {

        }
        this.template = null;

    }

    @Override
    public String apply(final Map<String, Object> params) {
        // TODO Auto-generated method stub
        return null;
    }

}
