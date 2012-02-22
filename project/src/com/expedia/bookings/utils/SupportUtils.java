package com.expedia.bookings.utils;

import java.util.Map;

import android.content.Context;
import android.text.TextUtils;

import com.expedia.bookings.R;
import com.mobiata.android.util.ResourceUtils;

public class SupportUtils {

	private static Map<String, String> sAppSupportUrls;
	private static Map<String, String> sContactExpediaUrls;
	private static Map<String, String> sInfoSupportNumbers;
	private static Map<String, String> sConfSupportNumbers;

	public static String getAppSupportUrl(Context context) {
		if (sAppSupportUrls == null) {
			sAppSupportUrls = ResourceUtils.getStringMap(context, R.array.pos_app_support_url_map);
		}
		String url = sAppSupportUrls.get(LocaleUtils.getPointOfSale());
		if (TextUtils.isEmpty(url)) {
			return getContactExpediaUrl(context);
		}
		return url;
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

	public static String getConfSupportNumber(Context context) {
		if (sConfSupportNumbers == null) {
			sConfSupportNumbers = ResourceUtils.getStringMap(context, R.array.pos_conf_support_number_map);
		}
		return sConfSupportNumbers.get(LocaleUtils.getPointOfSale());
	}

	public static boolean hasConfSupportNumber(Context context) {
		return !TextUtils.isEmpty(getConfSupportNumber(context));
	}

	public static String determineContactText(Context context) {
		if (hasConfSupportNumber(context)) {
			return context.getString(R.string.contact_phone_template, getConfSupportNumber(context));
		}
		return context.getString(R.string.contact_phone_template, getInfoSupportNumber(context));
	}
}
