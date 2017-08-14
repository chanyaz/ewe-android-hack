package com.expedia.bookings.featureconfig;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.message.BasicNameValuePair;
import org.joda.time.DateTime;

import android.app.AlertDialog;
import android.content.Context;
import android.text.format.DateUtils;
import android.view.View;

import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.expedia.bookings.activity.AirAsiaGoLocaleChangeReceiver;
import com.expedia.bookings.data.HotelFilter;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.data.pos.PointOfSaleId;
import com.expedia.bookings.server.EndPoint;
import com.expedia.bookings.utils.AboutUtils;
import com.expedia.bookings.utils.JodaUtils;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.fragment.AboutSectionFragment;

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

	public boolean isAppCrossSellInActivityShareContentEnabled() {
		return false;
	}

	public boolean isAppCrossSellInCarShareContentEnabled() {
		return false;
	}

	public String getHostnameForShortUrl() {
		return "a.aago.co";
	}

	public boolean shouldDisplayInsuranceDetailsIfAvailableOnItinCard() {
		return false;
	}

	public String getActionForLocaleChangeEvent() {
		return AirAsiaGoLocaleChangeReceiver.ACTION_LOCALE_CHANGED;
	}

	public boolean wantsCustomHandlingForLocaleConfiguration() {
		return true;
	}

	public int getSearchProgressImageResId() {
		return R.id.search_progress_image_aag;
	}

	public int getNotificationIconResourceId() {
		return R.drawable.ic_stat_aag;
	}

	public int getNotificationIndicatorLEDColor() {
		return 0xfbc51e;
	}

	public boolean shouldShowBrandLogoOnAccountButton() {
		return true;
	}

	public int getLoginContainerBackgroundResId(Context context) {
		return Ui.obtainThemeResID(context, R.attr.skin_phoneCheckoutLoginButtonDrawable);
	}

	public boolean doesLoginTextViewHaveCompoundDrawables() {
		return false;
	}

	public PointOfSaleId getDefaultPOS() {
		return PointOfSaleId.AIRASIAGO_MALAYSIA;
	}

	public boolean isAdXEnabled() {
		return true;
	}

	public int getAdXPosIdentifier() {
		int adXPosIdentifier = 6259;

		// For AirAsiaGo Thailand setting a separate ID.
		if (PointOfSale.getPointOfSale().getTwoLetterCountryCode().toLowerCase().equals("th")) {
			adXPosIdentifier = 6258;
		}

		return adXPosIdentifier;
	}

	public String getOmnitureReportSuiteIds() {
		if (BuildConfig.RELEASE) {
			return "expediaglobalapp";
		}
		else {
			return "expediaglobalappdev";
		}
	}

	public String getOmnitureTrackingServer() {
		return "om.expedia.com";
	}

	public void contactUsViaWeb(Context context) {
		AboutUtils.openWebsite(context, PointOfSale.getPointOfSale().getAppSupportUrl(), true);
	}

	public int getResIdForErrorBookingSucceededWithErrors() {
		return R.string.e3_error_checkout_booking_succeeded_with_errors_aag;
	}

	public int getResIdForErrorHotelServiceFatalFailure() {
		return R.string.e3_error_hotel_offers_hotel_service_failure_aag;
	}

	public List<BasicNameValuePair> getAdditionalParamsForReviewsRequest() {
		List<BasicNameValuePair> additionalParamsForReviewsRequest = new ArrayList<BasicNameValuePair>();
		additionalParamsForReviewsRequest.add(new BasicNameValuePair("locale", PointOfSale.getPointOfSale().getLocaleIdentifier()));
		return additionalParamsForReviewsRequest;
	}

	public boolean shouldUseDotlessDomain(EndPoint endpoint) {
		return endpoint != EndPoint.PRODUCTION;
	}

	public String touchupE3EndpointUrlIfRequired(String e3EndpointUrl) {
		//Domain name for AAG Thailand is thailand.airasiago.com, so removing www from URL.
		if (PointOfSale.getPointOfSale().getPointOfSaleId().equals(PointOfSaleId.AIRASIAGO_THAILAND)) {
			e3EndpointUrl = e3EndpointUrl.replaceFirst("w{3}\\.?", "");
		}
		return e3EndpointUrl;
	}

	public View.OnClickListener getInsuranceLinkViewClickListener(final Context context, final String insuranceTermsUrl) {
		throw new UnsupportedOperationException("Insurance not supported on Air Asia Go.");
	}

	public boolean isLeanPlumEnabled() {
		return false;
	}

	public boolean isKahunaEnabled() {
		return false;
	}

	public boolean isWeAreHiringInAboutEnabled() {
		return false;
	}

	public boolean isClearPrivateDataInAboutEnabled() {
		return false;
	}

	public String getCopyrightLogoUrl(Context context) {
		return PointOfSale.getPointOfSale().getWebsiteUrl();
	}

	public boolean areSocialMediaMenuItemsInAboutEnabled() {
		return false;
	}

	public AboutSectionFragment getAboutSectionFragment(Context context) {
		return null;
	}

	public boolean isLocalExpertEnabled() {
		return false;
	}

	public boolean isFacebookLoginIntegrationEnabled() {
		return false;
	}

	public boolean isFacebookShareIntegrationEnabled() {
		return false;
	}

	public boolean isGoogleWalletPromoEnabled() {
		return false;
	}

	public boolean isTrackingWithFlightTrackEnabled() {
		return false;
	}

	public boolean isHangTagProgressBarEnabled() {
		return false;
	}

	public boolean isSettingsInMenuVisible() {
		return true;
	}

	public String formatDateTimeForHotelUserReviews(Context context, DateTime dateTime) {
		return JodaUtils.formatDateTime(context, dateTime, DateUtils.FORMAT_NUMERIC_DATE);
	}

	public int getHotelSalePriceTextColorResourceId(Context context) {
		return Ui.obtainThemeColor(context, R.attr.skin_hotelPriceStandardColor);
	}

	public boolean wantsOtherAppsCrossSellInConfirmationScreen() {
		return false;
	}

	public void setupOtherAppsCrossSellInConfirmationScreen(final Context context, View view) {
	}

	public boolean isETPEnabled() {
		return false;
	}

	public String getClientShortName() {
		return "aag";
	}

	public String getAdXKey() {
		//Key not available for AAG for now, so passing blank.
		return "";
	}

	public boolean isAppSupportUrlEnabled() {
		return true;
	}

	public boolean isSigninEnabled() {
		return true;
	}

	public boolean isAppCrossSellInHotelShareContentEnabled() {
		return true;
	}

	public boolean isAppCrossSellInFlightShareContentEnabled() {
		return true;
	}

	public boolean isAppIntroEnabled() {
		return false;
	}

	public void launchAppIntroScreen(Context context) {
		throw new UnsupportedOperationException("App intro not supported.");
	}

	public boolean shouldSendSiteIdInRequests() {
		return false;
	}

	public String getPhoneCollectionId() {
		return "PhoneDestinations";
	}

	public int getSearchResultDealImageDrawable() {
		//No deal image
		return 0;
	}

	public int getHotelDetailsDealImageDrawable() {
		//No deal image
		return 0;
	}

	public int getCollectionCount() {
		// No need to hard code count
		return 0;
	}

	@Override
	public int getFlightSearchProgressImageResId() {
		return R.drawable.search_progress_static_flight_aag;
	}

	@Override
	public HotelFilter.Sort getDefaultSort() {
		return HotelFilter.Sort.POPULAR;
	}

	@Override
	public boolean sortByDistanceForCurrentLocation() {
		return true;
	}

	@Override
	public boolean isAbacusTestEnabled() {
		return false;
	}

	@Override
	public AlertDialog getUnsupportedVersionDialog(Context context) {
		return null;
	}
}
