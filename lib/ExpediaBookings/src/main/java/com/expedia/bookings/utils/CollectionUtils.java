package com.expedia.bookings.utils;

import java.util.Collection;

public class CollectionUtils {

	public static boolean isEmpty(Collection<?> collection) {
		if (collection == null || collection.isEmpty()) {
			return true;
		}
		return false;
	}

	public static boolean isNotEmpty(Collection<?> collection) {
		if (collection != null && !collection.isEmpty()) {
			return true;
		}
		return false;
	}
}
