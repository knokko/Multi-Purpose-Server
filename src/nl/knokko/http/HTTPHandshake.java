package nl.knokko.http;

import java.nio.charset.StandardCharsets;

import nl.knokko.util.ArrayHelper;

public class HTTPHandshake {
	
	private static final byte SPACE = (byte) ' ';
	
	private final byte[] data;
	
	public HTTPHandshake(byte[] fromClient) {
		data = fromClient;
	}
	
	public String findRequestedFile() {
		int index = ArrayHelper.indexOf(data, SPACE, 0) + 2;
		int indexSpaceEnd = ArrayHelper.indexOf(data, SPACE, index);
		return new String(data, index, indexSpaceEnd - index, StandardCharsets.UTF_8);
	}
}