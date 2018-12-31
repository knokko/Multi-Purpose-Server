package nl.knokko.multiserver.helper;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.imageio.ImageIO;

import nl.knokko.util.ArrayHelper;

import static java.nio.charset.StandardCharsets.UTF_8;;

public class WebHelper {
	
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
	
	public static Type determineConnectionType(byte[] data) {
		String connectionType = null;
		int dataIndex = ArrayHelper.indexOf(data, LINE_TERMINATOR[1], 0) + 1;
		while (dataIndex != -1) {
			if (ArrayHelper.startsWith(data, CONNECTION_PREFIX, dataIndex)) {
				int offset = dataIndex + CONNECTION_PREFIX.length;
				int endIndex = ArrayHelper.indexOf(data, LINE_TERMINATOR[0], offset);
				connectionType = new String(data, offset, endIndex - offset, StandardCharsets.UTF_8);
				break;
			} else {
				dataIndex = ArrayHelper.indexOf(data, LINE_TERMINATOR[1], dataIndex) + 1;
			}
		}
		if (connectionType != null) {
			System.out.println("Connection type is " + connectionType);
			if (connectionType.contains("upgrade") || connectionType.contains("Upgrade")) {
				return Type.WEBSOCKET;
			}
			else if (connectionType.equalsIgnoreCase("keep-alive")) {
				return Type.HTTP;
			} else {
				return Type.INVALID;
			}
		} else {
			System.out.println("connection type is null");
			return Type.INVALID;
		}
	}
}