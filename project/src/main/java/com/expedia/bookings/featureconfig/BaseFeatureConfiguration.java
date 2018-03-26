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
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.data.pos.PointOfSaleId;
import com.expedia.bookings.server.EndPoint;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.AboutUtils;
import com.expedia.bookings.utils.JodaUtils;

public abstract class BaseFeatureConfiguration {

	public boolean isAppCrossSellInActivityShareContentEnabled() {
		return true;
	}

	public boolean isAppCrossSellInCarShareContentEnabled() {
		return true;
	}

	public boolean shouldDisplayInsuranceDetailsIfAvailableOnItinCard() {
		return true;
	}

	public boolean wantsCustomHandlingForLocaleConfiguration() {
		return true;
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
			public void onClick(View view) {
				WebViewActivity.IntentBuilder builder = new WebViewActivity.IntentBuilder(context);
				builder.setUrl(insuranceTermsUrl);
				builder.setTitle(R.string.insurance);
				builder.setAllowMobileRedirects(false);
				context.startActivity(builder.getIntent());
			}
		};
	}

	public boolean isTuneEnabled() {
		return true;
	}

	public boolean isFacebookLoginIntegrationEnabled() {
		return true;
	}

	public boolean isFacebookShareIntegrationEnabled() {
		return true;
	}

	public String formatDateTimeForHotelUserReviews(Context context, DateTime dateTime) {
		return JodaUtils.formatDateTime(context, dateTime, DateUtils.FORMAT_NUMERIC_DATE);
	}

	public boolean isSplashLoadingAnimationEnabled() {
		return false;
	}

	public boolean isAppIntroEnabled() {
		return false;
	}

	//return 0 if logo is not required on launch screen
	public int getLaunchScreenActionLogo() {
		return 0;
	}

	public String getPOSSpecificBrandName(Context context) {
		return BuildConfig.brand;
	}

	public boolean isFacebookTrackingEnabled() {
		return true;
	}

	public boolean isAbacusTestEnabled() {
		return true;
	}

	public int getRewardsLayoutId() {
		return 0;
	}

	/**
	 * Reward program type
	 *
	 * @return true if points type, false if money/currency type
	 */
	public boolean isRewardProgramPointsType() {
		return false;
	}

	/**
	 * Gets the constant strings used by the API to indicate the reward tier. These should be ordered from lowest
	 * tier to highest tier. Tier names are case-insensitive. For example, for Expedia, this would be
	 * <code>["BLUE", "SILVER", "GOLD"]</code>.
	 */
	public String[] getRewardTierAPINames() {
		return null;
	}

	/**
	 * Gets the constant strings used in the POS Config files to indicate the support phone number for use by members
	 * at a given reward tier. These should be ordered from lowest tier to highest tier. You may leave an entry in the
	 * array as null to indicate that no tier-specific number applies. These values <strong>are case sensitive</strong>
	 * and must match exactly with how they will show up in the POS Config files. For example, for Expedia, this would
	 * be <code>[null, "supportPhoneNumberSilver", "supportPhoneNumberGold"]</code>.
	 */
	public String[] getRewardTierSupportNumberConfigNames() {
		return null;
	}

	/**
	 * Gets the constant strings used in the POS Config files to indicate the support email address for use by members
	 * at a given reward tier. These should be ordered from lowest tier to highest tier. You may leave an entry in the
	 * array as null to indicate that no tier-specific number applies. These values <strong>are case sensitive</strong>
	 * and must match exactly with how they will show up in the POS Config files. For example, for Expedia, this would
	 * be <code>[null, "supportEmailSilver", "supportEmailGold"]</code>.
	 */
	public String[] getRewardTierSupportEmailConfigNames() {
		return null;
	}

	public boolean isCommunicateSectionEnabled() {
		return true;
	}

	public PointOfSaleId getUSPointOfSaleId() {
		return null;
	}

	public boolean isGoogleAccountChangeEnabled() {
		return false;
	}

	public String getOmnitureEventValue(OmnitureTracking.OmnitureEventName key) {
		//Brands with rewards program to override this method
		return null;
	}

	public boolean shouldShowMemberTier() {
		return true;
	}

	public String getSharableFallbackImageURL() {
		return null;
	}

	public boolean shouldDisplayItinTrackAppLink() {
		return true;
	}

	public boolean shouldSetExistingUserForTune() {
		return false;
	}

	public boolean shouldShowItinShare() {
		return true;
	}

	public boolean isRateOurAppEnabled() {
		return true;
	}

	public boolean isRewardsCardEnabled() {
		return false;
	}

	public String getRewardsCardUrl(Context context) {
		return null;
	}

	/**
	 * Provide loyalty enrollment based on user's preference.
	 *
	 * @return
	 */
	public boolean showUserRewardsEnrollmentCheck() {
		return true;
	}

	public boolean sendEapidToTuneTracking() {
		return false;
	}

	public boolean shouldShowPackageIncludesView() {
		return true;
	}

	public boolean showHotelLoyaltyEarnMessage() {
		return true;
	}

	public boolean shouldShowUserReview() {
		return true;
	}

	public boolean shouldShowVIPLoyaltyMessage() {
		return false;
	}

	public int getPOSSpecificBrandLogo() {
		return R.drawable.app_copyright_logo;
	}

	public String getPosURLToShow(String posUrl) {
		return posUrl;
	}

	public boolean isFirebaseEnabled() {
		return false;
	}

	public boolean isCarnivalEnabled() {
		return true;
	}

	public boolean isRecaptchaEnabled() {
		return false;
	}

	public void contactUsViaWeb(Context context) {
		AboutUtils.openWebsite(context, PointOfSale.getPointOfSale().getBookingSupportUrl(), true);
	}

	public List<BasicNameValuePair> getAdditionalParamsForReviewsRequest() {
		List<BasicNameValuePair> additionalParamsForReviewsRequest = new ArrayList<>();
		additionalParamsForReviewsRequest.add(
			new BasicNameValuePair("locale", PointOfSale.getPointOfSale().getLocaleIdentifier()));
		return additionalParamsForReviewsRequest;
	}

	public String getCopyrightLogoUrl(Context context) {
		return PointOfSale.getPointOfSale().getWebsiteUrl();
	}

	/**
	 * This flag is only meant for AAG as AB tests cannot be enabled for it.
	 * Rest all brands completely depend on the Abacus Server for the configuration.
	 * @return Whether MID API is forcefully enabled for Packages.
	 */
	public boolean shouldForceEnableMIDAPIForPackages() {
		return false;
	}

	public abstract String getServerEndpointsConfigurationPath();

	public abstract String getPOSConfigurationPath();

	public abstract String getAppNameForMobiataPushNameHeader();

	public abstract String getAppSupportUrl(Context context);

	public abstract String getHostnameForShortUrl();

	public abstract int getNotificationIndicatorLEDColor();

	public abstract PointOfSaleId getDefaultPOS();

	public abstract String getClientShortName();
}
