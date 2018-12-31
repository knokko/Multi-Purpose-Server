package nl.knokko.multiserver.listener;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.enums.Role;
import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.framing.*;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.HandshakeImpl1Server;
import org.java_websocket.handshake.Handshakedata;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.handshake.ServerHandshakeBuilder;

import com.google.common.collect.Lists;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import nl.knokko.multiserver.plugin.ServerPlugin;
import nl.knokko.websocket.handler.WebSocketHandler;

public class WebSocketListener implements ChannelListener, org.java_websocket.WebSocketListener {
	
	public static ByteBuffer convert(ByteBuf buf) {
		byte[] bytes;
		if (buf.hasArray()) {
			bytes = buf.array();
		} else {
			bytes = new byte[buf.readableBytes()];
			buf.readBytes(bytes);
		}
		return convert(bytes);
	}
	
	public static ByteBuffer convert(byte[] bytes) {
		return ByteBuffer.wrap(bytes);
	}
	
	public static ByteBuf convert(ByteBuffer buffer, ChannelHandlerContext ctx) {
		byte[] bytes;
		if (buffer.hasArray()) {
			bytes = buffer.array();
		} else {
			bytes = new byte[buffer.remaining()];
			buffer.get(bytes);
		}
		return convert(bytes, ctx);
	}
	
	public static ByteBuf convert(byte[] bytes, ChannelHandlerContext ctx) {
		ByteBuf buf = ctx.alloc().buffer(bytes.length);
		buf.writeBytes(bytes);
		return buf;
	}
	
	private final Draft draft;
	private final WebSocketHandler handler;
	private final WebSocketImpl impl;
	
	private ChannelHandlerContext ctx;
	private boolean calledOnClose;
	
	public WebSocketListener(WebSocketHandler handler) {
		draft = new Draft_6455();
		draft.setParseMode(Role.SERVER);
		this.handler = handler;
		this.impl = new WebSocketImpl(this, Lists.newArrayList(draft));
	}

	@Override
	public void read(ChannelHandlerContext ctx, ByteBuf message) {
		this.ctx = ctx;
		impl.decode(convert(message));
	}

	public void readInitial(ChannelHandlerContext ctx, byte[] message) {
		this.ctx = ctx;
		impl.decode(convert(message));
	}

	@Override
	public ServerHandshakeBuilder onWebsocketHandshakeReceivedAsServer(WebSocket conn, Draft draft,
			ClientHandshake request) throws InvalidDataException {
		System.out.println("onWebsocketHandshakeReceivedAsServer");
		return new HandshakeImpl1Server();
	}

	@Override
	public void onWebsocketHandshakeReceivedAsClient(WebSocket conn, ClientHandshake request, ServerHandshake response)
			throws InvalidDataException {
		throw new UnsupportedOperationException("I am a server, no client!");
	}

	@Override
	public void onWebsocketHandshakeSentAsClient(WebSocket conn, ClientHandshake request) throws InvalidDataException {
		throw new UnsupportedOperationException("I am a server, no client!");
	}

	@Override
	public void onWebsocketMessage(WebSocket conn, String message) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(ServerPlugin.getInstance(), () -> {
			handler.onMessage(message, conn);
		});
	}

	@Override
	public void onWebsocketMessage(WebSocket conn, ByteBuffer blob) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(ServerPlugin.getInstance(), () -> {
			handler.onMessage(blob, conn);
		});
	}

	@Override
	public void onWebsocketOpen(WebSocket conn, Handshakedata d) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(ServerPlugin.getInstance(), () -> {
			handler.onOpen((ClientHandshake) d, conn);
		});
	}

	@Override
	public void onWebsocketClose(WebSocket ws, int code, String reason, boolean remote) {
		if (!calledOnClose) {
			calledOnClose = true;
			// The calledOnClose is set to true immediately because this method is preferred over onClose
			Bukkit.getScheduler().scheduleSyncDelayedTask(ServerPlugin.getInstance(), () -> {
				handler.onClose(reason, code, ws);
			});
		}
	}

	@Override
	public void onWebsocketClosing(WebSocket ws, int code, String reason, boolean remote) {
		System.out.println("onWebsocketClosing");
		ctx.close();
	}

	@Override
	public void onWebsocketCloseInitiated(WebSocket ws, int code, String reason) {
		System.out.println("onWebsocketCloseInitiated");
	}

	@Override
	public void onWebsocketError(WebSocket conn, Exception ex) {
		Bukkit.getLogger().log(Level.WARNING, "A websocket error occured", ex);
	}

	@Override
	public void onWebsocketPing(WebSocket conn, Framedata f) {
		conn.sendFrame(new PongFrame((PingFrame) f));
	}

	@Override
	public void onWebsocketPong(WebSocket conn, Framedata f) {
		System.out.println("onWebsocketPong");
	}

	@Override
	public void onWriteDemand(WebSocket conn) {
		BlockingQueue<ByteBuffer> buffers = impl.outQueue;
		ByteBuffer next = buffers.poll();
		while (next != null) {
			ctx.writeAndFlush(convert(next, ctx));
			next = buffers.poll();
		}
	}

	@Override
	public InetSocketAddress getLocalSocketAddress(WebSocket conn) {
		return (InetSocketAddress) ctx.channel().localAddress();
	}

	@Override
	public InetSocketAddress getRemoteSocketAddress(WebSocket conn) {
		return (InetSocketAddress) ctx.channel().remoteAddress();
	}
	
	public WebSocketImpl getImplementation() {
		return impl;
	}

	@Override
	public void onClose(ChannelHandlerContext ctx) {
		if (!calledOnClose) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(ServerPlugin.getInstance(), () -> {
				if (!calledOnClose) {
					// The calledOnClose is set later than in onWebsocketClose because onWebsocketClose is
					// the preferred close method
					handler.onClose("Disconnected", CloseFrame.ABNORMAL_CLOSE, impl);
					calledOnClose = true;
				}
			});
		}
	}
}