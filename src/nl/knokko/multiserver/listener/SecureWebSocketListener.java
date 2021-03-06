package nl.knokko.multiserver.listener;

import java.nio.charset.StandardCharsets;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class SecureWebSocketListener implements ChannelListener {

	@Override
	public void read(ChannelHandlerContext ctx, ByteBuf message) {
		byte[] data = new byte[message.readableBytes()];
		message.readBytes(data);
		System.out.println("Read secure websocket: " + new String(data, StandardCharsets.UTF_8));
	}

	public void readInitial(ChannelHandlerContext ctx, byte[] data) {
		System.out.println("Read initial secure websocket: " + new String(data, StandardCharsets.UTF_8));
	}

	@Override
	public void onClose(ChannelHandlerContext ctx) {}
}