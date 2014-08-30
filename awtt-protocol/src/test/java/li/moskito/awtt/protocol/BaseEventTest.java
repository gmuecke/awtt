package li.moskito.awtt.protocol;

import static org.junit.Assert.assertEquals;
import li.moskito.awtt.protocol.Event.Type;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BaseEventTest {

    @Mock
    private Object eventData;

    @Mock
    private Type type;

    private BaseEvent<Object> baseEvent;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        this.baseEvent = new BaseEvent<Object>(this.type, this.eventData);

    }

    @Test
    public void testGetType() throws Exception {
        assertEquals(this.type, this.baseEvent.getType());
    }

    @Test
    public void testGetEventData() throws Exception {
        assertEquals(this.eventData, this.baseEvent.getEventData());
    }

}
