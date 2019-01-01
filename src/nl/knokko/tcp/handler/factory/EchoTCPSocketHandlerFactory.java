package nl.knokko.tcp.handler.factory;

import io.netty.channel.ChannelHandlerContext;
import nl.knokko.tcp.handler.EchoTCPHandler;
import nl.knokko.tcp.handler.TCPHandler;

public class EchoTCPSocketHandlerFactory implements TCPSocketHandlerFactory {

	@Override
	public TCPHandler createHandler(ChannelHandlerContext ctx) {
		return new EchoTCPHandler();
	}
}