package nl.knokko.doubleserver.listener;

import java.util.Arrays;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class TCPListener implements ChannelListener {

	@Override
	public void read(ChannelHandlerContext ctx, ByteBuf message) {
		byte[] data = new byte[message.readableBytes()];
		message.readBytes(data);
		System.out.println("Received TCP data: " + Arrays.toString(data));
	}

	@Override
	public void readInitial(ChannelHandlerContext ctx, ByteBuf message) {
		byte[] data = new byte[message.readableBytes()];
		message.readBytes(data);
		System.out.println("Received initial TCP data: " + Arrays.toString(data));
	}
}