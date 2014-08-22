/**
 * 
 */
package li.moskito.awtt.protocol;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * @author Gerald
 */
@RunWith(Suite.class)
@SuiteClasses({
        BinaryBodyTest.class, CustomHeaderFieldDefinitionTest.class, HeaderFieldTest.class, HeaderTest.class,
        MessageTest.class, ProtocolExceptionTest.class, ProtocolRegistryTest.class
})
public class ProtocolTestSuite {

}
