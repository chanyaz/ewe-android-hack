package com.expedia.bookings.utils;

import android.app.Application;

import com.expedia.bookings.R;
import com.expedia.bookings.data.user.UserStateManager;
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration;
import com.expedia.bookings.tracking.FacebookEvents;
import com.tune.Tune;

import static com.expedia.bookings.activity.ExpediaBookingApp.isAutomation;

public class TrackingUtils {

	public static void initializeTracking(Application app) {
		if (!isAutomation()) {
			FacebookEvents.init(app);

			if (ProductFlavorFeatureConfiguration.getInstance().isTuneEnabled()) {
				String advertiserID = app.getString(R.string.tune_sdk_app_advertiser_id);
				String conversionKey = app.getString(R.string.tune_sdk_app_conversion_key);

				Tune tune = Tune.init(app, advertiserID, conversionKey);
				UserStateManager userStateManager = Ui.getApplication(app.getApplicationContext()).appComponent().userStateManager();
				Boolean shouldSetExistingUser = ProductFlavorFeatureConfiguration.getInstance().shouldSetExistingUserForTune();

				TuneTrackingProvider provider = new TuneTrackingProviderImpl(tune, app, userStateManager, shouldSetExistingUser);

				TuneUtils.init(provider);
			}
		}

	}
}
