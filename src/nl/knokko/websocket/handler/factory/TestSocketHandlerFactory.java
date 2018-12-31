package nl.knokko.websocket.handler.factory;

import io.netty.channel.ChannelHandlerContext;
import nl.knokko.websocket.handler.TestSocketHandler;
import nl.knokko.websocket.handler.WebSocketHandler;

public class TestSocketHandlerFactory implements WebSocketHandlerFactory {

	@Override
	public WebSocketHandler createHandler(ChannelHandlerContext ctx) {
		return new TestSocketHandler();
	}
}