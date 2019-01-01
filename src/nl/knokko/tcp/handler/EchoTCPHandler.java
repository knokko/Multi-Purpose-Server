package nl.knokko.tcp.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class EchoTCPHandler implements TCPHandler {

	@Override
	public void onOpen(ChannelHandlerContext ctx) {
		System.out.println("tcp opened");
	}

	@Override
	public void onMessage(ByteBuf message, ChannelHandlerContext ctx) {
		ctx.writeAndFlush(message);
		System.out.println("tcp message");
	}

	@Override
	public void onClose(ChannelHandlerContext ctx) {
		System.out.println("tcp closed");
	}
}