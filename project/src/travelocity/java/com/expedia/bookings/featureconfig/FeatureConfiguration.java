package com.expedia.bookings.featureconfig;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.message.BasicNameValuePair;
import org.joda.time.DateTime;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.format.DateUtils;
import android.view.View;

import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.expedia.bookings.activity.TravelocityLocaleChangeReceiver;
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
		return "ExpediaSharedData/TVLYServerURLs.json";
	}

	public String getPOSConfigurationPath() {
		return "ExpediaSharedData/TravelocityPointOfSaleConfig.json";
	}

	public String getAppNameForMobiataPushNameHeader() {
		return "TvlyBookings";
	}

	public String getAppSupportUrl(Context context) {
		return PointOfSale.getPointOfSale().getAppSupportUrl();
	}

	public int getCrossSellStringResourceIdForShareEmail() {
		return R.string.share_template_long_ad_tvly;
	}

	public boolean isAppCrossSellInActivityShareContentEnabled() {
		return true;
	}

	public boolean isAppCrossSellInCarShareContentEnabled() {
		return true;
	}

	public String getHostnameForShortUrl() {
		return "t.tvly.co";
	}

	public boolean shouldDisplayInsuranceDetailsIfAvailableOnItinCard() {
		return true;
	}

	public String getActionForLocaleChangeEvent() {
		return TravelocityLocaleChangeReceiver.ACTION_LOCALE_CHANGED;
	}

	public boolean wantsCustomHandlingForLocaleConfiguration() {
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

	public boolean shouldShowBrandLogoOnAccountButton() {
		return true;
	}

	public int getLoginContainerBackgroundResId(Context context) {
		return R.drawable.btn_login_hotels;
	}

	public boolean doesLoginTextViewHaveCompoundDrawables() {
		return true;
	}

	public PointOfSaleId getDefaultPOS() {
		return PointOfSaleId.TRAVELOCITY;
	}

	public boolean isAdXEnabled() {
		return true;
	}

	public int getAdXPosIdentifier() {
		int adXPosIdentifier = 13292;

		// For Travelocity canada setting a separate ID.
		if (PointOfSale.getPointOfSale().getPointOfSaleId() == PointOfSaleId.TRAVELOCITY_CA) {
			adXPosIdentifier = 14776;
		}

		return adXPosIdentifier;
	}

	public String getOmnitureReportSuiteIds() {
		if (BuildConfig.RELEASE) {
			return "expediaglobalapp,tvlglobalapp";
		}
		else {
			return "expediaglobalappdev";
		}
	}

	public String getOmnitureTrackingServer() {
		return "om.travelocity.com";
	}

	public void contactUsViaWeb(Context context) {
		AboutUtils.openWebsite(context, PointOfSale.getPointOfSale().getAppSupportUrl(), true);
	}

	public int getResIdForErrorBookingSucceededWithErrors() {
		return R.string.e3_error_checkout_booking_succeeded_with_errors_tvly;
	}

	public int getResIdForErrorHotelServiceFatalFailure() {
		return R.string.e3_error_hotel_offers_hotel_service_failure_tvly;
	}

	public List<BasicNameValuePair> getAdditionalParamsForReviewsRequest() {
		List<BasicNameValuePair> additionalParamsForReviewsRequest = new ArrayList<BasicNameValuePair>();
		additionalParamsForReviewsRequest.add(new BasicNameValuePair("origin", "TRAVELOCITY"));
		additionalParamsForReviewsRequest.add(new BasicNameValuePair("locale", PointOfSale.getPointOfSale().getLocaleIdentifier()));
		return additionalParamsForReviewsRequest;
	}

	public boolean shouldUseDotlessDomain(EndPoint endpoint) {
		return endpoint != EndPoint.PRODUCTION;
	}

	public String touchupE3EndpointUrlIfRequired(String e3EndpointUrl) {
		return e3EndpointUrl;
	}

	public View.OnClickListener getInsuranceLinkViewClickListener(final Context context, final String insuranceTermsUrl) {
		return new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent viewInsuranceIntent = new Intent(Intent.ACTION_VIEW);
				viewInsuranceIntent.setData(Uri.parse(insuranceTermsUrl));
				context.startActivity(viewInsuranceIntent);
			}
		};
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
		return true;
	}

	public String getClientShortName() {
		return "tvly";
	}

	public String getAdXKey() {
		return "65bcdbdb-8841-4d72-b792-4123773d5725";
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
		return R.drawable.search_progress_static_flight_tvly;
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
	}}
