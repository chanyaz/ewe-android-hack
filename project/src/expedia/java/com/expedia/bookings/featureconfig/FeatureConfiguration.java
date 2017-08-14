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
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.activity.WebViewActivity;
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

	public boolean isAppCrossSellInActivityShareContentEnabled() {
		return true;
	}

	public boolean isAppCrossSellInCarShareContentEnabled() {
		return true;
	}

	public String getHostnameForShortUrl() {
		return "e.xpda.co";
	}

	public boolean shouldDisplayInsuranceDetailsIfAvailableOnItinCard() {
		return true;
	}

	public String getActionForLocaleChangeEvent() {
		if (ExpediaBookingApp.isAutomation()) {
			return null;
		}
		throw new UnsupportedOperationException("Not Required/Implemented for Expedia App");
	}

	public boolean wantsCustomHandlingForLocaleConfiguration() {
		return false;
	}

	public int getSearchProgressImageResId() {
		return 0;
	}

	public int getNotificationIconResourceId() {
		return R.drawable.ic_stat_expedia;
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
		return true;
	}

	public PointOfSaleId getDefaultPOS() {
		return PointOfSaleId.UNITED_KINGDOM;
	}

	public boolean isAdXEnabled() {
		return true;
	}

	public int getAdXPosIdentifier() {
		return 2601;
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
		return R.string.e3_error_checkout_booking_succeeded_with_errors;
	}

	public int getResIdForErrorHotelServiceFatalFailure() {
		return R.string.e3_error_hotel_offers_hotel_service_failure;
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
		return e3EndpointUrl;
	}

	public View.OnClickListener getInsuranceLinkViewClickListener(final Context context, final String insuranceTermsUrl) {
		return new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				WebViewActivity.IntentBuilder builder = new WebViewActivity.IntentBuilder(context);
				builder.setUrl(insuranceTermsUrl);
				builder.setTheme(R.style.ItineraryTheme);
				builder.setTitle(R.string.insurance);
				builder.setAllowMobileRedirects(false);
				context.startActivity(builder.getIntent());
			}
		};
	}

	public boolean isLeanPlumEnabled() {
		return true;
	}

	public boolean isKahunaEnabled() {
		return true;
	}

	public boolean isWeAreHiringInAboutEnabled() {
		return true;
	}

	public boolean isClearPrivateDataInAboutEnabled() {
		return false;
	}

	public String getCopyrightLogoUrl(Context context) {
		return context.getString(Ui.obtainThemeResID(context, R.attr.skin_aboutInfoUrlString));
	}

	public boolean areSocialMediaMenuItemsInAboutEnabled() {
		return true;
	}

	public AboutSectionFragment getAboutSectionFragment(Context context) {
		return AboutSectionFragment.buildOtherAppsSection(context);
	}

	public boolean isLocalExpertEnabled() {
		return true;
	}

	public boolean isFacebookLoginIntegrationEnabled() {
		return true;
	}

	public boolean isFacebookShareIntegrationEnabled() {
		return true;
	}

	public boolean isGoogleWalletPromoEnabled() {
		return true;
	}

	public boolean isTrackingWithFlightTrackEnabled() {
		return true;
	}

	public boolean isHangTagProgressBarEnabled() {
		return true;
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
		return "expedia";
	}

	public String getAdXKey() {
		return "f2d75b7e-ed66-4f96-cf66-870f4c6b723e";
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
		return 0;
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
		return true;
	}

	@Override
	public AlertDialog getUnsupportedVersionDialog(Context context) {
		return null;
	}
}
