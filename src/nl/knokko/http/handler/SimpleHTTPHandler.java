package nl.knokko.http.handler;

import java.io.File;
import java.io.IOException;

import nl.knokko.http.HTTPHandshake;
import nl.knokko.http.response.HTTPResponse;
import nl.knokko.http.response.SimpleHTTPResponse;
import nl.knokko.multiserver.plugin.ServerPlugin;
import nl.knokko.util.ArrayHelper;

public class SimpleHTTPHandler implements HTTPHandler {
	
	private final File folder;
	
	private File defaultFile;
	
	public SimpleHTTPHandler(File folder) {
		this.folder = folder;
	}
	
	private void findDefaultFile() {
		File[] files = folder.listFiles();
		if (files == null) throw new IllegalArgumentException(folder + " is no folder");
		if (files.length > 0) {
			defaultFile = files[0];
			for (int index = 1; index < files.length; index++) {
				if (compare(files[index], defaultFile) > 0) {
					defaultFile = files[index];
				}
			}
		}
	}
	
	private int compare(File a, File b) {
		return compare(a.getName(), b.getName());
	}
	
	private int compare(String a, String b) {
		if (a.equals("index.html"))
			return 1;
		if (b.equals("index.html"))
			return -1;
		if (a.equals("main.html"))
			return 1;
		if (b.equals("main.html"))
			return -1;
		boolean htmlA = a.endsWith(".html") || a.endsWith(".htm");
		boolean htmlB = b.endsWith(".html") || b.endsWith(".htm");
		if (htmlA && !htmlB)
			return 1;
		if (htmlB && !htmlA)
			return -1;
		return -a.compareTo(b);
	}

	@Override
	public HTTPResponse process(HTTPHandshake handshake) {
		String requestedFile = handshake.findRequestedFile();
		File target;
		if (requestedFile.isEmpty()) {
			if (defaultFile == null) {
				findDefaultFile();
			}
			if (defaultFile != null) {
				target = defaultFile;
			} else {
				// Strange scenario since this handler should be used for websites with at least 2 files
				return null;
			}
		} else {
			target = new File(folder + "/" + requestedFile);
		}
		if (target.isFile()) {
			try {
				return new SimpleHTTPResponse(ServerPlugin.getContentTypeForFile(target.getName()), ArrayHelper.readFile(target));
			} catch (IOException io) {
				// 404?
				io.printStackTrace();
				return null;
			}
		} else {
			System.out.println("Can't find file " + target);
			// TODO maybe a neat file not found 404?
			return null;
		}
	}
}