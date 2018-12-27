package nl.knokko.doubleserver.handler;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import nl.knokko.util.ArrayHelper;

import static java.nio.charset.StandardCharsets.UTF_8;;

public class WebHandler {
	
	public static final byte[] FAVICON;
	public static final byte[] PAGE = "<html> <body> <p>My first StackOverflow answer </p> </body> </html>".getBytes(UTF_8);
	
	public static final byte[] CONNECTION_PREFIX = "Connection: ".getBytes(UTF_8);
	public static final byte[] LINE_TERMINATOR = new byte[] {13, 10};
	
	public static final byte[] HTTP_LINE = "HTTP/1.1 200 OK".getBytes(UTF_8);
	public static final byte[] CONTENT_TYPE_IMAGE = "Content-Type: image/png".getBytes(UTF_8);
	public static final byte[] CONTENT_TYPE_HTML = "Content-Type: text/html".getBytes(UTF_8);
	
	static {
		try {
			BufferedImage icon = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			ImageIO.write(icon, "PNG", output);
			FAVICON = output.toByteArray();
		} catch (IOException io) {
			throw new RuntimeException(io);
		}
	}
	
	public static enum Type {
		
		HTTP,
		WEBSOCKET,
		INVALID;
	}
	
	public static Type determineConnectionType(byte[] request) {
		byte[][] splitted = ArrayHelper.split(request, LINE_TERMINATOR);
		String connectionType = null;
		for (byte[] line : splitted) {
			if (ArrayHelper.startsWith(line, CONNECTION_PREFIX)) {
				connectionType = new String(line, CONNECTION_PREFIX.length, line.length - CONNECTION_PREFIX.length, UTF_8);
			}
		}
		if (connectionType != null) {
			if (connectionType.equalsIgnoreCase("keep-alive")) {
				return Type.HTTP;
			} else if (connectionType.equalsIgnoreCase("upgrade")) {
				return Type.WEBSOCKET;
			} else {
				return Type.INVALID;
			}
		} else {
			return Type.INVALID;
		}
	}
}