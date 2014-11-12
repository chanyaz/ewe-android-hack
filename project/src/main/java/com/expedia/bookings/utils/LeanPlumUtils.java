package com.expedia.bookings.utils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.notification.PushNotificationUtils;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.leanplum.Leanplum;
import com.leanplum.LeanplumPushService;
import com.mobiata.android.util.AndroidUtils;


public class LeanPlumUtils {
	public static Map<String, Object> mUserAtrributes = new HashMap<String, Object>();
	public static Context mContext;
	public static final String CAMPAIGN_TEXT_KEY = "campaignText";
	public static final String DEFAULT_CAMPAIGN_TEXT = "leanplum.notification";

	public static void init(Context context) {
		mContext = context;
		if (!AndroidUtils.isRelease(mContext)) {
			Leanplum.setAppIdForDevelopmentMode(mContext.getString(R.string.lean_plum_sdk_app_id),
				mContext.getString(R.string.lean_plum_sdk_dev_key));
		}
		else {
			Leanplum.setAppIdForProductionMode(mContext.getString(R.string.lean_plum_sdk_app_id),
				mContext.getString(R.string.lean_plum_sdk_prod_key));
		}
		String localeIdentifier = PointOfSale.getPointOfSale().getLocaleIdentifier();
		mUserAtrributes.put("PosLocale", localeIdentifier);

		String deviceLocale = Locale.getDefault().toString();
		mUserAtrributes.put("DeviceLocale", deviceLocale);

		String countryCode = PointOfSale.getPointOfSale().getTwoLetterCountryCode();
		mUserAtrributes.put("CountryCode", countryCode);

		String deviceType = ExpediaBookingApp.useTabletInterface(mContext) ? "Tablet" : "Phone";
		mUserAtrributes.put("DeviceType", deviceType);

		LeanplumPushService.setGcmSenderId(PushNotificationUtils.SENDER_ID);
		LeanplumPushService.setCustomizer(new LeanplumPushService.NotificationCustomizer() {
			@Override
			public void customize(NotificationCompat.Builder builder, Bundle bundle) {
				//	trackNotificationClick
				String campaignText = bundle.getString(CAMPAIGN_TEXT_KEY, DEFAULT_CAMPAIGN_TEXT);
				OmnitureTracking.trackLeanPlumNotification(mContext, campaignText);
			}
		});
		Leanplum.start(mContext, mUserAtrributes);

	}


	public static void updatePOS() {
		if (ExpediaBookingApp.IS_EXPEDIA) {
			PointOfSale pos = PointOfSale.getPointOfSale();
			mUserAtrributes.put("PosLocale", pos.getLocaleIdentifier());
			mUserAtrributes.put("CountryCode", pos.getTwoLetterCountryCode());

			String deviceLocale = Locale.getDefault().toString();
			mUserAtrributes.put("DeviceLocale", deviceLocale);

			Leanplum.setUserAttributes(mUserAtrributes);
		}
	}


}

