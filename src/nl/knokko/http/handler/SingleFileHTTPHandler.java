package nl.knokko.http.handler;

import java.io.File;
import java.io.IOException;

import nl.knokko.http.HTTPGetRequest;
import nl.knokko.http.response.HTTPResponse;
import nl.knokko.http.response.SimpleHTTPResponse;
import nl.knokko.multiserver.plugin.ServerPlugin;
import nl.knokko.util.ArrayHelper;

public class SingleFileHTTPHandler implements HTTPHandler {
	
	private HTTPResponse response;
	
	public SingleFileHTTPHandler(File file) {
		try {
			response = new SimpleHTTPResponse(ServerPlugin.getContentTypeForFile(file.getName()), ArrayHelper.readFile(file));
		} catch (IOException e) {
			throw new IllegalArgumentException("Can't read supplied file", e);
		}
	}

	@Override
	public HTTPResponse process(HTTPGetRequest handshake) {
		return response;
	}
}