package nl.knokko.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * This is a utility class that has methods to deal with byte arrays.
 * @author knokko
 *
 */
public class ArrayHelper {
	
	/**
	 * Increasing a byte that represents an upper case letter turns it into a lower case letter.
	 */
	private static final byte UPPER_LOWER_ADDER = 'a' - 'A';
	
	/**
	 * Converts all lower case bytes in original to their upper case letters and puts the result in dest.
	 * After a call to this method, dest will be a copy of original, but all lower case letters will be
	 * converted to their upper case letter.
	 * @param original The original byte array
	 * @param dest The destination byte array
	 */
	public static void toUpperCase(byte[] original, byte[] dest) {
		for (int index = 0; index < original.length; index++) {
			dest[index] = original[index];
			if (original[index] >= 'a' && original[index] <= 'z') {
				dest[index] -= UPPER_LOWER_ADDER;
			}
		}
	}
	
	/**
	 * Converts all upper case bytes in original to their lower case bytes and puts the result in dest.
	 * After a call to this method, dest will be a copy of original, but all upper case bytes will be
	 * converted to their upper case bytes.
	 * @param original The original byte array
	 * @param dest The destination byte array
	 */
	public static void toLowerCase(byte[] original, byte[] dest) {
		for (int index = 0; index < original.length; index++) {
			dest[index] = original[index];
			if (original[index] >= 'A' && original[index] <= 'Z') {
				dest[index] += UPPER_LOWER_ADDER;
			}
		}
	}
	
	/**
	 * Reads the specified file and puts all its content into a newly created byte array.
	 * @param file The file to read
	 * @return The contents of the file in a byte array
	 * @throws IOException If the file is too long or if an IO error occurs
	 */
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
		outerLoop: for (int index = 0; index + splitter.length <= data.length; index++) {
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
		outerLoop: for (int dataIndex = 0; dataIndex + splitter.length <= data.length; dataIndex++) {
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
		for (int index = fromIndex; index < bytes.length; index++) {
			if (bytes[index] == value) {
				return index;
			}
		}
		return -1;
	}

	public static int indexOf(byte[] bytes, byte[] prefix, int fromIndex) {
		outerLoop: for (int index = fromIndex; index + prefix.length <= bytes.length; index++) {
			for (int index2 = 0; index2 < prefix.length; index2++) {
				if (bytes[index + index2] != prefix[index2]) {
					continue outerLoop;
				}
			}
			return index;
		}
		return -1;
	}
	
	public static int indexOf(byte[] bytes, byte[] prefix1, byte[] prefix2, int fromIndex) {
		outerLoop: for (int index = fromIndex; index + prefix1.length <= bytes.length; index++) {
			for (int index2 = 0; index2 < prefix1.length; index2++) {
				if (bytes[index + index2] != prefix1[index2] && bytes[index + index2] != prefix2[index2]) {
					continue outerLoop;
				}
			}
			return index;
		}
		return -1;
	}
}