package nl.knokko.websocket.handler;

import java.nio.ByteBuffer;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;

public interface WebSocketHandler {
	
	void onOpen(ClientHandshake handshake, WebSocket conn);
	
	void onMessage(String message, WebSocket conn);
	
	void onMessage(ByteBuffer message, WebSocket conn);
	
	void onClose(String reason, int code, WebSocket conn);
}