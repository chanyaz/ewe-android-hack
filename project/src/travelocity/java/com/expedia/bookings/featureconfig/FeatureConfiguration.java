package com.expedia.bookings.featureconfig;

import android.content.Context;

import com.expedia.bookings.R;
import com.expedia.bookings.data.pos.PointOfSale;

public class FeatureConfiguration implements IProductFlavorFeatureConfiguration {
	public String getServerEndpointsConfigurationPath() {
		return "ExpediaSharedData/TVLYServerURLs.json";
	}

	public String getAppNameForMobiataPushNameHeader() {
		return "TravelocityBookings";
	}

	public String getAppSupportUrl(Context context) {
		return PointOfSale.getPointOfSale().getAppSupportUrl();
	}
}
