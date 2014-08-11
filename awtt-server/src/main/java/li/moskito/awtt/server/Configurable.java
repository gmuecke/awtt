/**
 * 
 */
package li.moskito.awtt.server;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;

/**
 * @author Gerald
 */
public interface Configurable {

    /**
     * Sets up the server
     * 
     * @param config
     * @throws ConfigurationException
     */
    void configure(HierarchicalConfiguration config) throws ConfigurationException;
}
