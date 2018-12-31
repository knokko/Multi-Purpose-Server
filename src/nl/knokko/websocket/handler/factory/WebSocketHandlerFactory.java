package nl.knokko.websocket.handler.factory;

import io.netty.channel.ChannelHandlerContext;
import nl.knokko.websocket.handler.WebSocketHandler;

public interface WebSocketHandlerFactory {
	
	WebSocketHandler createHandler(ChannelHandlerContext ctx);
}