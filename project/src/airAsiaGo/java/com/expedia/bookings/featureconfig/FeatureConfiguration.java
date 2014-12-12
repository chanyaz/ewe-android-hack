package com.expedia.bookings.featureconfig;

import android.content.Context;

import com.expedia.bookings.R;

public class FeatureConfiguration implements IProductFlavorFeatureConfiguration {
	public String getServerEndpointsConfigurationPath() {
		return "ExpediaSharedData/AirAsiaGoServerURLs.json";
	}

	public String getAppNameForMobiataPushNameHeader() {
		return "AAGBookings";
	}

	public String getAppSupportUrl(Context context) {
		return context.getString(R.string.app_support_url_aag);
	}
}
