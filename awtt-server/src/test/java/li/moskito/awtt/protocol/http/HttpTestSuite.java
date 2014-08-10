/**
 * 
 */
package li.moskito.awtt.protocol.http;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * @author Gerald
 *
 */
@RunWith(Suite.class)
@SuiteClasses({
        HeaderFieldTest.class, HTTPTest.class, MessageTest.class, ResponseTest.class
})
public class HttpTestSuite {

}
