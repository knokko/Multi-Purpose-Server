package nl.knokko.multiserver.listener;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import nl.knokko.http.HTTPHandshake;
import nl.knokko.http.handler.HTTPHandler;
import nl.knokko.http.response.HTTPResponse;
import nl.knokko.multiserver.plugin.ServerPlugin;

public class HTTPListener implements ChannelListener {

	@Override
	public void read(ChannelHandlerContext ctx, ByteBuf message) {
		// Shouldn't happen
		ctx.close();
	}

	public void readHandshake(ChannelHandlerContext ctx, byte[] data) {
		HTTPHandler handler = ServerPlugin.getHTTPHandler();
		if (handler != null) {
			HTTPResponse response = handler.process(new HTTPHandshake(data));
			if (response != null) {
				ctx.writeAndFlush(response.getResponse());
			}
		}
		ctx.close();
	}

	@Override
	public void onClose(ChannelHandlerContext ctx) {}
}