package com.expedia.bookings.utils;

import java.io.IOException;

public class RetrofitUtils {

	public static boolean isNetworkError(Throwable e) {
		if (e instanceof IOException) {
			return true;
		}
		return false;
	}
}
