package nl.knokko.tcp.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public interface TCPHandler {
	
	void onOpen(ChannelHandlerContext ctx);
	
	void onMessage(ByteBuf message, ChannelHandlerContext ctx);
	
	void onClose(ChannelHandlerContext ctx);
}