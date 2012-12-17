package com.expedia.bookings.utils;

import android.content.Context;

import com.expedia.bookings.data.pos.PointOfSaleInfo;

public class SupportUtils {

	public static String getAppSupportUrl(Context context) {
		return "http://www.mobiata.com/support/expediahotels-android";
	}

	public static String getWebsiteUrl() {
		String pos = PointOfSaleInfo.getPointOfSaleInfo().getUrl();
		String url = "http://www." + pos;
		return url;
	}

	public static String getBaggageFeeUrl(String origin, String destination) {
		String urlFormat = "http://www.expedia.com/Flights-BagFees?originapt=%s&destinationapt=%s";
		return String.format(urlFormat, origin, destination);
	}
}
