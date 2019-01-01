package nl.knokko.multiserver.plugin;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.java_websocket.framing.CloseFrame;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import net.minecraft.server.v1_12_R1.MinecraftServer;
import net.minecraft.server.v1_12_R1.ServerConnection;
import nl.knokko.http.handler.HTTPHandler;
import nl.knokko.http.handler.SimpleHTTPHandler;
import nl.knokko.http.handler.SingleFileHTTPHandler;
import nl.knokko.http.response.DefaultContentTypes;
import nl.knokko.multiserver.helper.WebHelper;
import nl.knokko.multiserver.listener.ChannelListener;
import nl.knokko.multiserver.listener.HTTPListener;
import nl.knokko.multiserver.listener.TCPSocketListener;
import nl.knokko.multiserver.listener.WebSocketListener;
import nl.knokko.tcp.handler.TCPHandler;
import nl.knokko.tcp.handler.factory.TCPSocketHandlerFactory;
import nl.knokko.websocket.handler.WebSocketHandler;
import nl.knokko.websocket.handler.factory.WebSocketHandlerFactory;

public class ServerPlugin extends JavaPlugin implements Listener {

	/**
	 * The first byte of the first message sent by a minecraft client is always 16.
	 */
	public static final byte FIRST_MINECRAFT_BYTE = 16;
	/**
	 * The first byte of the first message of a custom TCP connection must be 47 to
	 * distinguish it from other connection types.
	 */
	public static final byte FIRST_TCP_BYTE = 47;
	/**
	 * The first byte of the first message of an insecure web connection is always
	 * 71.
	 */
	public static final byte FIRST_WEB_BYTE = 71;
	/**
	 * The first byte of the first message of a secure web connection is always 22.
	 */
	public static final byte FIRST_SECURE_WEB_BYTE = 22;

	private static ServerPlugin instance;

	public static ServerPlugin getInstance() {
		return instance;
	}

	public static WebSocketHandlerFactory getWebSocketHandlerFactory() {
		return instance.webSocketHandlerFactory;
	}

	public static TCPSocketHandlerFactory getTCPSocketHandlerFactory() {
		return instance.tcpSocketHandlerFactory;
	}

	public static HTTPHandler getHTTPHandler() {
		return instance.httpHandler;
	}

	public static Collection<WebSocketListener> getWebSocketListeners() {
		return instance.webSocketListeners;
	}

	public static Collection<TCPSocketListener> getTCPSocketListeners() {
		return instance.tcpSocketListeners;
	}

	public static String getContentTypeForExtension(String extension) {
		return instance.contentTypes.get(extension);
	}

	public static String getContentTypeForFile(String fileName) {
		int indexDot = fileName.lastIndexOf(".");
		String contentType = null;
		if (indexDot != -1) {
			contentType = getContentTypeForExtension(fileName.substring(indexDot + 1));
		}
		if (contentType == null) {
			// I will have to choose something...
			contentType = "text/plain";
		}
		return contentType;
	}

	public static void registerContentType(String extension, String contentType) {
		instance.contentTypes.put(extension, contentType);
	}

	private Map<String, String> contentTypes;

	private HTTPHandler httpHandler;
	private WebSocketHandlerFactory webSocketHandlerFactory;
	private TCPSocketHandlerFactory tcpSocketHandlerFactory;

	private Collection<WebSocketListener> webSocketListeners;
	private Collection<TCPSocketListener> tcpSocketListeners;

	public void readConfig() {
		FileConfiguration config = getConfig();
		readWebsiteConfig(config);
		readTCPConfig(config);
	}

	public void readTCPConfig(FileConfiguration config) {
		String tcpSocketHandlerName = config.getString("tcp-socket-handler-factory");
		if (tcpSocketHandlerName == null) {
			tcpSocketHandlerFactory = null;
		} else {
			try {
				tcpSocketHandlerFactory = (TCPSocketHandlerFactory) Class.forName(tcpSocketHandlerName).newInstance();
			} catch (Exception ex) {
				Bukkit.getLogger().log(Level.SEVERE, "Failed to load the TCP handler factory " + tcpSocketHandlerName,
						ex);
				tcpSocketHandlerFactory = null;
			}
		}
	}

