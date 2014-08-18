/**
 * 
 */
package li.moskito.awtt.protocol.http;

import li.moskito.awtt.protocol.Command;

/**
 * see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec5.html#sec5.1.1">HTTP Request Method</a>
 * 
 * @author Gerald
 */
public enum HttpCommands implements Command {

    OPTIONS,
    GET,
    HEAD,
    POST,
    PUT,
    DELETE,
    TRACE,
    CONNECT;
}
