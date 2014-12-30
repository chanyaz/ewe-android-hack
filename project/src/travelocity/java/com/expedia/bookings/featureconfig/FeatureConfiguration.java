package com.expedia.bookings.featureconfig;

import android.content.Context;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.TravelocityLocaleChangeReceiver;
import com.expedia.bookings.data.pos.PointOfSale;

public class FeatureConfiguration implements IProductFlavorFeatureConfiguration {
	public String getServerEndpointsConfigurationPath() {
		return "ExpediaSharedData/TVLYServerURLs.json";
	}

	public String getPOSConfigurationPath() {
		return "ExpediaSharedData/TravelocityPointOfSaleConfig.json";
	}

	public String getAppNameForMobiataPushNameHeader() {
		return "TravelocityBookings";
	}

	public String getAppSupportUrl(Context context) {
		return PointOfSale.getPointOfSale().getAppSupportUrl();
	}

	public int getCrossSellStringResourceIdForShareEmail() {
		return R.string.share_template_long_ad_tvly;
	}

	public String getHostnameForShortUrl() {
		return "t.tvly.co";
	}

	public Boolean shouldDisplayInsuranceDetailsIfAvailableOnItinCard() {
		return true;
	}

	public String getActionForLocaleChangeEvent() {
		return TravelocityLocaleChangeReceiver.ACTION_LOCALE_CHANGED;
	}

	public Boolean wantsCustomHandlingForLocaleConfiguration() {
		return true;
	}

	public int getSearchProgressImageResId() {
		return R.id.search_progress_image_tvly;
	}

	public int getNotificationIconResourceId() {
		return R.drawable.ic_stat_travelocity;
	}

	public int getNotificationIndicatorLEDColor() {
		return 0x072b61;
	}
}
