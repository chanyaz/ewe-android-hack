package com.expedia.bookings.utils;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class RetrofitUtils {

	public static boolean isNetworkError(Throwable e) {
		return e instanceof IOException;
	}

	public static RetrofitError getRetrofitError(Throwable e) {
		if (isNetworkError(e)) {
			return RetrofitError.NO_INTERNET;
		}
		else if (isTimeoutException(e)) {
			return RetrofitError.TIMEOUT;
		}
		return RetrofitError.UNKNOWN;
	}

	private static boolean isTimeoutException(Throwable e) {
		return e instanceof TimeoutException;
	}
}
