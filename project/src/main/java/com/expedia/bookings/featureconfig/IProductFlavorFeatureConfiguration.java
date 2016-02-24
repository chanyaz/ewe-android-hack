package com.expedia.bookings.featureconfig;

import java.util.List;

import org.apache.http.message.BasicNameValuePair;
import org.joda.time.DateTime;

import android.content.Context;
import android.view.View;

import com.expedia.bookings.data.HotelFilter;
import com.expedia.bookings.data.pos.PointOfSaleId;
import com.expedia.bookings.server.EndPoint;
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

	String getActionForLocaleChangeEvent();

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

	boolean isWeAreHiringInAboutEnabled();

	boolean isClearPrivateDataInAboutEnabled();

	String getCopyrightLogoUrl(Context context);

	boolean areSocialMediaMenuItemsInAboutEnabled();

	AboutSectionFragment getAboutSectionFragment(Context context);

	boolean isFacebookLoginIntegrationEnabled();

	boolean isFacebookShareIntegrationEnabled();

	boolean isHangTagProgressBarEnabled();

	boolean isSettingsInMenuVisible();

	String formatDateTimeForHotelUserReviews(Context context, DateTime dateTime);

	int getHotelSalePriceTextColorResourceId(Context context);

	boolean wantsOtherAppsCrossSellInConfirmationScreen();

	void setupOtherAppsCrossSellInConfirmationScreen(final Context context, View view);

	boolean isETPEnabled();

	String getClientShortName();

	String getAdXKey();

	boolean isAppSupportUrlEnabled();

	boolean isSigninEnabled();

	boolean isItinDisabled();

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
}
