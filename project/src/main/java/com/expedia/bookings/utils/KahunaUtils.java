package com.expedia.bookings.utils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import android.content.Context;
import android.os.Build;

import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.User;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration;
import com.expedia.bookings.notification.PushNotificationUtils;
import com.kahuna.sdk.KahunaAnalytics;
import com.mobiata.android.util.AndroidUtils;

/**
 * Created by mohsharma on 4/10/15.
 */
public class KahunaUtils {

	public static Map<String, String> mUserAttributes = new HashMap<String, String>();
	public static final String PROD_APP_KEY = "46adc9b9151f47d888be8bbf43d99af5";
	public static final String QA_APP_KEY = "f97ca9992ee14ba596695c6bd42892f3";
	public static Context mContext;

	public static void init(ExpediaBookingApp app) {
		mContext = app;
		if (BuildConfig.DEBUG) {
			KahunaAnalytics.onAppCreate(app, QA_APP_KEY, PushNotificationUtils.SENDER_ID);
			KahunaAnalytics.setDebugMode(true);
		}
		else {
			KahunaAnalytics.onAppCreate(app, PROD_APP_KEY, PushNotificationUtils.SENDER_ID);
		}

		String localeIdentifier = PointOfSale.getPointOfSale().getLocaleIdentifier();
		mUserAttributes.put("pos_locale", localeIdentifier);

		String deviceLocale = Locale.getDefault().toString();
		mUserAttributes.put("device_locale", deviceLocale);

		String countryCode = PointOfSale.getPointOfSale().getTwoLetterCountryCode();
		mUserAttributes.put("country_code", countryCode);

		String deviceType = ExpediaBookingApp.useTabletInterface(mContext) ? "Tablet" : "Phone";
		mUserAttributes.put("device_type", deviceType);

		String appVersion = AndroidUtils.getAppVersion(mContext);
		mUserAttributes.put("app_short_version", appVersion);

		String osVersion = Build.VERSION.RELEASE;
		mUserAttributes.put("os_version", osVersion);

		updateLoggedInStatus();

		//Set up the icons to show on push notifications
		KahunaAnalytics.setIconResourceId(R.drawable.ic_stat_expedia);

		KahunaAnalytics.enablePush();

		KahunaAnalytics.setUserAttributes(mUserAttributes);
	}

	public static void startKahunaTracking() {
		if (ProductFlavorFeatureConfiguration.getInstance().isKahunaEnabled()) {
			KahunaAnalytics.start();
		}
	}

	public static void stopKahunaTracking() {
		if (ProductFlavorFeatureConfiguration.getInstance().isKahunaEnabled()) {
			KahunaAnalytics.stop();
		}
	}

	public static void updatePOS() {
		if (ProductFlavorFeatureConfiguration.getInstance().isKahunaEnabled()) {
			PointOfSale pos = PointOfSale.getPointOfSale();
			mUserAttributes.put("pos_locale", pos.getLocaleIdentifier());
			mUserAttributes.put("country_code", pos.getTwoLetterCountryCode());

			String deviceLocale = Locale.getDefault().toString();
			mUserAttributes.put("device_locale", deviceLocale);

			KahunaAnalytics.setUserAttributes(mUserAttributes);
		}
	}

	public static void updateLoggedInStatus() {
		if (ProductFlavorFeatureConfiguration.getInstance().isKahunaEnabled()) {
			boolean isUserLoggedIn = User.isLoggedIn(mContext);
			mUserAttributes.put("logged_in", Boolean.toString(isUserLoggedIn));

			if (isUserLoggedIn) {

				if (Db.getUser() == null) {
					Db.loadUser(mContext);
				}

				if (Db.getUser().getPrimaryTraveler() != null) {
					KahunaAnalytics.setUsernameAndEmail(Db.getUser().getExpediaUserId(),
						Db.getUser().getPrimaryTraveler().getEmail());
					mUserAttributes.put("firstname", Db.getUser().getPrimaryTraveler().getFirstName());
					mUserAttributes.put("lastname", Db.getUser().getPrimaryTraveler().getLastName());
				}

				mUserAttributes.put("rewards_member", User.getLoggedInLoyaltyMembershipTier(mContext).toString());
				mUserAttributes.put("exp_user_id", Db.getUser().getExpediaUserId());
				mUserAttributes.put("tuid", Db.getUser().getTuidString());

			}
			KahunaAnalytics.setUserAttributes(mUserAttributes);
		}
	}

	public static void tracking(String eventName) {
		if (ProductFlavorFeatureConfiguration.getInstance().isKahunaEnabled()) {
			KahunaAnalytics.trackEvent(eventName);
			if (eventName.equalsIgnoreCase("Login")) {
				updateLoggedInStatus();
			}
			else if (eventName.equalsIgnoreCase("Logout")) {
				trackSignOutUser();
			}
		}
	}

	public static void trackSignOutUser() {
		if (ProductFlavorFeatureConfiguration.getInstance().isKahunaEnabled()) {
			mUserAttributes.put("logged_in", "false");
			KahunaAnalytics.setUserAttributes(mUserAttributes);
			KahunaAnalytics.logout();
		}
	}

}
