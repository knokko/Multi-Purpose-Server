package nl.knokko.multiserver.listener;

import org.bukkit.Bukkit;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import nl.knokko.multiserver.plugin.ServerPlugin;
import nl.knokko.tcp.handler.TCPHandler;

public class TCPSocketListener implements ChannelListener {
	
	private final TCPHandler handler;
	
	private ChannelHandlerContext ctx;
	
	public TCPSocketListener(TCPHandler handler) {
		this.handler = handler;
	}

	@Override
	public void read(ChannelHandlerContext ctx, ByteBuf message) {
		this.ctx = ctx;
		Bukkit.getScheduler().scheduleSyncDelayedTask(ServerPlugin.getInstance(), () -> {
			handler.onMessage(message, ctx);
		});
	}

	public void readInitial(ChannelHandlerContext ctx, ByteBuf message) {
		this.ctx = ctx;
		Bukkit.getScheduler().scheduleSyncDelayedTask(ServerPlugin.getInstance(), () -> {
			handler.onOpen(ctx);
			handler.onMessage(message, ctx);
		});
	}
	
	public boolean isClosed() {
		return !ctx.channel().isOpen();
	}
	
	public void onServerStop() {
		if (ctx.channel().isOpen()) {
			ctx.close();
		}
	}
	
	public ChannelHandlerContext getContext() {
		return ctx;
	}

	@Override
	public void onClose(ChannelHandlerContext ctx) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(ServerPlugin.getInstance(), () -> {
			handler.onClose(ctx);
		});
	}
}