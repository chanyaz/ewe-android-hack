package com.expedia.bookings.utils;

import android.content.Context;

public class SupportUtils {

	public static String getAppSupportUrl(Context context) {
		return "http://www.mobiata.com/support/expediahotels-android";
	}

	public static String getBaggageFeeUrl(String origin, String destination) {
		String urlFormat = "http://www.expedia.com/Flights-BagFees?originapt=%s&destinationapt=%s";
		return String.format(urlFormat, origin, destination);
	}
}
