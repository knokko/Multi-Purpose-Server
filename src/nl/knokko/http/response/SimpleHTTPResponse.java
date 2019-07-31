package nl.knokko.http.response;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;

import static nl.knokko.multiserver.helper.WebHelper.*;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import static java.nio.charset.StandardCharsets.UTF_8;;

public class SimpleHTTPResponse implements HTTPResponse {
	
	private static final ByteBufAllocator ALLOCATOR = new PooledByteBufAllocator();
	
	private static final byte[] CONTENT_TYPE = "Content-Type: ".getBytes(UTF_8);
	private static final byte[] CONTENT_LENGTH = "Content-Length: ".getBytes(UTF_8);
	private static final byte[] DATE = "Date: ".getBytes(UTF_8);
	private static final byte[] SERVER = "Server: MC Web Server (https://github.com/knokko/Multi-Purpose-Server)".getBytes(UTF_8);
	
	private final ByteBuf response;
	
	public SimpleHTTPResponse(String contentType, byte[] payload) {
		byte[] contentTypeBytes = contentType.getBytes(UTF_8);
		byte[] contentLengthBytes = Integer.toString(payload.length).getBytes(UTF_8);
		byte[] date = (DateTimeFormatter.ofPattern("EEE, dd LLL uuuu HH:mm:ss").format(OffsetDateTime.now(Clock.systemUTC())) + " GMT").getBytes(UTF_8);
		response = ALLOCATOR.buffer(HTTP_LINE.length + contentTypeBytes.length + CONTENT_TYPE.length 
				+ CONTENT_LENGTH.length + contentLengthBytes.length + payload.length 
				+ DATE.length + date.length + SERVER.length + 8 * LINE_TERMINATOR.length);
		response.writeBytes(HTTP_LINE);
		response.writeBytes(LINE_TERMINATOR);
		
		response.writeBytes(CONTENT_TYPE);
		response.writeBytes(contentTypeBytes);
		response.writeBytes(LINE_TERMINATOR);
		
		response.writeBytes(CONTENT_LENGTH);
		response.writeBytes(contentLengthBytes);
		response.writeBytes(LINE_TERMINATOR);
		
		response.writeBytes(DATE);
		response.writeBytes(date);
		response.writeBytes(LINE_TERMINATOR);
		
		// Server is entirely hardcoded unlike the previous header fields
		response.writeBytes(SERVER);
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