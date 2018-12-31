package nl.knokko.http.response;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;

import static nl.knokko.multiserver.helper.WebHelper.*;

import java.nio.charset.StandardCharsets;

public class SimpleHTTPResponse implements HTTPResponse {
	
	private static final ByteBufAllocator ALLOCATOR = new PooledByteBufAllocator();
	
	private final ByteBuf response;
	
	public SimpleHTTPResponse(String contentType, byte[] payload) {
		byte[] contentTypeBytes = contentType.getBytes(StandardCharsets.UTF_8);
		response = ALLOCATOR.buffer(HTTP_LINE.length + contentTypeBytes.length + payload.length + 5 * LINE_TERMINATOR.length);
		response.writeBytes(HTTP_LINE);
		response.writeBytes(LINE_TERMINATOR);
		response.writeBytes(contentTypeBytes);
		response.writeBytes(LINE_TERMINATOR);
		response.writeBytes(LINE_TERMINATOR);
		response.writeBytes(payload);
		response.writeBytes(LINE_TERMINATOR);
		response.writeBytes(LINE_TERMINATOR);
	}

	@Override
	public ByteBuf getResponse() {
		return response.retainedDuplicate();
	}
}