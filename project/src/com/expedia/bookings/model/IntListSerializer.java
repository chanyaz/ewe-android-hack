package com.expedia.bookings.model;

import java.util.ArrayList;
import java.util.List;

import android.text.TextUtils;

import com.activeandroid.TypeSerializer;

final public class IntListSerializer extends TypeSerializer {
	private final List<Integer> DESERIEALIZED_TYPE = new ArrayList<Integer>();

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

		Integer[] ints = ((List<Integer>)data).toArray(new Integer[] {});

		return TextUtils.join(";", ints);
	}

	@Override
	public List<Integer> deserialize(Object data) {
		if (data == null) {
			return null;
		}

		List<Integer> ints = new ArrayList<Integer>();
		for (String value : ((String) data).split(";")) {
			ints.add(Integer.parseInt(value));
		}
		
		return ints;
	}
}