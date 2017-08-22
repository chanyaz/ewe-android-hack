package com.expedia.bookings.utils;

import java.io.IOException;

public class RetrofitUtils {

	public static boolean isNetworkError(Throwable e) {
		return e instanceof IOException;
	}
}
