/**
 * 
 */
package li.moskito.awtt.server;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * @author Gerald
 */
@RunWith(Suite.class)
@SuiteClasses({
        BlockingConnectionHandlerTest.class, MessageWorkerTest.class, MultiportServerTest.class, PortTest.class,
        ServerBuilderTest.class, ServerRuntimeExceptionTest.class, StartupTest.class
})
public class ServerTestSuite {

}
