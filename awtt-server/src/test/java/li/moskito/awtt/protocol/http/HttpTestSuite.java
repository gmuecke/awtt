/**
 * 
 */
package li.moskito.awtt.protocol.http;

import li.moskito.awtt.protocol.MessageChannelOptionsTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * @author Gerald
 */
@RunWith(Suite.class)
@SuiteClasses({
        ContentTypeTest.class, HttpChannelTest.class, HttpHeaderFieldTest.class, HttpHeaderTest.class,
        HttpMessageTest.class, HttpProtocolExceptionTest.class, HttpProtocolHandlerTest.class, HttpRequestTest.class,
        HttpResponseTest.class, HttpStatusCodesTest.class, HTTPTest.class, RequestHeadersTest.class,
        ResponseHeadersTest.class, MessageChannelOptionsTest.class
})
public class HttpTestSuite {

}