	public void readWebsiteConfig(FileConfiguration config) {
		ConfigurationSection contentTypesSection = config.getConfigurationSection("content-types");
		if (contentTypesSection == null) {
			contentTypes = DefaultContentTypes.get();
			contentTypesSection = config.createSection("content-types");
			Set<Entry<String, String>> entrySet = contentTypes.entrySet();
			for (Entry<String, String> entry : entrySet) {
				contentTypesSection.set(entry.getKey(), entry.getValue());
			}
		} else {
			contentTypes = new HashMap<String, String>();
			Set<Entry<String, Object>> givenContentTypes = contentTypesSection.getValues(false).entrySet();
			for (Entry<String, Object> entry : givenContentTypes) {
				if (entry.getValue() instanceof String) {
					contentTypes.put(entry.getKey(), (String) entry.getValue());
				} else {
					Bukkit.getLogger()
							.warning("The content type for extension " + entry.getKey() + " should be a text");
				}
			}
		}
		String httpHandlerName = config.getString("http-handler");
		if (httpHandlerName == null) {
			File webFolder = new File(getDataFolder() + "/website");
			if (webFolder.exists()) {
				File[] files = webFolder.listFiles();
				if (files == null) {
					Bukkit.getLogger().warning("The website folder should be a folder/directory");
					httpHandler = null;
				} else {
					if (files.length == 0) {
						Bukkit.getLogger().info(
								"No files were found in the website folder, so the server will not act as website.");
						httpHandler = null;
					} else if (files.length == 1) {
						httpHandler = new SingleFileHTTPHandler(files[0]);
					} else {
						httpHandler = new SimpleHTTPHandler(webFolder);
					}
				}
			} else {
				webFolder.mkdirs();
				httpHandler = null;
				Bukkit.getLogger().info("The website folder wasn't found, so a new one was created.");
			}
		} else {
			try {
				httpHandler = (HTTPHandler) Class.forName(httpHandlerName).newInstance();
			} catch (Exception ex) {
				Bukkit.getLogger().log(Level.SEVERE, "Failed to load http handler " + httpHandlerName, ex);
				httpHandler = null;
			}
		}
		String wsHandlerFactoryName = config.getString("websocket-handler-factory");
		if (wsHandlerFactoryName == null) {
			webSocketHandlerFactory = null;
		} else {
			try {
				webSocketHandlerFactory = (WebSocketHandlerFactory) Class.forName(wsHandlerFactoryName).newInstance();
			} catch (Exception ex) {
				Bukkit.getLogger().log(Level.SEVERE, "Failed to load websocket handler " + wsHandlerFactoryName, ex);
				webSocketHandlerFactory = null;
			}
		}
		saveConfig();
	}

	@Override
	@SuppressWarnings("unchecked")
	public void onEnable() {
		try {
			instance = this;
			readConfig();
			webSocketListeners = new LinkedList<WebSocketListener>();
			tcpSocketListeners = new LinkedList<TCPSocketListener>();

			// Hacking my way into the network channels requires a deprecated method and
			// reflection
			Field endPointsField = ServerConnection.class.getDeclaredField("g");
			endPointsField.setAccessible(true);
			@SuppressWarnings("deprecation")
			ChannelFuture oldEndPoint = ((List<ChannelFuture>) endPointsField.get(MinecraftServer.getServer().an()))
					.get(0);

			// This channel handler will register a channel handler for every connecting
			// client that will inspect any message before it reaches the Minecraft code.
			oldEndPoint.channel().pipeline().addFirst("multipurpose_server_inspector",
					new ChannelInboundHandlerAdapter() {

						@Override
						public void channelActive(ChannelHandlerContext ctx) throws Exception {
							System.out.println("outer ChannelActive");
							super.channelActive(ctx);
						}
						
						@Override
						public void exceptionCaught(ChannelHandlerContext ctx, Throwable t) {
							System.out.println("outer exception:");
							t.printStackTrace();
						}

						@Override
						public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
							System.out.println("outer ChannelReadComplete");
							super.channelReadComplete(ctx);
						}

						@Override
						public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
							System.out.println("outer ChannelRegistered");
							super.channelRegistered(ctx);
						}

						@Override
						public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
							System.out.println("outer ChannelUnregistered");
							super.channelUnregistered(ctx);
						}

						@Override
						public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
							System.out.println("outer ChannelWritabilityChanged");
							super.channelWritabilityChanged(ctx);
						}

