package li.moskito.awtt.protocol.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import li.moskito.awtt.common.Configurable;
import li.moskito.awtt.protocol.HeaderField;
import li.moskito.awtt.protocol.Message;
import li.moskito.awtt.protocol.http.HTTP.ResponseOptions;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class HTTPTest {

    @Mock
    private TestHandler mockHandler;

    public static class TestHandler extends HttpProtocolHandler implements Configurable {

        private static TestHandler mock;

        @Override
        public void configure(final HierarchicalConfiguration config) throws ConfigurationException {
            mock.configure(config);

        }

        @Override
        protected HttpResponse onGet(final HttpRequest httpRequest) {
            return mock.onGet(httpRequest);
        }

    }

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private HttpRequest request;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private HttpResponse response;

    private HTTP http;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        TestHandler.mock = this.mockHandler;
        this.http = new HTTP();

    }

    @Test
    public void testProcess_HttpRequest_noHandlers() throws Exception {
        final HttpResponse response = this.http.process(this.request);
        assertNotNull(response);
        assertEquals(HttpStatusCodes.NOT_IMPLEMENTED, response.getStatusCode());

    }

    @Test
    public void testProcess_HttpRequest_withHandlers() throws Exception {
        this.testConfigure();
        when(this.mockHandler.accepts(this.request)).thenReturn(true);
        when(this.mockHandler.onGet(this.request)).thenReturn(this.response);
        when(this.request.getCommand()).thenReturn(HttpCommands.GET);

        final HttpResponse actualResponse = this.http.process(this.request);
        assertNotNull(actualResponse);
        assertEquals(this.response, actualResponse);

    }

    @Test
    public void testProcess_HttpResponse() throws Exception {
        final HttpResponse response = mock(HttpResponse.class);
        assertEquals(response, this.http.process(response));
    }

    @Test
    public void testProcess_Message_request() throws Exception {
        this.testConfigure();
        when(this.mockHandler.accepts(this.request)).thenReturn(true);
        when(this.mockHandler.onGet(this.request)).thenReturn(this.response);
        when(this.request.getCommand()).thenReturn(HttpCommands.GET);

        final Message actualResponse = this.http.process((Message) this.request);
        assertNotNull(actualResponse);
        assertEquals(this.response, actualResponse);

    }

    @Test
    public void testProcess_Message_response() throws Exception {
        final HttpResponse response = mock(HttpResponse.class);
        assertEquals(response, this.http.process((Message) response));
    }

    @Test
    public void testProcess_Message_any() throws Exception {
        final Message request = mock(Message.class);
        final Message responseMessage = this.http.process(request);
        assertTrue(this.response instanceof HttpResponse);
        final HttpResponse response = (HttpResponse) responseMessage;
        assertEquals(HttpStatusCodes.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void testOpenChannel() throws Exception {
        final HttpChannel channel = this.http.openChannel();
        assertNotNull(channel);

    }

    @Test
    public void testGetDefaultPort() throws Exception {
        assertEquals(80, this.http.getDefaultPort());
    }

    @Test
    public void testConfigure() throws Exception {
        final HierarchicalConfiguration config = new HierarchicalConfiguration();
        config.addProperty("handler", "");
        config.addProperty("handler.@class", "li.moskito.awtt.protocol.http.HTTPTest$TestHandler");
        this.http.configure(config);

        verify(this.mockHandler).configure(any(HierarchicalConfiguration.class));
    }

    @Test
    public void testToHttpDate() throws Exception {
        final Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
        cal.set(Calendar.DAY_OF_WEEK, 5); // THU = 5
        cal.set(Calendar.DAY_OF_MONTH, 8);
        cal.set(Calendar.MONTH, 4); // MAY = 4
        cal.set(Calendar.YEAR, 2014);
        cal.set(Calendar.HOUR_OF_DAY, 13);
        cal.set(Calendar.MINUTE, 49);
        cal.set(Calendar.SECOND, 1);

        final String httpDate = HTTP.toHttpDate(cal.getTime());
        assertEquals("Thu, 08 May 2014 13:49:01 GMT", httpDate);
    }

    @Test
    public void testHttpDateConversionRoundtrip() throws Exception {
        final Date date = new Date(new Date().getTime() / 1000 * 1000); // strip ms
        assertEquals(date, HTTP.fromHttpDate(HTTP.toHttpDate(date)));

        final String httpDate = "Thu, 08 May 2014 13:49:01 GMT";
        assertEquals(httpDate, HTTP.toHttpDate(HTTP.fromHttpDate(httpDate)));
    }

    @Test
    public void testFromHttpDate_validDate() throws Exception {
        final Date date = HTTP.fromHttpDate("Thu, 08 May 2014 13:49:01 GMT");
        assertNotNull(date);
        final Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        assertEquals(5, cal.get(Calendar.DAY_OF_WEEK)); // 5 = THU
        assertEquals(8, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals(4, cal.get(Calendar.MONTH)); // 4 = May
        assertEquals(2014, cal.get(Calendar.YEAR));
        assertEquals(15, cal.get(Calendar.HOUR_OF_DAY)); // in UTC!
        assertEquals(49, cal.get(Calendar.MINUTE));
        assertEquals(1, cal.get(Calendar.SECOND));
    }

    @Test
    public void testCreateResponse() throws Exception {
        final HttpResponse response = HTTP.createResponse(HttpStatusCodes.CONTINUE);
        assertNotNull(response);
        assertEquals(HttpStatusCodes.CONTINUE, response.getHeader().getStatusCode());
        assertNotNull(response.getHeader().getField(ResponseHeaders.DATE));
        assertNotNull(response.getHeader().getField(ResponseHeaders.DATE).getValue());
    }

    @Test
    public void testCreateResponse_withCloseOption() throws Exception {
        final HttpResponse response = HTTP.createResponse(HttpStatusCodes.CONTINUE, ResponseOptions.FORCE_CLOSE);
        assertNotNull(response);
        assertEquals(HttpStatusCodes.CONTINUE, response.getHeader().getStatusCode());
        assertNotNull(response.getHeader().getField(ResponseHeaders.DATE));
        assertNotNull(response.getHeader().getField(ResponseHeaders.DATE).getValue());

        assertNotNull(response.getHeader().getField(ResponseHeaders.CONNECTION));
        assertEquals("close", response.getHeader().getField(ResponseHeaders.CONNECTION).getValue());

        assertNotNull(response.getHeader().getField(ResponseHeaders.CONTENT_LENGTH));
        assertEquals("0", response.getHeader().getField(ResponseHeaders.CONTENT_LENGTH).getValue());
    }

    @Test
    public void testIsClosedByRequest_http11_close() throws Exception {
        when(this.request.getHeader().getVersion()).thenReturn(HttpVersion.HTTP_1_1);
        final HttpHeaderField connectionField = mock(HttpHeaderField.class);
        when(connectionField.getValue()).thenReturn("close");
        when(this.request.getHeader().hasField(RequestHeaders.CONNECTION)).thenReturn(true);
        when(this.request.getHeader().getField(RequestHeaders.CONNECTION)).thenReturn(connectionField);

        assertTrue(this.http.isClosedByRequest(this.request));
    }

    @Test
    public void testIsClosedByRequest_http11_defaultKeepAlive() throws Exception {
        when(this.request.getHeader().getVersion()).thenReturn(HttpVersion.HTTP_1_1);
        assertFalse(this.http.isClosedByRequest(this.request));
    }

    @Test
    public void testIsClosedByRequest_http10_keepAlive() throws Exception {
        when(this.request.getHeader().getVersion()).thenReturn(HttpVersion.HTTP_1_0);
        final HttpHeaderField connectionField = mock(HttpHeaderField.class);
        when(connectionField.getValue()).thenReturn("keep-alive");
        when(this.request.getHeader().hasField(RequestHeaders.CONNECTION)).thenReturn(true);
        when(this.request.getHeader().getField(RequestHeaders.CONNECTION)).thenReturn(connectionField);

        assertFalse(this.http.isClosedByRequest(this.request));
    }

    @Test
    public void testIsClosedByRequest_http10_defaultClose() throws Exception {
        when(this.request.getHeader().getVersion()).thenReturn(HttpVersion.HTTP_1_0);
        assertTrue(this.http.isClosedByRequest(this.request));
    }

    @Test
    public void testGetKeepAliverHeaders() throws Exception {
        final List<HeaderField> headers = this.http.getKeepAliverHeaders(3, 257);
        assertEquals(2, headers.size());
        assertEquals(ResponseHeaders.CONNECTION, headers.get(0).getHeaderFieldDefinition());
        assertEquals("Keep-Alive", headers.get(0).getValue());
        assertEquals("Keep-Alive", headers.get(1).getHeaderFieldDefinition().getName());
        assertEquals("timeout=3, max=257", headers.get(1).getValue());

    }
}
