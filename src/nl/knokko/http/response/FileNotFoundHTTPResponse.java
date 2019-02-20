package nl.knokko.http.response;

import java.nio.charset.StandardCharsets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import nl.knokko.multiserver.helper.WebHelper;

public class FileNotFoundHTTPResponse implements HTTPResponse {
	
	private static final ByteBuf RESPONSE;
	
	static {
		byte[] bytes = "HTTP/1.1 404 Not Found.".getBytes(StandardCharsets.UTF_8);
		RESPONSE = new PooledByteBufAllocator().buffer(bytes.length + 2 * WebHelper.LINE_TERMINATOR.length);
		RESPONSE.writeBytes(bytes);
		RESPONSE.writeBytes(WebHelper.LINE_TERMINATOR);
		RESPONSE.writeBytes(WebHelper.LINE_TERMINATOR);
		// TODO I don't think this is entirely correct
	}

	@Override
	public ByteBuf getResponse() {
		return RESPONSE.retainedDuplicate();
	}
}