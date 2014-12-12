package com.expedia.bookings.featureconfig;

import android.content.Context;

public interface IProductFlavorFeatureConfiguration {
	String getServerEndpointsConfigurationPath();
	String getAppNameForMobiataPushNameHeader();
	String getAppSupportUrl(Context context);
	int getCrossSellStringResourceIdForShareEmail();
	String getHostnameForShortUrl();
}
