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
import nl.knokko.multiserver.listener.TCPListener;
import nl.knokko.multiserver.listener.WebSocketListener;
import nl.knokko.tcp.handler.TCPHandler;
import nl.knokko.tcp.handler.factory.TCPHandlerFactory;
import nl.knokko.websocket.handler.WebSocketHandler;
import nl.knokko.websocket.handler.factory.WebSocketHandlerFactory;

public class ServerPlugin extends JavaPlugin implements Listener {
	
	public static final byte FIRST_MINECRAFT_BYTE = 16;
	public static final byte FIRST_TCP_BYTE = 47;
	public static final byte FIRST_WEB_BYTE = 71;
	public static final byte FIRST_SECURE_WEB_BYTE = 22;
	
	private static ServerPlugin instance;
	
	public static ServerPlugin getInstance() {
		return instance;
	}
	
	public static WebSocketHandlerFactory getWebSocketHandlerFactory() {
		return instance.socketHandlerFactory;
	}
	
	public static TCPHandlerFactory getTCPHandlerFactory() {
		return instance.tcpHandlerFactory;
	}
	
	public static HTTPHandler getHTTPHandler() {
		return instance.httpHandler;
	}
	
	public static Collection<WebSocketListener> getWebsocketListeners(){
		return instance.websocketListeners;
	}
	
