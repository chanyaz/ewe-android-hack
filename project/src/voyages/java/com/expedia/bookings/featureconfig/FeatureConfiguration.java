package com.expedia.bookings.featureconfig;

public class FeatureConfiguration implements IProductFlavorFeatureConfiguration {
	public String getServerEndpointsConfigurationPath() {
		return "ExpediaSharedData/VSCServerURLs.json";
	}

	public String getAppNameForMobiataPushNameHeader() {
		return "VSCBookings";
	}
}
