package nl.knokko.multiserver.listener;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public interface ChannelListener {

	void read(ChannelHandlerContext ctx, ByteBuf message);
	
	void onClose(ChannelHandlerContext ctx);
}