	public static Collection<TCPListener> getTCPListeners(){
		return instance.tcpListeners;
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
	
	private Map<String,String> contentTypes;
	
	private HTTPHandler httpHandler;
	private WebSocketHandlerFactory socketHandlerFactory;
	private TCPHandlerFactory tcpHandlerFactory;
	
	private Collection<WebSocketListener> websocketListeners;
	private Collection<TCPListener> tcpListeners;
	
	public void readConfig() {
		FileConfiguration config = getConfig();
		readWebsiteConfig(config);
		readTCPConfig(config);
	}
	
	public void readTCPConfig(FileConfiguration config) {
		String tcpHandlerName = config.getString("tcp-handler-factory");
		if (tcpHandlerName != null) {
			tcpHandlerFactory = null;
		} else {
			try {
				tcpHandlerFactory = (TCPHandlerFactory) Class.forName(tcpHandlerName).newInstance();
			} catch (Exception ex) {
				Bukkit.getLogger().log(Level.SEVERE, "Failed to load the TCP handler factory " + tcpHandlerName, ex);
				tcpHandlerFactory = null;
			}
		}
	}
	
	public void readWebsiteConfig(FileConfiguration config) {
		ConfigurationSection contentTypesSection = config.getConfigurationSection("content-types");
		if (contentTypesSection == null) {
			contentTypes = DefaultContentTypes.get();
			contentTypesSection = config.createSection("content-types");
			Set<Entry<String,String>> entrySet = contentTypes.entrySet();
			for (Entry<String,String> entry : entrySet) {
				contentTypesSection.set(entry.getKey(), entry.getValue());
			}
		} else {
			contentTypes = new HashMap<String,String>();
			Set<Entry<String,Object>> givenContentTypes = contentTypesSection.getValues(false).entrySet();
			for (Entry<String,Object> entry : givenContentTypes) {
				if (entry.getValue() instanceof String) {
					contentTypes.put(entry.getKey(), (String) entry.getValue());
				} else {
					Bukkit.getLogger().warning("The content type for extension " + entry.getKey() + " should be a text");
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
						Bukkit.getLogger().info("No files were found in the website folder, so the server will not act as website.");
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
		String wsHandlerName = config.getString("websocket-handler-factory");
		if (wsHandlerName == null) {
			socketHandlerFactory = null;
		} else {
			try {
				socketHandlerFactory = (WebSocketHandlerFactory) Class.forName(wsHandlerName).newInstance();
			} catch (Exception ex) {
				Bukkit.getLogger().log(Level.SEVERE, "Failed to load websocket handler " + wsHandlerName, ex);
				socketHandlerFactory = null;
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
			websocketListeners = new LinkedList<WebSocketListener>();
			tcpListeners = new LinkedList<TCPListener>();
			
			// Hacking my way into the network channels requires a deprecated method and reflection
			Field endPointsField = ServerConnection.class.getDeclaredField("g");
			endPointsField.setAccessible(true);
			@SuppressWarnings("deprecation")
			ChannelFuture oldEndPoint = ((List<ChannelFuture>)endPointsField.get(MinecraftServer.getServer().an())).get(0);

			// This channel handler will register a channel handler for every connecting
			// client that will inspect any message before it reaches the Minecraft code.
			oldEndPoint.channel().pipeline().addFirst("multipurpose_server_inspector", new ChannelInboundHandlerAdapter() {

				@Override
				public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
					super.channelRead(ctx, msg);
					Channel channel = (Channel) msg;
					ChannelPipeline pipeline = channel.pipeline();

					// This channel handler will be registered for every connection client that will
					// inspect any message before it reaches the Minecraft code.
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

										if (firstByte == FIRST_MINECRAFT_BYTE) {
											deactivated = true;
											super.channelRead(ctx, msg);
										}

										else if (firstByte == FIRST_WEB_BYTE) {
											byte[] data = new byte[message.readableBytes()];
											message.getBytes(0, data);
											WebHelper.Type type = WebHelper.determineConnectionType(data);
											System.out.println("type is " + type);
											if (type == WebHelper.Type.HTTP) {
												HTTPListener httpListener = new HTTPListener();
												httpListener.readHandshake(ctx, data);
												listener = httpListener;
											} else if (type == WebHelper.Type.WEBSOCKET) {
												if (socketHandlerFactory != null) {
													WebSocketHandler websocketHandler = socketHandlerFactory.createHandler(ctx);
													if (websocketHandler != null) {
														WebSocketListener websocketListener = new WebSocketListener(websocketHandler);
														websocketListeners.add(websocketListener);
														websocketListener.readInitial(ctx, data);
														listener = websocketListener;
													} else {
														deactivated = true;
														ctx.close();
													}
												} else {
													Bukkit.getLogger().warning("A websocket handshake was received, but there is no websocket handler factory");
													deactivated = true;
													super.channelRead(ctx, msg);
												}
											} else {
												deactivated = true;
												super.channelRead(ctx, msg);
											}
										}

										// All secure web connections start with the byte 22
										else if (firstByte == FIRST_SECURE_WEB_BYTE) {
											// TODO implement the secure web protocols and find a way to read this stuff
											// and find the difference
											System.out.println("We are dealing with a secure websocket or https connection");
											byte[] data = new byte[message.readableBytes()];
											message.getBytes(0, data);
											System.out.println(Arrays.toString(data));
										}

										// For non-minecraft tcp connections
										else if (firstByte == FIRST_TCP_BYTE) {
											if (tcpHandlerFactory != null) {
												TCPHandler tcpHandler = tcpHandlerFactory.createHandler(ctx);
												if (tcpHandler != null) {
													TCPListener tcpListener = new TCPListener(tcpHandler);
													tcpListeners.add(tcpListener);
													// The tcp listener doesn't need to know that the first byte is FIRST_TCP_BYTE
													message.readByte();
													tcpListener.readInitial(ctx, message);
													listener = tcpListener;
												} else {
													deactivated = true;
													ctx.close();
												}
											} else {
												Bukkit.getLogger().warning("Someone attempted a TCP connection, but no TCP handler factory.");
												deactivated = true;
												super.channelRead(ctx, msg);
											}
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
						
						@Override
						public void channelInactive(ChannelHandlerContext ctx) throws Exception {
							if (!deactivated && listener != null) {
								listener.onClose(ctx);
							} else {
								super.channelInactive(ctx);
							}
						}
					});
				}
			});
			Bukkit.getScheduler().scheduleSyncRepeatingTask(instance, () -> {
				Iterator<WebSocketListener> webSocketIterator = websocketListeners.iterator();
				while (webSocketIterator.hasNext()) {
					WebSocketListener next = webSocketIterator.next();
					if (next.getImplementation().isOpen()) {
						next.getImplementation().sendPing();
					} else {
						webSocketIterator.remove();
					}
				}
				Iterator<TCPListener> tcpIterator = tcpListeners.iterator();
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
		for (WebSocketListener listener : websocketListeners) {
			if (listener.getImplementation().isOpen()) {
				listener.getImplementation().close(CloseFrame.GOING_AWAY, "Server is stopping");
			}
		}
		for (TCPListener listener : tcpListeners) {
			listener.onServerStop();
		}
		websocketListeners.clear();
		socketHandlerFactory = null;
		instance = null;
	}
}