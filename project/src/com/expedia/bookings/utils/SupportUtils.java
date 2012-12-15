package com.expedia.bookings.utils;

import java.util.Map;

import android.content.Context;
import android.text.TextUtils;

import com.expedia.bookings.R;
import com.expedia.bookings.data.pos.PointOfSaleInfo;
import com.mobiata.android.util.ResourceUtils;

public class SupportUtils {

	private static Map<String, String> sContactExpediaUrls;
	private static Map<String, String> sInfoSupportNumbers;
	private static Map<String, String> sFlightSupportNumbers;

	public static String getAppSupportUrl(Context context) {
		return "http://www.mobiata.com/support/expediahotels-android";
	}

	public static String getContactExpediaUrl(Context context) {
		if (sContactExpediaUrls == null) {
			sContactExpediaUrls = ResourceUtils.getStringMap(context, R.array.pos_contact_expedia_url_map);
		}
		return sContactExpediaUrls.get(PointOfSaleInfo.getPointOfSaleInfo().getUrl());
	}

	public static String getWebsiteUrl() {
		String pos = PointOfSaleInfo.getPointOfSaleInfo().getUrl();
		String url = "http://www." + pos;
		return url;
	}

	public static String getInfoSupportNumber(Context context) {
		if (sInfoSupportNumbers == null) {
			sInfoSupportNumbers = ResourceUtils.getStringMap(context, R.array.pos_info_support_number_map);
		}
		return sInfoSupportNumbers.get(PointOfSaleInfo.getPointOfSaleInfo().getUrl());
	}

	public static boolean hasInfoSupportNumber(Context context) {
		return !TextUtils.isEmpty(getInfoSupportNumber(context));
	}

	public static String getFlightSupportNumber(Context context) {
		if (sFlightSupportNumbers == null) {
			sFlightSupportNumbers = ResourceUtils.getStringMap(context, R.array.pos_flight_support_number_map);
		}
		return sFlightSupportNumbers.get(PointOfSaleInfo.getPointOfSaleInfo().getUrl());
	}

	public static String getBaggageFeeUrl(String origin, String destination) {
		String urlFormat = "http://www.expedia.com/Flights-BagFees?originapt=%s&destinationapt=%s";
		return String.format(urlFormat, origin, destination);
	}
}