						@Override
						public void channelInactive(ChannelHandlerContext ctx) throws Exception {
							System.out.println("outer channelInactive");
							super.channelInactive(ctx);
						}

						@Override
						public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
							Channel channel = (Channel) msg;
							ChannelPipeline pipeline = channel.pipeline();
							System.out.println("outer ChannelRead: msg is " + msg);

							// This channel handler will be registered for every connection client that will
							// inspect any message before it reaches the Minecraft code.
							pipeline.addFirst("multipurpose_handler_inspector", new ChannelInboundHandlerAdapter() {

								private ChannelState state = ChannelState.UNKNOWN;

								private ChannelListener listener;
								
								@Override
								public void exceptionCaught(ChannelHandlerContext ctx, Throwable t) {
									System.out.println("inner exception: " + System.identityHashCode(this));
									t.printStackTrace();
									// TODO also call the super methods here later
								}

								@Override
								public void channelActive(ChannelHandlerContext ctx) throws Exception {
									System.out.println("inner ChannelActive");
									//super.channelActive(ctx);
								}

								@Override
								public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
									if (state != ChannelState.VANILLA) {
										System.out.println("inner ChannelReadComplete");
									}
									if (state == ChannelState.VANILLA) {
										super.channelReadComplete(ctx);
									}
								}

								@Override
								public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
									System.out.println("inner ChannelRegistered");
									//super.channelRegistered(ctx);
								}

