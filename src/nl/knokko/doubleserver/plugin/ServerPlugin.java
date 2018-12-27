package nl.knokko.doubleserver.plugin;

import java.lang.reflect.Field;
import java.util.List;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import net.minecraft.server.v1_12_R1.MinecraftServer;
import net.minecraft.server.v1_12_R1.ServerConnection;
import nl.knokko.doubleserver.handler.WebHandler;
import nl.knokko.doubleserver.listener.*;

public class ServerPlugin extends JavaPlugin implements Listener {

	@Override
	@SuppressWarnings("unchecked")
	public void onEnable() {
		try {
			// Hacking my way into the network channels requires some deprecated methods and
			// dirty reflection
			@SuppressWarnings("deprecation")
			MinecraftServer server = MinecraftServer.getServer();
			ServerConnection connection = server.an();
			Field endPointsField = ServerConnection.class.getDeclaredField("g");
			endPointsField.setAccessible(true);
			List<ChannelFuture> endPoints = (List<ChannelFuture>) endPointsField.get(connection);
			ChannelFuture oldEndPoint = endPoints.get(0);

			// This channel handler will register a channel handler for every connecting
			// client that will
			// inspect any message before it reaches the Minecraft code.
			oldEndPoint.channel().pipeline().addFirst("multipurpose_server_inspector", new ChannelInboundHandlerAdapter() {

				@Override
				public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
					super.channelRead(ctx, msg);
					Channel channel = (Channel) msg;
					ChannelPipeline pipeline = channel.pipeline();

					// This channel handler will be registered for every connection client that will
					// inspect
					// any message before it reaches the Minecraft code.
					pipeline.addFirst("multipurpose_handler_inspector", new ChannelInboundHandlerAdapter() {

						private boolean deactivated;

						private ChannelListener listener;

						@Override
						public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
							try {
								if (!deactivated) {
									ByteBuf message = (ByteBuf) msg;
									if (listener != null) {
										listener.read(ctx, message);
									} else {
										byte firstByte = message.getByte(0);

										// All Minecraft connections start with the byte 16
										if (firstByte == 16) {
											deactivated = true;
											super.channelRead(ctx, msg);
										}

										// All insecure web connections start with the byte 71
										else if (firstByte == 71) {
											byte[] data = new byte[message.readableBytes()];
											message.getBytes(0, data);
											WebHandler.Type type = WebHandler.determineConnectionType(data);
											if (type == WebHandler.Type.HTTP) {
												listener = new HTTPListener();
												listener.readInitial(ctx, message);
											} else if (type == WebHandler.Type.WEBSOCKET) {
												// TODO Find a nice way to handle web socket connections
												listener = new WebSocketListener();
												listener.readInitial(ctx, message);
											} else {
												deactivated = true;
												super.channelRead(ctx, msg);
											}
										}

										// All secure web connections start with the byte 22
										else if (firstByte == 22) {
											// TODO implement the secure web protocols and find a way to read this stuff
											// and find the difference
											System.out.println(
													"We are dealing with a secure websocket or https connection");
											byte[] data = new byte[message.readableBytes()];
											message.getBytes(0, data);
											System.out.println(new String(data));
										}

										// My applications
										else if (firstByte == 31) {
											listener = new TCPListener();
											listener.readInitial(ctx, message);
										} else {
											System.out.println("Unknown connection type");
											deactivated = true;
											super.channelRead(ctx, msg);
										}
									}
								} else {
									super.channelRead(ctx, msg);
								}
							} catch (Exception ex) {
								ex.printStackTrace();
							}
						}
					});
				}
			});
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}