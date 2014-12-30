package com.expedia.bookings.featureconfig;

import android.content.Context;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.AirAsiaGoLocaleChangeReceiver;

public class FeatureConfiguration implements IProductFlavorFeatureConfiguration {
	public String getServerEndpointsConfigurationPath() {
		return "ExpediaSharedData/AirAsiaGoServerURLs.json";
	}

	public String getPOSConfigurationPath() {
		return "ExpediaSharedData/AirAsiaGoPointOfSaleConfig.json";
	}

	public String getAppNameForMobiataPushNameHeader() {
		return "AAGBookings";
	}

	public String getAppSupportUrl(Context context) {
		return context.getString(R.string.app_support_url_aag);
	}

	public int getCrossSellStringResourceIdForShareEmail() {
		return R.string.share_template_long_ad_aag;
	}

	public String getHostnameForShortUrl() {
		return "a.aago.co";
	}

	public Boolean shouldDisplayInsuranceDetailsIfAvailableOnItinCard() {
		return false;
	}

	public String getActionForLocaleChangeEvent() {
		return AirAsiaGoLocaleChangeReceiver.ACTION_LOCALE_CHANGED;
	}

	public Boolean wantsCustomHandlingForLocaleConfiguration() {
		return true;
	}
}
