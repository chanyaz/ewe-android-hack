package com.expedia.bookings.utils;

import android.app.Application;

import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration;
import com.expedia.bookings.tracking.FacebookEvents;

import static com.expedia.bookings.activity.ExpediaBookingApp.isAutomation;

public class TrackingUtils {

	public static void initializeTracking(Application app) {
		if (!isAutomation()) {
			FacebookEvents.init(app);

			if (ProductFlavorFeatureConfiguration.getInstance().isTuneEnabled()) {
				TuneUtils.init(app);
			}
		}

	}
}
