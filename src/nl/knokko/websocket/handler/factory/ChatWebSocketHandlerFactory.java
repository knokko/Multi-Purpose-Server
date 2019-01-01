package nl.knokko.websocket.handler.factory;

import io.netty.channel.ChannelHandlerContext;
import nl.knokko.websocket.handler.ChatWebSocketHandler;
import nl.knokko.websocket.handler.WebSocketHandler;

public class ChatWebSocketHandlerFactory implements WebSocketHandlerFactory {

	@Override
	public WebSocketHandler createHandler(ChannelHandlerContext ctx) {
		return new ChatWebSocketHandler();
	}
}