package nl.knokko.http;

import java.nio.charset.StandardCharsets;

import nl.knokko.multiserver.helper.WebHelper;
import nl.knokko.util.ArrayHelper;

/**
 * Represents a header field of an HTTP request.
 * @author knokko
 *
 */
public class HeaderField {
	
	private final byte[] lowerCase;
	private final byte[] upperCase;
	
	/**
	 * Creates a new header field representing the given string
	 * @param lowerCaseString The name of the header field in lowercase letters only
	 */
	public HeaderField(String lowerCaseString) {
		lowerCase = lowerCaseString.getBytes(StandardCharsets.UTF_8);
		upperCase = new byte[lowerCase.length];
		ArrayHelper.toUpperCase(lowerCase, this.upperCase);
	}
	
	public String createLowerCaseString() {
		return new String(lowerCase, StandardCharsets.UTF_8);
	}
	
	public String createUpperCaseString() {
		return new String(upperCase, StandardCharsets.UTF_8);
	}
	
	@Override
	public String toString() {
		return createLowerCaseString();
	}
	
	/**
	 * Searches the GET request for this header field. If the request contains this header field, the
	 * value of this header field will be returned. Otherwise, this method returns null.
	 * @param requestData The bytes that make up the HTTP request
	 * @return The value of the header field, or null if the request doesn't contain the header field.
	 */
	public String getValue(byte[] requestData) {
		int index = ArrayHelper.indexOf(requestData, lowerCase, upperCase, 0);
		if (index != -1) {
			int startIndex = index + lowerCase.length;
			int endIndex = ArrayHelper.indexOf(requestData, WebHelper.LINE_TERMINATOR[0], startIndex);
			return new String(requestData, startIndex, endIndex - startIndex, StandardCharsets.UTF_8);
		} else {
			return null;
		}
	}
}