package com.expedia.bookings.featureconfig;

public class FeatureConfiguration implements IProductFlavorFeatureConfiguration {
	public String getServerEndpointsConfigurationPath() {
		return "ExpediaSharedData/AirAsiaGoServerURLs.json";
	}

	public String getAppNameForMobiataPushNameHeader() {
		return "AAGBookings";
	}
}
