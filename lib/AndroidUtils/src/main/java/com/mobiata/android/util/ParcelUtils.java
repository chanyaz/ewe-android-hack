package com.mobiata.android.util;

import android.os.Parcel;
import android.text.TextUtils;

public class ParcelUtils {

	public static void writeEnum(Parcel parcel, Enum<?> val) {
		if (val == null) {
			parcel.writeString(null);
		}
		else {
			parcel.writeString(val.name());
		}
	}

	public static <T extends Enum<T>> T readEnum(Parcel parcel, Class<T> enumType) {
		String name = parcel.readString();
		if (TextUtils.isEmpty(name)) {
			return null;
		}
		return Enum.valueOf(enumType, name);
	}

}
