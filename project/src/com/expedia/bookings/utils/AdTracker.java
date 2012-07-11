package com.expedia.bookings.utils;

import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;

import com.expedia.bookings.R;

public class AdTracker {
	public static void initialize(Context context) {
		final Resources res = context.getResources();

		// Amobee
		final List<String> amobeePos = Arrays.asList(res.getStringArray(R.array.valid_amobee_points_of_sale));
		final String appId = context.getString(R.string.amobee_app_id);
		Amobee.initialize(context, appId, amobeePos.contains(LocaleUtils.getPointOfSale(context)));

		// Somo
		final List<String> somoPos = Arrays.asList(res.getStringArray(R.array.valid_somo_points_of_sale));
		final int userId = res.getInteger(R.integer.somo_user_id);
		final int applicationId = res.getInteger(R.integer.somo_application_id);
		Somo.initialize(context, userId, applicationId, somoPos.contains(LocaleUtils.getPointOfSale(context)));

		// TODO: Initialize omniture
	}

	public static void trackFirstLaunch() {
		Amobee.trackFirstLaunch();
		Somo.trackFirstLaunch();
	}

	public static void trackLaunch() {
		Amobee.trackLaunch();
		Somo.trackLaunch();
	}

	public static void trackLogin() {
		Amobee.trackLogin();
		Somo.trackLogin();
	}

	public static void trackBooking(String currency, double totalPrice, int duration, int daysRemaining) {
		Amobee.trackBooking(currency, totalPrice, duration, daysRemaining);
		Somo.trackBooking(currency, totalPrice, duration, daysRemaining);
	}
}