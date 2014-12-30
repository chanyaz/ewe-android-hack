package com.expedia.bookings.featureconfig;

import android.content.Context;

public interface IProductFlavorFeatureConfiguration {
	String getServerEndpointsConfigurationPath();
	String getPOSConfigurationPath();
	String getAppNameForMobiataPushNameHeader();
	String getAppSupportUrl(Context context);
	int getCrossSellStringResourceIdForShareEmail();
	String getHostnameForShortUrl();
	Boolean shouldDisplayInsuranceDetailsIfAvailableOnItinCard();
	String getActionForLocaleChangeEvent();
	Boolean wantsCustomHandlingForLocaleConfiguration();
	int getSearchProgressImageResId();
}
