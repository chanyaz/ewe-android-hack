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
	@Override
	public String getServerEndpointsConfigurationPath() {
		return "ExpediaSharedData/EbookersServerURLs.json";
	}

	@Override
	public String getPOSConfigurationPath() {
		return "ExpediaSharedData/EbookersPointOfSaleConfig.json";
	}

	@Override
	public String getAppNameForMobiataPushNameHeader() {
		return "EbookersBookings";
	}

	@Override
	public String getAppSupportUrl(Context context) {
		return context.getString(R.string.app_support_url);
	}

	@Override
	public boolean isAppCrossSellInActivityShareContentEnabled() {
		return true;
	}

	@Override
	public boolean isAppCrossSellInCarShareContentEnabled() {
		return true;
	}

	@Override
	public String getHostnameForShortUrl() {
		return "e.bukr.us";
	}

	@Override
	public boolean shouldDisplayInsuranceDetailsIfAvailableOnItinCard() {
		return true;
	}

	@Override
	public boolean wantsCustomHandlingForLocaleConfiguration() {
		return true;
	}

	@Override
	public int getSearchProgressImageResId() {
		return 0;
	}

	@Override
	public int getNotificationIconResourceId() {
		return R.drawable.ic_stat;
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
		return PointOfSaleId.EBOOKERS_UNITED_KINGDOM;
	}

	@Override
	public void contactUsViaWeb(Context context) {
		AboutUtils.openWebsite(context, PointOfSale.getPointOfSale().getBookingSupportUrl(), true);
	}

	@Override
	public List<BasicNameValuePair> getAdditionalParamsForReviewsRequest() {
		List<BasicNameValuePair> additionalParamsForReviewsRequest = new ArrayList<>();
		additionalParamsForReviewsRequest.add(
			new BasicNameValuePair("locale", PointOfSale.getPointOfSale().getLocaleIdentifier()));
		return additionalParamsForReviewsRequest;
	}

	@Override
	public boolean shouldUseDotlessDomain(EndPoint endpoint) {
		return endpoint != EndPoint.PRODUCTION;
	}

	@Override
	public String touchupE3EndpointUrlIfRequired(String e3EndpointUrl) {
		return e3EndpointUrl;
	}

	@Override
	public View.OnClickListener getInsuranceLinkViewClickListener(final Context context, final String insuranceTermsUrl) {
		return new View.OnClickListener() {
			@Override
			public void onClick(View view) {
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
		return true;
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
		return true;
	}

	@Override
	public boolean isFacebookLoginIntegrationEnabled() {
		return true;
	}

	@Override
	public boolean isFacebookShareIntegrationEnabled() {
		return true;
	}

	@Override
	public boolean isHangTagProgressBarEnabled() {
		return true;
	}

	@Override
	public String formatDateTimeForHotelUserReviews(Context context, DateTime dateTime) {
		return JodaUtils.formatDateTime(context, dateTime, DateUtils.FORMAT_NUMERIC_DATE | DateUtils.FORMAT_SHOW_YEAR);
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
	public boolean isETPEnabled() {
		return true;
	}

	@Override
	public String getClientShortName() {
		return "ebookers";
	}

	public String getAdXKey() {
		return "f2d75b7e-ed66-4f96-cf66-870f4c6b723e";
	}

	@Override
	public boolean isSigninEnabled() {
		return true;
	}

	public boolean isAppSupportUrlEnabled() {
		return true;
	}

	@Override
	public boolean isLOBIconCenterAligned() {
		return false;
	}

	@Override
	public int getLaunchScreenActionLogo() {
		return 0;
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

	public int getHotelDealImageDrawable() {
		//No deal image
		return 0;
	}

	public int getCollectionCount() {
		// No need to hard code count
		return 0;
	}

	@Override
	public int updatePOSSpecificActionBarLogo() {
		//ignore
		return 0;
	}

	@Override
	public String getPOSSpecificBrandName(Context context) {
		return BuildConfig.brand;
	}

	@Override
	public DisplaySort getDefaultSort() {
		return DisplaySort.RECOMMENDED;
	}

	@Override
	public boolean sortByDistanceForCurrentLocation() {
		return true;
	}

	@Override
	public boolean isFacebookTrackingEnabled() {
		return true;
	}

	@Override
	public boolean isAbacusTestEnabled() {
		return true;
	}

	@Override
	public boolean isNewHotelEnabled() {
		return true;
	}

	@Override
	public int getRewardsLayoutId() {
		return R.layout.bucks_widget_stub;
	}

	@Override
	public boolean isRewardProgramPointsType() {
		return false;
	}

	private static final String[] rewardTierAPINames = new String[] { "SILVER", "GOLD", "PLATINUM" };
	private static final String[] rewardTierSupportPhoneNumberConfigNames = new String[] {
			"supportPhoneNumberSilver", "supportPhoneNumberGold", "supportPhoneNumberPlatinum"
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
		return null;
	}

	@Override
	public boolean isCommunicateSectionEnabled() {
		return true;
	}

	@Override
	public PointOfSaleId getUSPointOfSaleId() {
		return null;
	}

	@Override
	public boolean isGoogleAccountChangeEnabled() {
		return true;
	}

	@Override
	public String getOmnitureEventValue(OmnitureTracking.OmnitureEventName key) {
		switch (key) {
		case REWARD_PROGRAM_NAME:
			return "ebookers";
		case HOTEL_CHECKOUT_START_REWARDS_REDEEMABLE:
			return "event119";
		case REWARD_APPLIED_PERCENTAGE_TEMPLATE:
			return "ebookers | %d";
		case NO_REWARDS_USED:
			return "no bonus+ used";
		case TOTAL_POINTS_BURNED:
			return "event123";
		case BRAND_KEY_FOR_OMNITURE:
			return "EBookers";
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
		return false;
	}

	@Override
	public boolean shouldSetExistingUserForTune() {
		return false;
	}

	@Override
	public boolean shouldShowItinShare() {
		return false;
	}

	@Override
	public boolean isRateOurAppEnabled() {
		return true;
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
		return true;
	}

	@Override
	public boolean sendEapidToTuneTracking() {
		return false;
	}

	@Override
	public boolean shouldShowPackageIncludesView() {
		return false;
	}

	@Override
	public boolean showHotelLoyaltyEarnMessage() {
		return true;
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
