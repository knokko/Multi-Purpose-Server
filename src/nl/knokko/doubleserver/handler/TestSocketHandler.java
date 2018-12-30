package nl.knokko.doubleserver.handler;

import java.nio.ByteBuffer;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;

public class TestSocketHandler implements WebSocketHandler {

	@Override
	public void onOpen(ClientHandshake handshake, WebSocket conn) {
		System.out.println("onOpen");
	}

	@Override
	public void onMessage(String message, WebSocket conn) {
		System.out.println("received " + message);
	}

	@Override
	public void onMessage(ByteBuffer message, WebSocket conn) {
		System.out.println("received a binary frame");
	}

	@Override
	public void onClose(String reason, int code, WebSocket conn) {
		System.out.println("closed with because " + reason + " with code " + code);
	}
}