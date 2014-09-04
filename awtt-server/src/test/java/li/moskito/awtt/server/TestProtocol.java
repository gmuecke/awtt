/**
 * 
 */
package li.moskito.awtt.server;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import li.moskito.awtt.protocol.Message;
import li.moskito.awtt.protocol.MessageChannel;
import li.moskito.awtt.protocol.MessageChannelOptions;
import li.moskito.awtt.protocol.Protocol;

/**
 * @author Gerald
 */
public class TestProtocol implements Protocol {

    @Override
    public int getDefaultPort() {
        return 55000;
    }

    @Override
    public MessageChannel openChannel() {
        final MessageChannel channel = mock(MessageChannel.class);
        when(channel.getOption(MessageChannelOptions.KEEP_ALIVE_TIMEOUT)).thenReturn(Integer.valueOf(5));
        return channel;
    }

    @Override
    public Message process(final Message message) {
        return message;
    }

}
