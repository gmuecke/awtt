/**
 * 
 */
package li.moskito.awtt.protocol.http;

import li.moskito.awtt.protocol.BinaryBodyTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * @author Gerald
 */
@RunWith(Suite.class)
@SuiteClasses({
        BinaryBodyTest.class, HttpHeaderFieldTest.class, HTTPTest.class, HttpMessageTest.class, HttpResponseTest.class,
        HttpRequestTest.class
})
public class HttpTestSuite {

}
