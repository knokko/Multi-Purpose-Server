package nl.knokko.doubleserver.listener;

import java.nio.charset.StandardCharsets;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class WebSocketListener implements ChannelListener {

	@Override
	public void read(ChannelHandlerContext ctx, ByteBuf message) {
		byte[] data = new byte[message.readableBytes()];
		message.readBytes(data);
		System.out.println("Read websocket: " + new String(data, StandardCharsets.UTF_8));
	}

	@Override
	public void readInitial(ChannelHandlerContext ctx, ByteBuf message) {
		byte[] data = new byte[message.readableBytes()];
		message.readBytes(data);
		System.out.println("Read initial websocket: " + new String(data, StandardCharsets.UTF_8));
	}
}