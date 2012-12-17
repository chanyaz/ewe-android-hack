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
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.SearchParams;
import com.expedia.bookings.data.pos.PointOfSale;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Item;
import com.google.analytics.tracking.android.Transaction;
import com.mobiata.android.util.SettingUtils;
import com.omniture.AppMeasurement;

public class AdTracker {
	private static AppMeasurement mAppMeasurement;
	private static String mAndroidId;
	private static String mMarketingDate;

	public static void initialize(Context context) {
		final Resources res = context.getResources();

		// Google
		EasyTracker.getInstance().setContext(context);

		// Amobee
		final List<String> amobeePos = Arrays.asList(res.getStringArray(R.array.valid_amobee_points_of_sale));
		final String appId = context.getString(R.string.amobee_app_id);
		Amobee.initialize(context, appId, amobeePos.contains(PointOfSale.getPointOfSaleInfo().getUrl()));

		// Somo
		final List<String> somoPos = Arrays.asList(res.getStringArray(R.array.valid_somo_points_of_sale));
		final int userId = res.getInteger(R.integer.somo_user_id);
		final int applicationId = res.getInteger(R.integer.somo_application_id);
		Somo.initialize(context, userId, applicationId, somoPos.contains(PointOfSale.getPointOfSaleInfo().getUrl()));

		// Omniture
		mAppMeasurement = new AppMeasurement((Application) context.getApplicationContext());
		mAndroidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
		mMarketingDate = SettingUtils.get(context, context.getString(R.string.preference_amobee_marketing_date), "");
	}

	public static void trackFirstLaunch() {
		// Google Analytics
		EasyTracker.getTracker().trackEvent("user_action", "first_launch", "launch", null);

		// Other
		Amobee.trackFirstLaunch();
		Somo.trackFirstLaunch();
	}

	public static void trackLaunch() {
		// Google Analytics
		EasyTracker.getTracker().trackEvent("user_action", "launch", "launch", null);

		// Other
		Amobee.trackLaunch();
		//Somo.trackLaunch();

		// Omniture
		mAppMeasurement.visitorID = mAndroidId;
		mAppMeasurement.eVar7 = mAndroidId;
		mAppMeasurement.eVar10 = mMarketingDate;
		mAppMeasurement.eVar27 = "App Launch";

		mAppMeasurement.track();
	}

	public static void trackLogin() {
		// Google Analytics
		EasyTracker.getTracker().trackEvent("user_action", "login", "login", null);

		// Other
		Amobee.trackLogin();
		Somo.trackLogin();
	}

	public static void trackBooking() {
		// Values
		final SearchParams searchParams = Db.getSearchParams().copy();
		final Property property = Db.getSelectedProperty();
		final Rate rate = Db.getSelectedRate();

		final String propertyId = property.getPropertyId();
		final String propertyName = property.getName();
		final String currency = rate.getDisplayRate().getCurrency();
		final Integer duration = searchParams.getStayDuration();
		final Double avgPrice = rate.getAverageRate().getAmount().doubleValue();
		final Double totalPrice = rate.getTotalAmountAfterTax().getAmount().doubleValue();
		final Double totalTax = rate.getTaxesAndFeesPerRoom() != null ? rate.getTaxesAndFeesPerRoom().getAmount()
				.doubleValue() : 0;
		final Integer daysRemaining = (int) ((searchParams.getCheckInDate().getTime().getTime() - new Date().getTime()) / (24 * 60 * 60 * 1000));

		// Google Analytics
		Transaction transaction = new Transaction.Builder(currency, (long) (totalPrice * 1000000))
				.setAffiliation("Expedia").setTotalTaxInMicros((long) (totalTax * 1000000)).setShippingCostInMicros(0)
				.build();

		transaction.addItem(new Item.Builder(propertyId, propertyName, (long) (avgPrice * 1000000),
				(long) (duration * 1000000)).setProductCategory("Hotel").build());

		EasyTracker.getTracker().trackTransaction(transaction);

		// Other
		Amobee.trackBooking(currency, totalPrice, duration, daysRemaining);
		Somo.trackBooking(currency, totalPrice, duration, daysRemaining);
	}
}