package com.expedia.bookings.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration;
import com.mobiata.android.Log;
import com.mobiata.android.util.AndroidUtils;

public class ServicesUtil {

	/**
	 * Constructs a user agent string to be used against Expedia requests. It is important to exclude the word "Android"
	 * otherwise mobile redirects occur when we don't want them. This is useful for all API requests contained here
	 * in ExpediaServices as well as certain requests through WebViewActivity in order to prevent the redirects.
	 *
	 * @param context
	 * @return
	 */
	public static String generateUserAgentString(Context context) {
		// Construct a proper user agent string
		String versionName;
		try {
			PackageManager pm = context.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
			versionName = pi.versionName;
		}
		catch (Exception e) {
			// PackageManager is traditionally wonky, need to accept all exceptions here.
			Log.w("Couldn't get package info in order to submit proper version #!", e);
			versionName = "1.0";
		}
		// Be careful not to use the word "Android" here
		// https://mingle/projects/e3_mobile_web/cards/676
		String userAgent = "ExpediaBookings/" + versionName + " (EHad; Mobiata)";
		return userAgent;
	}

	public static String generateClientId(Context context) {
		String clientName = ProductFlavorFeatureConfiguration.getInstance().getClientShortName();
		String deviceType = ExpediaBookingApp.useTabletInterface(context) ? "tablet" : "phone";
		return clientName + ".app.android." + deviceType + ":" + AndroidUtils.getAppVersion(context);
	}

	public static String generateSourceType() {
		return "mobileapp";
	}

	public static String generateLangId() {
		int langid = PointOfSale.getPointOfSale().getDualLanguageId();
		return langid == 0 ? "" : Integer.toString(langid);
	}

	public static String generateSiteId() {
		return Integer.toString(PointOfSale.getPointOfSale().getSiteId());
	}

}
