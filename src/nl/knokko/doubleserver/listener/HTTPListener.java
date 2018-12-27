package nl.knokko.doubleserver.listener;

import java.nio.charset.StandardCharsets;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import nl.knokko.util.ArrayHelper;

import static nl.knokko.doubleserver.handler.WebHandler.*;

public class HTTPListener implements ChannelListener {

	@Override
	public void read(ChannelHandlerContext ctx, ByteBuf message) {
		System.out.println("Closing connection because someone attempted a second http");
		ctx.close();
	}

	@Override
	public void readInitial(ChannelHandlerContext ctx, ByteBuf message) {
		byte[] data = new byte[message.readableBytes()];
		message.readBytes(data);
		byte[][] inputLines = ArrayHelper.split(data, LINE_TERMINATOR);
		String firstInputLine = new String(inputLines[0], StandardCharsets.UTF_8);
		String requestedFile = firstInputLine.substring(firstInputLine.indexOf(" ") + 2, firstInputLine.lastIndexOf(" "));
		byte[] contentType;
		byte[] response;
		if (requestedFile.equals("favicon.ico")) {
			response = FAVICON;
			contentType = CONTENT_TYPE_IMAGE;
		} else {
			response = PAGE;
			contentType = CONTENT_TYPE_HTML;
		}
		ByteBuf responseBuffer = ctx.alloc().buffer(HTTP_LINE.length + contentType.length + response.length + 5 * LINE_TERMINATOR.length);
		responseBuffer.writeBytes(HTTP_LINE);
		responseBuffer.writeBytes(LINE_TERMINATOR);
		responseBuffer.writeBytes(contentType);
		responseBuffer.writeBytes(LINE_TERMINATOR);
		responseBuffer.writeBytes(LINE_TERMINATOR);
		responseBuffer.writeBytes(response);
		responseBuffer.writeBytes(LINE_TERMINATOR);
		responseBuffer.writeBytes(LINE_TERMINATOR);
		ctx.writeAndFlush(responseBuffer);
		ctx.close();
	}
}