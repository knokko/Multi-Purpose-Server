package nl.knokko.http;

import java.nio.charset.StandardCharsets;

import nl.knokko.util.ArrayHelper;

/**
 * Represents an HTTP GET request. This class is lazy, so it doesn't search for header fields or the requested
 * file path until it's methods are invoked.
 * @author knokko
 *
 */
public class HTTPGetRequest extends HTTPRequest {
	
	private static final byte SPACE = (byte) ' ';
	
	/**
	 * Creates an HTTP GET request that will read from the given bytes.
	 * @param fromClient The exact bytes that were sent by the client
	 */
	public HTTPGetRequest(byte[] fromClient) {
		super(fromClient);
	}
	
	/**
	 * Finds and returns the filepath that was requested. When someone enters yourdomain/example/test in his
	 * browser, this method will return example/test. If someone simply enters yourdomain/, the empty string
	 * will be returned. For invalid requests, this method may return null.
	 * @return The file that was requested, or null if it can't be found because the request is invalid
	 */
	public String findRequestedFile() {
		int index = ArrayHelper.indexOf(data, SPACE, 0) + 2;
		if (index != 1) {
			int indexSpaceEnd = ArrayHelper.indexOf(data, SPACE, index);
			if (indexSpaceEnd != -1) {
				return new String(data, index, indexSpaceEnd - index, StandardCharsets.UTF_8);
			} else {
				return null;
			}
		} else {
			return null;
		}
	}
}