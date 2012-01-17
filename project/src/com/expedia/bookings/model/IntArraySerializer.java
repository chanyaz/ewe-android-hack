package com.expedia.bookings.model;

import java.util.Arrays;
import java.util.List;

import com.activeandroid.TypeSerializer;

final public class IntArraySerializer extends TypeSerializer {
	private final int[] DESERIEALIZED_TYPE = new int[0];

	@Override
	public Class<?> getDeserializedType() {
		return DESERIEALIZED_TYPE.getClass();
	}

	@Override
	public SerializedType getSerializedType() {
		return SerializedType.STRING;
	}

	@Override
	public String serialize(Object data) {
		if (data == null) {
			return null;
		}

		int[] intArray = (int[]) data;

		StringBuilder sb = new StringBuilder();
		for (int c : intArray) {
			sb.append(c);
			sb.append(";");
		}
		if (sb.length() > 1) {
			sb.setLength(sb.length() - 1);
		}
		String serialized = sb.toString();

		return serialized;
	}

	@Override
	public int[] deserialize(Object data) {
		if (data == null) {
			return null;
		}

		String serialized = (String) data;
		List<String> list = Arrays.asList(serialized.split(";"));

		int[] intArray = new int[list.size()];
		for (int i = 0; i < intArray.length; i++) {
			intArray[i] = Integer.parseInt(list.get(i));
		}

		return intArray;
	}
}