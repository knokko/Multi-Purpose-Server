package nl.knokko.websocket.handler;

import java.nio.ByteBuffer;

import org.bukkit.Bukkit;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;

public class ChatWebSocketHandler implements WebSocketHandler {

	@Override
	public void onOpen(ClientHandshake handshake, WebSocket conn) {}

	@Override
	public void onMessage(String message, WebSocket conn) {
		Bukkit.broadcastMessage("Web: " + message);
	}

	@Override
	public void onMessage(ByteBuffer message, WebSocket conn) {}

	@Override
	public void onClose(String reason, int code, WebSocket conn) {}
}