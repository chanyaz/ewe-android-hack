package com.expedia.bookings.featureconfig;

import java.util.List;

import org.apache.http.message.BasicNameValuePair;
import org.joda.time.DateTime;

import android.app.AlertDialog;
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

	int getCrossSellStringResourceIdForShareEmail();

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

	int getLoginContainerBackgroundResId(Context context);

	boolean doesLoginTextViewHaveCompoundDrawables();

	PointOfSaleId getDefaultPOS();

	boolean isAdXEnabled();

	int getAdXPosIdentifier();

	String getOmnitureReportSuiteIds();

	String getOmnitureTrackingServer();

	void contactUsViaWeb(Context context);

	int getResIdForErrorHotelServiceFatalFailure();

	int getResIdForErrorBookingSucceededWithErrors();

	List<BasicNameValuePair> getAdditionalParamsForReviewsRequest();

	boolean shouldUseDotlessDomain(EndPoint endpoint);

	String touchupE3EndpointUrlIfRequired(String e3EndpointUrl);

	View.OnClickListener getInsuranceLinkViewClickListener(final Context context, final String insuranceTermsUrl);

	boolean isLeanPlumEnabled();

	boolean isKahunaEnabled();

	boolean isWeAreHiringInAboutEnabled();

	boolean isClearPrivateDataInAboutEnabled();

	String getCopyrightLogoUrl(Context context);

	boolean areSocialMediaMenuItemsInAboutEnabled();

	AboutSectionFragment getAboutSectionFragment(Context context);

	boolean isLocalExpertEnabled();

	boolean isFacebookLoginIntegrationEnabled();

	boolean isFacebookShareIntegrationEnabled();

	boolean isGoogleWalletPromoEnabled();

	boolean isTrackingWithFlightTrackEnabled();

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

	boolean isAppCrossSellInHotelShareContentEnabled();

	boolean isAppCrossSellInFlightShareContentEnabled();

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

	HotelFilter.Sort getDefaultSort();

	boolean sortByDistanceForCurrentLocation();

	boolean isAbacusTestEnabled();

	AlertDialog getUnsupportedVersionDialog(Context context);
}
