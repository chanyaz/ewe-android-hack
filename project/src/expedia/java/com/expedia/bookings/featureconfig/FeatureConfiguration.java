package com.expedia.bookings.featureconfig;

import java.lang.UnsupportedOperationException;

import android.content.Context;

import com.expedia.bookings.R;

public class FeatureConfiguration implements IProductFlavorFeatureConfiguration {
	public String getServerEndpointsConfigurationPath() {
		return "ExpediaSharedData/ExpediaServerURLs.json";
	}

	public String getPOSConfigurationPath() {
		return "ExpediaSharedData/ExpediaPointOfSaleConfig.json";
	}

	public String getAppNameForMobiataPushNameHeader() {
		return "ExpediaBookings";
	}

	public String getAppSupportUrl(Context context) {
		return context.getString(R.string.app_support_url);
	}

	public int getCrossSellStringResourceIdForShareEmail() {
		return R.string.share_template_long_ad;
	}

	public String getHostnameForShortUrl() {
		return "e.xpda.co";
	}

	public Boolean shouldDisplayInsuranceDetailsIfAvailableOnItinCard() {
		return true;
	}

	public String getActionForLocaleChangeEvent() {
		throw new UnsupportedOperationException("Not Required/Implemented for Expedia App");
	}

	public Boolean wantsCustomHandlingForLocaleConfiguration() {
		return false;
	}
}
