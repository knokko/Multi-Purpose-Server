package nl.knokko.http.handler;

import nl.knokko.http.HTTPHandshake;
import nl.knokko.http.response.HTTPResponse;

public interface HTTPHandler {
	
	HTTPResponse process(HTTPHandshake handshake);
}