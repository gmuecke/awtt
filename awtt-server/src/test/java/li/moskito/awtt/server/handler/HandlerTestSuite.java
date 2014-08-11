/**
 * 
 */
package li.moskito.awtt.server.handler;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * @author Gerald
 */
@RunWith(Suite.class)
@SuiteClasses({
        BlockingConnectionHandlerTest.class, HttpRequestHandlerTest.class, StaticFileContentRequestHandlerTest.class
})
public class HandlerTestSuite {

}
