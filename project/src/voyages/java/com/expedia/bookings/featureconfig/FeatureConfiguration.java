package com.expedia.bookings.featureconfig;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.message.BasicNameValuePair;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import android.content.Context;
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
import com.expedia.bookings.utils.Ui;

public class FeatureConfiguration implements IProductFlavorFeatureConfiguration {
	@Override
	public String getServerEndpointsConfigurationPath() {
		return "ExpediaSharedData/VSCServerURLs.json";
	}

	@Override
	public String getPOSConfigurationPath() {
		return "ExpediaSharedData/VSCPointOfSaleConfig.json";
	}

	@Override
	public String getAppNameForMobiataPushNameHeader() {
		return "VSCBookings";
	}

	@Override
	public String getAppSupportUrl(Context context) {
		return PointOfSale.getPointOfSale().getBookingSupportUrl();
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
		return "v.vygs.co";
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
		return R.drawable.ic_stat_vsc;
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
		return PointOfSaleId.VSC;
	}

	@Override
	public void contactUsViaWeb(Context context) {
		AboutUtils.openWebsite(context, "http://voyages-sncf.mobi/aide-appli-2/aide-appli-hotel/pagecontactandroid.html", false);
	}

	@Override
	public List<BasicNameValuePair> getAdditionalParamsForReviewsRequest() {
		List<BasicNameValuePair> additionalParamsForReviewsRequest = new ArrayList<>();
		additionalParamsForReviewsRequest.add(new BasicNameValuePair("origin", "VSC"));
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

	@Override
	public boolean isClearPrivateDataInAboutEnabled() {
		return true;
	}

	@Override
	public String getCopyrightLogoUrl(Context context) {
		return context.getString(R.string.app_copyright_logo_url);
	}

	@Override
	public boolean areSocialMediaMenuItemsInAboutEnabled() {
		return false;
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
	public boolean isHangTagProgressBarEnabled() {
		return true;
	}

	@Override
	public String formatDateTimeForHotelUserReviews(Context context, DateTime dateTime) {
		//1608. VSC - Apparently since we are forcing FR locale the dateUtils is not formatting appropriately.
		//Ugly hack to ensure European date format.
		String customDateFormat = "dd/MM/yyyy";
		DateTimeFormatter dtf = DateTimeFormat.forPattern(customDateFormat);
		return dtf.print(dateTime);
	}

	@Override
	public int getHotelSalePriceTextColorResourceId(Context context) {
		//1747. VSC Change price text to sale color
		return Ui.obtainThemeColor(context, R.attr.skin_hotelPriceSaleColor);
	}

	@Override
	public boolean wantsOtherAppsCrossSellInConfirmationScreen() {
		return true;
	}

	@Override
	public boolean isETPEnabled() {
		return true;
	}

	@Override
	public String getClientShortName() {
		return "vsc";
	}

	public String getAdXKey() {
		//Key not available for VSC for now, so passing blank.
		return "";
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
		return R.drawable.ic_action_bar_logo;
	}

	@Override
	public int updatePOSSpecificActionBarLogo() {
		//ignore
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
		return false;
	}

	@Override
	public String[] getRewardTierAPINames() {
		return null;
	}

	@Override
	public String[] getRewardTierSupportNumberConfigNames() {
		return null;
	}

	@Override
	public String[] getRewardTierSupportEmailConfigNames() {
		return null;
	}

	@Override
	public boolean isCommunicateSectionEnabled() {
		return false;
	}

	@Override
	public PointOfSaleId getUSPointOfSaleId() {
		return null;
	}

	@Override
	public boolean isGoogleAccountChangeEnabled() {
		return false;
	}

	@Override
	public String getOmnitureEventValue(OmnitureTracking.OmnitureEventName key) {
		//Ignore
		return null;
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
		return false;
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
		return "agence" + posUrl;
	}
}
