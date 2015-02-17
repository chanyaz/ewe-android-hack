package com.expedia.bookings.utils;

import android.content.Context;

import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration;
import com.mobiata.android.util.AndroidUtils;

public class ServicesUtil {

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
