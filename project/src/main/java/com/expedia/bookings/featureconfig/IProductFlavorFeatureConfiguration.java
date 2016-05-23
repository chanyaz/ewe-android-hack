package com.expedia.bookings.featureconfig;

import java.util.List;

import org.apache.http.message.BasicNameValuePair;
import org.joda.time.DateTime;

import android.content.Context;
import android.view.View;

import com.expedia.bookings.data.HotelFilter;
import com.expedia.bookings.data.pos.PointOfSaleId;
import com.expedia.bookings.server.EndPoint;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.mobiata.android.fragment.AboutSectionFragment;

public interface IProductFlavorFeatureConfiguration {
	String getServerEndpointsConfigurationPath();

	String getPOSConfigurationPath();

	String getAppNameForMobiataPushNameHeader();

	String getAppSupportUrl(Context context);

	boolean isAppCrossSellInActivityShareContentEnabled();

	boolean isAppCrossSellInCarShareContentEnabled();

	String getHostnameForShortUrl();

	boolean shouldDisplayInsuranceDetailsIfAvailableOnItinCard();

	boolean wantsCustomHandlingForLocaleConfiguration();

	int getSearchProgressImageResId();

	int getNotificationIconResourceId();

	int getNotificationIndicatorLEDColor();

	boolean shouldShowBrandLogoOnAccountButton();

	PointOfSaleId getDefaultPOS();

	void contactUsViaWeb(Context context);

	List<BasicNameValuePair> getAdditionalParamsForReviewsRequest();

	boolean shouldUseDotlessDomain(EndPoint endpoint);

	String touchupE3EndpointUrlIfRequired(String e3EndpointUrl);

	View.OnClickListener getInsuranceLinkViewClickListener(final Context context, final String insuranceTermsUrl);

	boolean isLeanPlumEnabled();

	boolean isTuneEnabled();

	boolean isClearPrivateDataInAboutEnabled();

	String getCopyrightLogoUrl(Context context);

	boolean areSocialMediaMenuItemsInAboutEnabled();

	AboutSectionFragment getAboutSectionFragment(Context context);

	boolean isFacebookLoginIntegrationEnabled();

	boolean isFacebookShareIntegrationEnabled();

	boolean isHangTagProgressBarEnabled();

	String formatDateTimeForHotelUserReviews(Context context, DateTime dateTime);

	int getHotelSalePriceTextColorResourceId(Context context);

	boolean wantsOtherAppsCrossSellInConfirmationScreen();

	void setupOtherAppsCrossSellInConfirmationScreen(final Context context, View view);

	boolean isETPEnabled();

	String getClientShortName();

	String getAdXKey();

	boolean isAppSupportUrlEnabled();

	boolean isSigninEnabled();

	boolean isAppIntroEnabled();

	void launchAppIntroScreen(Context context);

	boolean shouldSendSiteIdInRequests();

	String getPhoneCollectionId();

	int getSearchResultDealImageDrawable();

	int getHotelDetailsDealImageDrawable();

	int getCollectionCount();

	/**
	 * return the static image resID to show staic loading image on flight search loading screen
	 * return 0 to enable Plane window view animation on flight search loading screen, currently its enabled only for Samsung and Expedia
	 */
	int getFlightSearchProgressImageResId();

	boolean isLOBIconCenterAligned();

	//return 0 if logo is not required on launch screen
	int getLaunchScreenActionLogo();

	int updatePOSSpecificActionBarLogo();

	String getPOSSpecificBrandName(Context context);

	HotelFilter.Sort getDefaultSort();

	boolean sortByDistanceForCurrentLocation();

	boolean isFacebookTrackingEnabled();

	boolean isAbacusTestEnabled();

	boolean isNewHotelEnabled();

	int getRewardsLayoutId();

	/**
	 * Reward program type
	 *
	 * @return true if points type, false if money/currency type
	 */
	boolean isRewardProgramPointsType();

	/**
	 * Gets the constant strings used by the API to indicate the reward tier. These should be ordered from lowest
	 * tier to highest tier. Tier names are case-insensitive. For example, for Expedia, this would be
	 * <code>["BLUE", "SILVER", "GOLD"]</code>.
	 */
	String[] getRewardTierAPINames();

	/**
	 * Gets the constant strings used in the POS Config files to indicate the support phone number for use by members
	 * at a given reward tier. These should be ordered from lowest tier to highest tier. You may leave an entry in the
	 * array as null to indicate that no tier-specific number applies. These values <strong>are case sensitive</strong>
	 * and must match exactly with how they will show up in the POS Config files. For example, for Expedia, this would
	 * be <code>[null, "supportPhoneNumberSilver", "supportPhoneNumberGold"]</code>.
	 */
	String[] getRewardTierSupportNumberConfigNames();

	/**
	 * Gets the constant strings used in the POS Config files to indicate the support email address for use by members
	 * at a given reward tier. These should be ordered from lowest tier to highest tier. You may leave an entry in the
	 * array as null to indicate that no tier-specific number applies. These values <strong>are case sensitive</strong>
	 * and must match exactly with how they will show up in the POS Config files. For example, for Expedia, this would
	 * be <code>[null, "supportEmailSilver", "supportEmailGold"]</code>.
	 */
	String[] getRewardTierSupportEmailConfigNames();

	boolean isCommunicateSectionEnabled();

	PointOfSaleId getUSPointOfSaleId();

	boolean isEarnMessageOnCheckoutSignInButtonEnabled();

	boolean isGoogleAccountChangeEnabled();

	String getOmnitureEventValue(OmnitureTracking.OmnitureEventName key);

	boolean shouldShowMemberTier();

	boolean shouldShowAirAttach();

	String getSharableFallbackImageURL();

	boolean shouldDisplayItinTrackAppLink();

	boolean shouldSetExistingUserForTune();
}
