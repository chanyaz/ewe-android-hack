package com.expedia.bookings.featureconfig;

import android.content.Context;
import android.view.View;
import com.expedia.bookings.R;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.data.pos.PointOfSaleId;

public class FeatureConfiguration extends BaseFeatureConfiguration {
	@Override
	public String getServerEndpointsConfigurationPath() {
		return "ExpediaSharedData/AirAsiaGoServerURLs.json";
	}

	@Override
	public String getPOSConfigurationPath() {
		return "ExpediaSharedData/AirAsiaGoPointOfSaleConfig.json";
	}

	@Override
	public String getAppNameForMobiataPushNameHeader() {
		return "AAGBookings";
	}

	@Override
	public String getAppSupportUrl(Context context) {
		return context.getString(R.string.app_support_url_aag);
	}

	@Override
	public boolean isAppCrossSellInActivityShareContentEnabled() {
		return false;
	}

	@Override
	public boolean isAppCrossSellInCarShareContentEnabled() {
		return false;
	}

	@Override
	public String getHostnameForShortUrl() {
		return "a.aago.co";
	}

	@Override
	public boolean shouldDisplayInsuranceDetailsIfAvailableOnItinCard() {
		return false;
	}

	@Override
	public int getNotificationIndicatorLEDColor() {
		return 0xfbc51e;
	}

	@Override
	public PointOfSaleId getDefaultPOS() {
		return PointOfSaleId.AIRASIAGO_MALAYSIA;
	}

	@Override
	public String touchupE3EndpointUrlIfRequired(String e3EndpointUrl) {
		//Domain name for AAG Thailand is thailand.airasiago.com, so removing www from URL.
		if (PointOfSale.getPointOfSale().getPointOfSaleId().equals(PointOfSaleId.AIRASIAGO_THAILAND)) {
			e3EndpointUrl = e3EndpointUrl.replaceFirst("w{3}\\.?", "");
		}
		return e3EndpointUrl;
	}

	@Override
	public View.OnClickListener getInsuranceLinkViewClickListener(final Context context, final String insuranceTermsUrl) {
		throw new UnsupportedOperationException("Insurance not supported on Air Asia Go.");
	}

	@Override
	public boolean isFacebookLoginIntegrationEnabled() {
		return false;
	}

	@Override
	public boolean isFacebookShareIntegrationEnabled() {
		return false;
	}

	@Override
	public String getClientShortName() {
		return "aag";
	}

	@Override
	public boolean isAbacusTestEnabled() {
		return false;
	}

	@Override
	public boolean isCommunicateSectionEnabled() {
		return false;
	}

	@Override
	public boolean isRateOurAppEnabled() {
		return false;
	}

	@Override
	public boolean showUserRewardsEnrollmentCheck() {
		return false;
	}

	@Override
	public boolean showHotelLoyaltyEarnMessage() {
		return false;
	}

	@Override
	public boolean shouldForceEnableMIDAPIForPackages() {
		return true;
	}
}
