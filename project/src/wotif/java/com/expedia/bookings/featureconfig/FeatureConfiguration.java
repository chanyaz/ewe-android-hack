package com.expedia.bookings.featureconfig;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.format.DateUtils;
import android.view.View;

import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.data.pos.PointOfSaleId;
import com.expedia.bookings.server.EndPoint;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.AboutUtils;
import com.expedia.bookings.utils.JodaUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.vm.AbstractHotelFilterViewModel;
import com.mobiata.android.fragment.AboutSectionFragment;

import org.apache.http.message.BasicNameValuePair;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

public class FeatureConfiguration implements IProductFlavorFeatureConfiguration {
	@Override
	public String getServerEndpointsConfigurationPath() {
		return "ExpediaSharedData/WotifServerURLs.json";
	}

	@Override
	public String getPOSConfigurationPath() {
		return "ExpediaSharedData/WotifPointOfSaleConfig.json";
	}

	@Override
	public String getAppNameForMobiataPushNameHeader() {
		return "WotifBookings";
	}

	@Override
	public String getAppSupportUrl(Context context) {
		return context.getString(R.string.app_support_url_wotif);
	}

	@Override
	public boolean shouldShowEmailUsOnAppSupportWebview() {
		return true;
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
		return "w.wotf.co";
	}

	@Override
	public boolean shouldDisplayInsuranceDetailsIfAvailableOnItinCard() {
		return true;
	}

	@Override
	public boolean wantsCustomHandlingForLocaleConfiguration() {
		return false;
	}

	@Override
	public int getSearchProgressImageResId() {
		return 0;
	}

	@Override
	public int getNotificationIconResourceId() {
		return R.drawable.ic_stat_wotif;
	}

	@Override
	public int getNotificationIndicatorLEDColor() {
		return 0x072b61;
	}

	@Override
	public boolean shouldShowBrandLogoOnAccountButton() {
		return true;
	}

	@Override
	public PointOfSaleId getDefaultPOS() {
		return PointOfSaleId.WOTIF;
	}

	@Override
	public void contactUsViaWeb(Context context) {
		AboutUtils.openWebsite(context, PointOfSale.getPointOfSale().getAppSupportUrl(), true);
	}

	@Override
	public List<BasicNameValuePair> getAdditionalParamsForReviewsRequest() {
		List<BasicNameValuePair> additionalParamsForReviewsRequest = new ArrayList<>();
		additionalParamsForReviewsRequest.add(new BasicNameValuePair("caller", "Wotif"));
		additionalParamsForReviewsRequest.add(new BasicNameValuePair("locale", PointOfSale.getPointOfSale().getLocaleIdentifier()));
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
				Intent viewInsuranceIntent = new Intent(Intent.ACTION_VIEW);
				viewInsuranceIntent.setData(Uri.parse(insuranceTermsUrl));
				context.startActivity(viewInsuranceIntent);
			}
		};
	}

	@Override
	public boolean isLeanPlumEnabled() {
		return true;
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
		return false;
	}

	@Override
	public AboutSectionFragment getAboutSectionFragment(Context context) {
		return null;
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
		return true;
	}

	@Override
	public String getClientShortName() {
		return "wotif";
	}

	@Override
	public String getAdXKey() {
		//Key not available for Wotif for now, so passing blank.
		return "";
	}

	public boolean isAppSupportUrlEnabled() {
		return false;
	}

	@Override
	public boolean isSigninEnabled() {
		return true;
	}

	@Override
	public boolean isLOBIconCenterAligned() {
		return true;
	}

	@Override
	public int getLaunchScreenActionLogo() {
		return R.drawable.ic_ab_wotif_logo;
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
	public int getFlightSearchProgressImageResId() {
		return 0;
	}

	@Override
	public AbstractHotelFilterViewModel.Sort getDefaultSort() {
		return AbstractHotelFilterViewModel.Sort.RECOMMENDED;
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
		return false;
	}

	@Override
	public boolean isNewHotelEnabled() {
		return true;
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
		return true;
	}

	@Override
	public PointOfSaleId getUSPointOfSaleId() {
		return null;
	}

	@Override
	public boolean isEarnMessageOnCheckoutSignInButtonEnabled() {
		return false;
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
	public boolean isWeReHiringEnabled() {
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
		return false;
	}

	@Override
	public boolean useNewLaunchScreen() {
		return true;
	}

	@Override
	public boolean sendEapidToTuneTracking() {
		return false;
	}

	@Override
	public boolean shouldShowPackageIncludesView() {
		return true;
	}

}