								@Override
								public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
									System.out.println("inner ChannelUnregistered");
									if (state == ChannelState.VANILLA) {
										super.channelUnregistered(ctx);
									}
								}

								@Override
								public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
									System.out.println("inner ChannelWritabilityChanged");
									if (state == ChannelState.VANILLA) {
										super.channelWritabilityChanged(ctx);
									}
								}

								@Override
								public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
									if (state != ChannelState.VANILLA) {
										System.out.println("inner ChannelRead " + System.identityHashCode(this));
									}
									try {
										if (state == ChannelState.CUSTOM) {
											listener.read(ctx, (ByteBuf) msg);
										} else if (state == ChannelState.UNKNOWN) {
											ByteBuf message = (ByteBuf) msg;
											byte firstByte = message.getByte(0);
											if (firstByte == FIRST_MINECRAFT_BYTE) {
												setVanillaState(ctx);
											} else if (firstByte == FIRST_WEB_BYTE) {
												byte[] data = new byte[message.readableBytes()];
												message.getBytes(0, data);
												WebHelper.Type type = WebHelper.determineConnectionType(data);
												System.out.println("type is " + type);
												if (type == WebHelper.Type.HTTP) {
													HTTPListener httpListener = new HTTPListener();
													httpListener.readHandshake(ctx, data);
													setCustomState(httpListener, ctx);
												} else if (type == WebHelper.Type.WEBSOCKET) {
													if (webSocketHandlerFactory != null) {
														WebSocketHandler websocketHandler = webSocketHandlerFactory
																.createHandler(ctx);
														if (websocketHandler != null) {
															WebSocketListener websocketListener = new WebSocketListener(
																	websocketHandler);
															webSocketListeners.add(websocketListener);
															websocketListener.readInitial(ctx, data);
															setCustomState(websocketListener, ctx);
														} else {
															state = ChannelState.DEAD;
															ctx.close();
														}
													} else {
														Bukkit.getLogger().warning(
																"A websocket handshake was received, but there is no websocket handler factory");
														setVanillaState(ctx);
													}
												} else {
													setVanillaState(ctx);
												}
											} else if (firstByte == FIRST_SECURE_WEB_BYTE) {
												// TODO implement the secure web protocols and find a way to read this
												// stuff
												// and find the difference
												System.out.println(
														"We are dealing with a secure websocket or https connection");
												byte[] data = new byte[message.readableBytes()];
												message.getBytes(0, data);
												System.out.println(Arrays.toString(data));
												setVanillaState(ctx);
											} else if (firstByte == FIRST_TCP_BYTE) {
												if (tcpSocketHandlerFactory != null) {
													TCPHandler tcpHandler = tcpSocketHandlerFactory.createHandler(ctx);
													if (tcpHandler != null) {
														TCPSocketListener tcpListener = new TCPSocketListener(
																tcpHandler);
														tcpSocketListeners.add(tcpListener);
														// The tcp listener doesn't need to know that the first byte is
														// FIRST_TCP_BYTE
														message.readByte();
														tcpListener.readInitial(ctx, message);
														setCustomState(tcpListener, ctx);
													} else {
														state = ChannelState.DEAD;
														ctx.close();
													}
												} else {
													Bukkit.getLogger().warning(
															"Someone attempted a TCP connection, but there is no TCP handler factory.");
													setVanillaState(ctx);
												}
											} else {
												System.out.println("Unknown connection type");
												setVanillaState(ctx);
											}
										}
									} catch (Exception ex) {
										ex.printStackTrace();
									}
									if (state == ChannelState.VANILLA) {
										super.channelRead(ctx, msg);
									}
								}

								@Override
								public void channelInactive(ChannelHandlerContext ctx) throws Exception {
									System.out.println("inner channelInactive");
									if (state == ChannelState.CUSTOM) {
										listener.onClose(ctx);
									} else if (state == ChannelState.VANILLA){
										super.channelInactive(ctx);
									}
								}
								
								private void setVanillaState(ChannelHandlerContext ctx) throws Exception {
									state = ChannelState.VANILLA;
									super.channelRegistered(ctx);
									super.channelActive(ctx);
								}
								
								private void setCustomState(ChannelListener listener, ChannelHandlerContext ctx) {
									this.listener = listener;
									state = ChannelState.CUSTOM;
									ctx.channel().pipeline().remove("timeout");
								}
							});
							super.channelRead(ctx, msg);
						}
					});
			Bukkit.getScheduler().scheduleSyncRepeatingTask(instance, () -> {
				Iterator<WebSocketListener> webSocketIterator = webSocketListeners.iterator();
				while (webSocketIterator.hasNext()) {
					if (!webSocketIterator.next().getImplementation().isOpen()) {
						webSocketIterator.remove();
					}
				}
				Iterator<TCPSocketListener> tcpIterator = tcpSocketListeners.iterator();
				while (tcpIterator.hasNext()) {
					if (tcpIterator.next().isClosed()) {
						tcpIterator.remove();
					}
				}
			}, 100, 100);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void onDisable() {
		for (WebSocketListener listener : webSocketListeners) {
			if (listener.getImplementation().isOpen()) {
				listener.getImplementation().close(CloseFrame.GOING_AWAY, "Server is stopping");
			}
		}
		for (TCPSocketListener listener : tcpSocketListeners) {
			listener.onServerStop();
		}
		webSocketListeners.clear();
		webSocketHandlerFactory = null;
		instance = null;
	}

	private static enum ChannelState {

		UNKNOWN, VANILLA, CUSTOM, DEAD;
	}
}