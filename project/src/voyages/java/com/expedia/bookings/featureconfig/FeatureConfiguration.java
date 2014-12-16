package com.expedia.bookings.featureconfig;

import android.content.Context;

import com.expedia.bookings.R;

public class FeatureConfiguration implements IProductFlavorFeatureConfiguration {
	public String getServerEndpointsConfigurationPath() {
		return "ExpediaSharedData/VSCServerURLs.json";
	}

	public String getPOSConfigurationPath() {
		return "ExpediaSharedData/VSCPointOfSaleConfig.json";
	}

	public String getAppNameForMobiataPushNameHeader() {
		return "VSCBookings";
	}

	public String getAppSupportUrl(Context context) {
		return context.getString(R.string.app_support_url_vsc);
	}

	public int getCrossSellStringResourceIdForShareEmail() {
		return R.string.share_template_long_ad_vsc;
	}

	public String getHostnameForShortUrl() {
		return "v.vygs.co";
	}
}
