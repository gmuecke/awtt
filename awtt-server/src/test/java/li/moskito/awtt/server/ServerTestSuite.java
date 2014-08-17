/**
 * 
 */
package li.moskito.awtt.server;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * @author Gerald
 *
 */
@RunWith(Suite.class)
@SuiteClasses({
        BlockingConnectionHandlerTest.class, ConnectionHandlerParametersTest.class, MessageWorkerTest.class,
        MultiportServerTest.class, PortTest.class, ServerBuilderTest.class
})
public class ServerTestSuite {

}
