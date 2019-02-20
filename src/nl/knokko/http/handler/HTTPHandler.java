package nl.knokko.http.handler;

import nl.knokko.http.HTTPGetRequest;
import nl.knokko.http.response.HTTPResponse;

public interface HTTPHandler {
	
	HTTPResponse process(HTTPGetRequest handshake);
}