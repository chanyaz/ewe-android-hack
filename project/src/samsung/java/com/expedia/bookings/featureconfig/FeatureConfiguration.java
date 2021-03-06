package com.expedia.bookings.featureconfig;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.message.BasicNameValuePair;
import org.joda.time.DateTime;

import android.content.Intent;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.View;

import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.expedia.bookings.activity.AppIntroActivity;
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
		return "ExpediaSharedData/SamsungServerURLs.json";
	}

	public String getPOSConfigurationPath() {
		return "ExpediaSharedData/SamsungPointOfSaleConfig.json";
	}

	public String getAppNameForMobiataPushNameHeader() {
		return "SamsungBookings";
	}

	public String getAppSupportUrl(Context context) {
		return context.getString(R.string.app_support_url);
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

	public PointOfSaleId getDefaultPOS() {
		return PointOfSaleId.UNITED_STATES;
	}

	public boolean isAdXEnabled() {
		return true;
	}

	public int getAdXPosIdentifier() {
		return 2601;
	}

	public void contactUsViaWeb(Context context) {
		AboutUtils.openWebsite(context, PointOfSale.getPointOfSale().getAppSupportUrl(), true);
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
		return false;
	}

	@Override
	public boolean isTuneEnabled() {
		return false;
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
		return "samsung";
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

	@Override
	public boolean isItinDisabled() {
		return false;
	}

	public boolean isAppIntroEnabled() {
		return true;
	}

	public void launchAppIntroScreen(Context context) {
		Intent intent = new Intent(context, AppIntroActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}

	public boolean shouldSendSiteIdInRequests() {
		return true;
	}

	public String getPhoneCollectionId() {
		return "SamsungDestinations";
	}

	public int getSearchResultDealImageDrawable() {
		return R.drawable.for_samsung_blue;
	}

	public int getHotelDetailsDealImageDrawable() {
		return R.drawable.for_samsung_white;
	}

	@Override
	public int getCollectionCount() {
		return 11;
	}

	@Override
	public int getFlightSearchProgressImageResId() {
		return 0;
	}

	@Override
	public boolean isLOBIconCenterAligned() {
		return false;
	}

	@Override
	public int getLaunchScreenActionLogo() {
		return 0;
	}

	@Override
	public int updatePOSSpecificActionBarLogo() {
		return 0;
	}

	@Override
	public String getPOSSpecificBrandName(Context context) {
		return BuildConfig.brand;
	}

	@Override
	public HotelFilter.Sort getDefaultSort() {
		return HotelFilter.Sort.DEALS;
	}

	@Override
	public boolean sortByDistanceForCurrentLocation() {
		return false;
	}

	@Override
	public boolean isFacebookTrackingEnabled() {
		return false;
	}

	@Override
	public boolean isAbacusTestEnabled() {
		return false;
	}
}
