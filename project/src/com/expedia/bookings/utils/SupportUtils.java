package com.expedia.bookings.utils;

import java.util.Map;

import android.content.Context;
import android.text.TextUtils;

import com.expedia.bookings.R;
import com.mobiata.android.util.ResourceUtils;

public class SupportUtils {

	private static Map<String, String> sContactExpediaUrls;
	private static Map<String, String> sInfoSupportNumbers;

	public static String getAppSupportUrl(Context context) {
		return "http://www.mobiata.com/support/expediahotels-android";
	}

	public static String getContactExpediaUrl(Context context) {
		if (sContactExpediaUrls == null) {
			sContactExpediaUrls = ResourceUtils.getStringMap(context, R.array.pos_contact_expedia_url_map);
		}
		return sContactExpediaUrls.get(LocaleUtils.getPointOfSale());
	}

	public static String getWebsiteUrl() {
		String pos = LocaleUtils.getPointOfSale();
		String url = "http://www." + pos;
		return url;
	}

	public static String getInfoSupportNumber(Context context) {
		if (sInfoSupportNumbers == null) {
			sInfoSupportNumbers = ResourceUtils.getStringMap(context, R.array.pos_info_support_number_map);
		}
		return sInfoSupportNumbers.get(LocaleUtils.getPointOfSale());
	}

	public static boolean hasInfoSupportNumber(Context context) {
		return !TextUtils.isEmpty(getInfoSupportNumber(context));
	}

	public static String getFlightSupportNumber(Context context) {
		// NOTE: While this currently returns a static number, I anticipate
		// that it will eventually return a much more dynamic result someday
		// in the future (much like the hotels side of the app).  Thus
		// this preparatory function.

		// TODO: Insert real # once we get it.
		return "867-5309";
	}
}
