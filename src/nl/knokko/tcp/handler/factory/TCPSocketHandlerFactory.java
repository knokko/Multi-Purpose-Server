package nl.knokko.tcp.handler.factory;

import io.netty.channel.ChannelHandlerContext;
import nl.knokko.tcp.handler.TCPHandler;

public interface TCPSocketHandlerFactory {
	
	TCPHandler createHandler(ChannelHandlerContext ctx);
}