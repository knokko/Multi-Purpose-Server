package nl.knokko.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

public class ArrayHelper {
	
	public static byte[] readFile(File file) throws IOException {
		long fileLength = file.length();
		if (fileLength > Integer.MAX_VALUE) {
			throw new IOException("File " + file + " is too large!");
		}
		byte[] bytes = new byte[(int) fileLength];
		FileInputStream input = new FileInputStream(file);
		int totalRead = 0;
		while (totalRead < bytes.length) {
			int read = input.read(bytes, totalRead, bytes.length - totalRead);
			if (read == -1) {
				input.close();
				throw new IOException("End of file " + file + " was reached before all bytes were read");
			}
			totalRead += read;
		}
		input.close();
		return bytes;
	}
	
	public static boolean startsWith(byte[] array, byte[] prefix) {
		return startsWith(array, prefix, 0);
	}
	
	public static boolean startsWith(byte[] array, byte[] prefix, int fromIndex) {
		if (array.length < prefix.length) {
			return false;
		}
		for (int index = 0; index < prefix.length; index++) {
			if (array[index + fromIndex] != prefix[index]) {
				return false;
			}
		}
		return true;
	}

	public static byte[][] split(byte[] data, byte[] splitter) {
		if (splitter == null) {
			throw new NullPointerException("splitter");
		}
		if (splitter.length < 1) {
			throw new IllegalArgumentException("splitter can't be empty");
		}
		
		// Count the amount of parts to determine array size
		int count = 0;
		outerLoop:
		for (int index = 0; index + splitter.length <= data.length; index++) {
			for (int j = 0; j < splitter.length; j++) {
				if (data[index + j] != splitter[j]) {
					continue outerLoop;
				}
			}
			count++;
		}
		
		// Now create the result and fill it
		byte[][] result = new byte[count + 1][];
		int resultIndex = 0;
		int firstIndex = 0;
		outerLoop:
		for (int dataIndex = 0; dataIndex + splitter.length <= data.length; dataIndex++) {
			for (int j = 0; j < splitter.length; j++) {
				if (data[dataIndex + j] != splitter[j]) {
					continue outerLoop;
				}
			}
			result[resultIndex++] = Arrays.copyOfRange(data, firstIndex, dataIndex);
			firstIndex = dataIndex + splitter.length;
			dataIndex += splitter.length - 1;
		}
		
		// Don't forget the last part
		result[resultIndex] = Arrays.copyOfRange(data, firstIndex, data.length);
		return result;
	}
	
	public static int indexOf(byte[] bytes, byte value, int fromIndex) {
		for (; fromIndex < bytes.length; fromIndex++) {
			if (bytes[fromIndex] == value) {
				return fromIndex;
			}
		}
		return -1;
	}
}