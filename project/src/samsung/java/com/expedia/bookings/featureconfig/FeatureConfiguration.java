package com.expedia.bookings.featureconfig;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.message.BasicNameValuePair;
import org.joda.time.DateTime;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateUtils;
import android.view.View;

import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.expedia.bookings.activity.AppIntroActivity;
import com.expedia.bookings.activity.WebViewActivity;
import com.expedia.bookings.data.hotel.DisplaySort;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.data.pos.PointOfSaleId;
import com.expedia.bookings.server.EndPoint;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.AboutUtils;
import com.expedia.bookings.utils.JodaUtils;
import com.expedia.bookings.utils.Ui;

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

	public boolean wantsCustomHandlingForLocaleConfiguration() {
		return true;
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
				builder.setTitle(R.string.insurance);
				builder.setAllowMobileRedirects(false);
				context.startActivity(builder.getIntent());
			}
		};
	}

	@Override
	public boolean isTuneEnabled() {
		return false;
	}

	public boolean isKahunaEnabled() {
		return true;
	}

	public boolean isClearPrivateDataInAboutEnabled() {
		return false;
	}

	public String getCopyrightLogoUrl(Context context) {
		return context.getString(R.string.app_copyright_logo_url);
	}

	public boolean areSocialMediaMenuItemsInAboutEnabled() {
		return true;
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

	public String formatDateTimeForHotelUserReviews(Context context, DateTime dateTime) {
		return JodaUtils.formatDateTime(context, dateTime, DateUtils.FORMAT_NUMERIC_DATE);
	}

	public int getHotelSalePriceTextColorResourceId(Context context) {
		return Ui.obtainThemeColor(context, R.attr.skin_hotelPriceStandardColor);
	}

	public boolean wantsOtherAppsCrossSellInConfirmationScreen() {
		return false;
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

	public int getHotelDealImageDrawable() {
		return R.drawable.for_samsung;
	}

	@Override
	public int getCollectionCount() {
		return 11;
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
	public DisplaySort getDefaultSort() {
		return DisplaySort.DEALS;
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
		return true;
	}

	@Override
	public boolean isNewHotelEnabled() {
		return false;
	}

	@Override
	public int getRewardsLayoutId() {
		return 0;
	}

	@Override
	public boolean isRewardProgramPointsType() {
		return true;
	}

	private static final String[] rewardTierAPINames = new String[] { "BLUE", "SILVER", "GOLD" };
	private static final String[] rewardTierSupportPhoneNumberConfigNames = new String[] {
			null, "supportPhoneNumberSilver", "supportPhoneNumberGold"
	};
	private static final String[] rewardTierSupportEmailConfigNames = new String[] {
			null, "supportEmailSilver", "supportEmailGold"
	};

	@Override
	public String[] getRewardTierAPINames() {
		return rewardTierAPINames;
	}

	@Override
	public String[] getRewardTierSupportNumberConfigNames() {
		return rewardTierSupportPhoneNumberConfigNames;
	}

	@Override
	public String[] getRewardTierSupportEmailConfigNames() {
		return rewardTierSupportEmailConfigNames;
	}

	@Override
	public boolean isCommunicateSectionEnabled() {
		return true;
	}

	@Override
	public PointOfSaleId getUSPointOfSaleId() {
		return PointOfSaleId.UNITED_STATES;
	}

	@Override
	public boolean isGoogleAccountChangeEnabled() {
		return false;
	}

	@Override
	public String getOmnitureEventValue(OmnitureTracking.OmnitureEventName key) {
		switch (key) {
		case REWARD_PROGRAM_NAME:
			return "expedia";
		case HOTEL_CHECKOUT_START_REWARDS_REDEEMABLE:
			return "event114";
		case REWARD_APPLIED_PERCENTAGE_TEMPLATE:
			return "expedia | %d";
		case NO_REWARDS_USED:
			return "no points used";
		case TOTAL_POINTS_BURNED:
			return "event117";
		case BRAND_KEY_FOR_OMNITURE:
			return "Expedia";
		}
		//It should not be the case
		throw new IllegalArgumentException("Unknown enum value");
	}

	@Override
	public boolean shouldShowMemberTier() {
		return true;
	}

	@Override
	public boolean shouldShowAirAttach() {
		return true;
	}

	@Override
	public String getSharableFallbackImageURL() {
		return null;
	}

	@Override
	public boolean shouldDisplayItinTrackAppLink() {
		return true;
	}

	@Override
	public boolean shouldSetExistingUserForTune() {
		return false;
	}

	@Override
	public boolean shouldShowItinShare() {
		return true;
	}

	@Override
	public boolean isRateOurAppEnabled() {
		return false;
	}

	@Override
	public boolean isRewardsCardEnabled() {
		return false;
	}

	@Override
	public String getRewardsCardUrl(Context context) {
		return null;
	}

	@Override
	public boolean showUserRewardsEnrollmentCheck() {
		return false;
	}

	@Override
	public boolean sendEapidToTuneTracking() {
		return false;
	}

	@Override
	public boolean shouldShowPackageIncludesView() {
		return true;
	}

	@Override
	public boolean showHotelLoyaltyEarnMessage() {
		return false;
	}

	@Override
	public boolean shouldShowUserReview() {
		return true;
	}

	@Override
	public boolean shouldShowVIPLoyaltyMessage() {
		return false;
	}

	@Override
	public int getPOSSpecificBrandLogo() {
		return R.drawable.app_copyright_logo;
	}

	@Override
	public String getPosURLToShow(String posUrl) {
		return posUrl;
	}
}
