package nl.knokko.http.response;

import io.netty.buffer.ByteBuf;

public interface HTTPResponse {
	
	ByteBuf getResponse();
}