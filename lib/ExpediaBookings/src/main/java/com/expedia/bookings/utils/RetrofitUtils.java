package com.expedia.bookings.utils;

import retrofit.RetrofitError;

public class RetrofitUtils {

	public static boolean isNetworkError(Throwable e) {
		if (e instanceof RetrofitError) {
			return ((RetrofitError) e).getKind().equals(RetrofitError.Kind.NETWORK);
		}
		return false;
	}
}
