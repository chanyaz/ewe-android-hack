package com.expedia.bookings.tracking;

import java.util.Arrays;
import java.util.List;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.provider.Settings;

import com.expedia.bookings.R;
import com.expedia.bookings.utils.LocaleUtils;
import com.mobiata.android.util.SettingUtils;
import com.omniture.AppMeasurement;

public class AdTracker {
	private static AppMeasurement mAppMeasurement;
	private static String mAndroidId;
	private static String mMarketingDate;

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

		// Omniture
		mAppMeasurement = new AppMeasurement((Application) context.getApplicationContext());
		mAndroidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
		mMarketingDate = SettingUtils.get(context, context.getString(R.string.preference_amobee_marketing_date), "");
	}

	public static void trackFirstLaunch() {
		Amobee.trackFirstLaunch();
		Somo.trackFirstLaunch();
	}

	public static void trackLaunch() {
		Amobee.trackLaunch();
		Somo.trackLaunch();

		// Omniture
		mAppMeasurement.visitorID = mAndroidId;
		mAppMeasurement.eVar7 = mAndroidId;
		mAppMeasurement.eVar10 = mMarketingDate;
		mAppMeasurement.eVar27 = "App Launch";

		mAppMeasurement.track();
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