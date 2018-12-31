package nl.knokko.multiserver.listener;

import org.bukkit.Bukkit;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import nl.knokko.multiserver.plugin.ServerPlugin;
import nl.knokko.tcp.handler.TCPHandler;

public class TCPListener implements ChannelListener {
	
	private final TCPHandler handler;
	
	private ChannelHandlerContext ctx;
	
	public TCPListener(TCPHandler handler) {
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

	@Override
	public void onClose(ChannelHandlerContext ctx) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(ServerPlugin.getInstance(), () -> {
			handler.onClose(ctx);
		});
	}
}