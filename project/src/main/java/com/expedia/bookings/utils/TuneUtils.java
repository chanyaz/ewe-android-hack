package com.expedia.bookings.utils;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;

import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.User;
import com.mobileapptracker.MATEvent;
import com.mobileapptracker.MobileAppTracker;

public class TuneUtils {

	public static MobileAppTracker mobileAppTracker = null;
	private static boolean initialized = false;
	public static Context context;

	public static void init(ExpediaBookingApp app) {
		initialized = true;
		context = app.getApplicationContext();

		String advertiserID = app.getString(R.string.tune_sdk_app_advertiser_id);
		String conversionKey = app.getString(R.string.tune_sdk_app_conversion_key);

		mobileAppTracker = MobileAppTracker.init(app, advertiserID, conversionKey);

		mobileAppTracker.setDebugMode(BuildConfig.DEBUG);
		mobileAppTracker.setAllowDuplicates(BuildConfig.DEBUG);

		MATEvent launchEvent = new MATEvent("launch");
		boolean isUserLoggedIn = User.isLoggedIn(context);
		if (isUserLoggedIn) {
			List<String> list = getTuidandMembershipTier();
			if (!list.isEmpty()) {
				launchEvent.withAttribute1(list.get(0));
				launchEvent.withAttribute3(list.get(1));
			}

		}
		launchEvent.withAttribute2(Boolean.toString(isUserLoggedIn));
		trackEvent(launchEvent);

	}

	public static void startTune(Activity activity) {
		if (initialized) {
			// Get source of open for app re-engagement
			mobileAppTracker.setReferralSources(activity);
			// MAT will not function unless the measureSession call is included
			mobileAppTracker.measureSession();

		}
	}

	private static void trackEvent(MATEvent eventName) {
		mobileAppTracker.measureEvent(eventName);

	}

	public static void trackLogin() {
		if (initialized) {
			MATEvent loginEvent = new MATEvent("login");
			List<String> list = getTuidandMembershipTier();
			if (!list.isEmpty()) {
				loginEvent.withAttribute1(list.get(0));
				loginEvent.withAttribute2(list.get(1));
			}
			trackEvent(loginEvent);
		}
	}

	private static List<String> getTuidandMembershipTier() {
		List<String> list = new ArrayList<String>();
		boolean isUserLoggedIn = User.isLoggedIn(context);
		if (isUserLoggedIn) {
			if (Db.getUser() == null) {
				Db.loadUser(context);
			}
			list.add(Db.getUser().getTuidString());
			list.add(User.getLoggedInLoyaltyMembershipTier(context).toString());
		}
		return list;
	}

}
