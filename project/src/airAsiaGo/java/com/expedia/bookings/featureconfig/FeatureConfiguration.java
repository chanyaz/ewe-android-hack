package com.expedia.bookings.featureconfig;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.message.BasicNameValuePair;
import org.joda.time.DateTime;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.View;

import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.expedia.bookings.activity.AirAsiaGoLocaleChangeReceiver;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.data.pos.PointOfSaleId;
import com.expedia.bookings.server.EndPoint;
import com.expedia.bookings.utils.AboutUtils;
import com.expedia.bookings.utils.JodaUtils;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.fragment.AboutSectionFragment;

public class FeatureConfiguration implements IProductFlavorFeatureConfiguration {
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
	public String getActionForLocaleChangeEvent() {
		return AirAsiaGoLocaleChangeReceiver.ACTION_LOCALE_CHANGED;
	}

	@Override
	public boolean wantsCustomHandlingForLocaleConfiguration() {
		return true;
	}

	@Override
	public int getSearchProgressImageResId() {
		return R.id.search_progress_image_aag;
	}

	@Override
	public int getNotificationIconResourceId() {
		return R.drawable.ic_stat_aag;
	}

	@Override
	public int getNotificationIndicatorLEDColor() {
		return 0xfbc51e;
	}

	@Override
	public boolean shouldShowBrandLogoOnAccountButton() {
		return true;
	}

	@Override
	public PointOfSaleId getDefaultPOS() {
		return PointOfSaleId.AIRASIAGO_MALAYSIA;
	}

	@Override
	public boolean isAdXEnabled() {
		return true;
	}

	@Override
	public int getAdXPosIdentifier() {
		int adXPosIdentifier = 6259;

		// For AirAsiaGo Thailand setting a separate ID.
		if (PointOfSale.getPointOfSale().getTwoLetterCountryCode().toLowerCase().equals("th")) {
			adXPosIdentifier = 6258;
		}

		return adXPosIdentifier;
	}

	@Override
	public String getOmnitureReportSuiteIds() {
		if (BuildConfig.RELEASE) {
			return "expediaglobalapp";
		}
		else {
			return "expediaglobalappdev";
		}
	}

	@Override
	public String getOmnitureTrackingServer() {
		return "om.expedia.com";
	}

	@Override
	public void contactUsViaWeb(Context context) {
		AboutUtils.openWebsite(context, PointOfSale.getPointOfSale().getAppSupportUrl(), true);
	}

	@Override
	public List<BasicNameValuePair> getAdditionalParamsForReviewsRequest() {
		List<BasicNameValuePair> additionalParamsForReviewsRequest = new ArrayList<BasicNameValuePair>();
		additionalParamsForReviewsRequest.add(new BasicNameValuePair("locale", PointOfSale.getPointOfSale().getLocaleIdentifier()));
		return additionalParamsForReviewsRequest;
	}

	@Override
	public boolean shouldUseDotlessDomain(EndPoint endpoint) {
		return endpoint != EndPoint.PRODUCTION;
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
	public boolean isLeanPlumEnabled() {
		return false;
	}

	@Override
	public boolean isTuneEnabled() {
		return false;
	}

	@Override
	public boolean isWeAreHiringInAboutEnabled() {
		return false;
	}

	@Override
	public boolean isClearPrivateDataInAboutEnabled() {
		return false;
	}

	@Override
	public String getCopyrightLogoUrl(Context context) {
		return PointOfSale.getPointOfSale().getWebsiteUrl();
	}

	@Override
	public boolean areSocialMediaMenuItemsInAboutEnabled() {
		return false;
	}

	@Override
	public AboutSectionFragment getAboutSectionFragment(Context context) {
		return null;
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
	public boolean isGoogleWalletPromoEnabled() {
		return false;
	}

	@Override
	public boolean isHangTagProgressBarEnabled() {
		return false;
	}

	@Override
	public boolean isSettingsInMenuVisible() {
		return true;
	}

	@Override
	public String formatDateTimeForHotelUserReviews(Context context, DateTime dateTime) {
		return JodaUtils.formatDateTime(context, dateTime, DateUtils.FORMAT_NUMERIC_DATE);
	}

	@Override
	public int getHotelSalePriceTextColorResourceId(Context context) {
		return Ui.obtainThemeColor(context, R.attr.skin_hotelPriceStandardColor);
	}

	@Override
	public boolean wantsOtherAppsCrossSellInConfirmationScreen() {
		return false;
	}

	@Override
	public void setupOtherAppsCrossSellInConfirmationScreen(final Context context, View view) {
	}

	@Override
	public boolean isETPEnabled() {
		return false;
	}

	@Override
	public String getClientShortName() {
		return "aag";
	}

	@Override
	public String getAdXKey() {
		//Key not available for AAG for now, so passing blank.
		return "";
	}

	@Override
	public boolean isSigninEnabled() {
		return true;
	}

	@Override
	public boolean isAppCrossSellInHotelShareContentEnabled() {
		return true;
	}

	@Override
	public boolean isAppCrossSellInFlightShareContentEnabled() {
		return true;
	}

	@Override
	public int getFlightSearchProgressImageResId() {
		return R.drawable.search_progress_static_flight_aag;
	}
	public boolean isLOBIconCenterAligned() {
		return false;
	}

	@Override
	public int getLaunchScreenActionLogo() {
		return 0;
	}
}
