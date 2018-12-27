package nl.knokko.util;

import java.util.Arrays;

public class ArrayHelper {
	
	public static boolean startsWith(byte[] array, byte[] prefix) {
		if (array.length < prefix.length) {
			return false;
		}
		for (int index = 0; index < prefix.length; index++) {
			if (array[index] != prefix[index]) {
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
}