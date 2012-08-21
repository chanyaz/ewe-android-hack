package com.expedia.bookings.tracking;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.provider.Settings;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.utils.LocaleUtils;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.GAServiceManager;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.Item;
import com.google.analytics.tracking.android.Tracker;
import com.google.analytics.tracking.android.Transaction;
import com.mobiata.android.util.SettingUtils;
import com.omniture.AppMeasurement;

public class AdTracker {
	private static GoogleAnalytics mGoogleAnalytics;
	private static Tracker mGoogleTracker;

	private static AppMeasurement mAppMeasurement;
	private static String mAndroidId;
	private static String mMarketingDate;

	public static void initialize(Context context) {
		final Resources res = context.getResources();

		// Google
		mGoogleAnalytics = GoogleAnalytics.getInstance(context);
		mGoogleTracker = mGoogleAnalytics.getTracker(context.getString(R.string.google_analytics_ua));

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
		// Google Analytics
		mGoogleTracker.trackEvent("user_action", "first_launch", "launch", null);
		GAServiceManager.getInstance().dispatch();

		// Other
		Amobee.trackFirstLaunch();
		Somo.trackFirstLaunch();
	}

	public static void trackLaunch() {
		// Google Analytics
		mGoogleTracker.trackEvent("user_action", "launch", "launch", null);
		GAServiceManager.getInstance().dispatch();

		// Other
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
		// Google Analytics
		mGoogleTracker.trackEvent("user_action", "login", "login", null);
		GAServiceManager.getInstance().dispatch();

		// Other
		Amobee.trackLogin();
		Somo.trackLogin();
	}

	public static void trackBooking() {
		// Values
		final String propertyId = Db.getSelectedProperty().getPropertyId();
		final String propertyName = Db.getSelectedProperty().getName();
		final String currency = Db.getSelectedRate().getDisplayRate().getCurrency();
		final Integer duration = Db.getSearchParams().getStayDuration();
		final Double avgPrice = Db.getSelectedRate().getAverageRate().getAmount();
		final Double totalPrice = Db.getSelectedRate().getTotalAmountAfterTax().getAmount();
		final Double totalTax = Db.getSelectedRate().getTaxesAndFeesPerRoom().getAmount();
		final Integer daysRemaining = (int) ((Db.getSearchParams().getCheckInDate().getTime().getTime() - new Date()
				.getTime()) / (24 * 60 * 60 * 1000));

		// Google Analytics
		Transaction transaction = new Transaction.Builder(currency, (long) (totalPrice * 1000000))
				.setAffiliation("Expedia").setTotalTaxInMicros((long) (totalTax * 1000000)).setShippingCostInMicros(0)
				.build();

		transaction.addItem(new Item.Builder(propertyId, propertyName, (long) (avgPrice * 1000000),
				(long) (duration * 1000000)).setProductCategory("Hotel").build());

		EasyTracker.getTracker().trackTransaction(transaction);
		GAServiceManager.getInstance().dispatch();

		// Other
		Amobee.trackBooking(currency, totalPrice, duration, daysRemaining);
		Somo.trackBooking(currency, totalPrice, duration, daysRemaining);
	}
}