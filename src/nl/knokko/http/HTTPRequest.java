package nl.knokko.http;

/**
 * Represents an HTTP request. This class is lazy, so this class won't search for header fields in its data
 * until the find method is invoked.
 * @author knokko
 *
 */
public abstract class HTTPRequest {
	
	protected final byte[] data;
	
	/**
	 * Creates an HTTP request that is backed by the given byte array.
	 * @param fromClient The exact bytes that were sent by the client
	 */
	public HTTPRequest(byte[] fromClient) {
		data = fromClient;
	}
	
	/**
	 * Searches this GET request for the specified header field. If the request contains the header field, the
	 * value of the header field will be returned. Otherwise, this method returns null.
	 * @param headerField The header field
	 * @return The value of the header field, or null if the request doesn't contain the header field.
	 */
	public String find(HeaderField headerField) {
		return headerField.getValue(data);
	}
}