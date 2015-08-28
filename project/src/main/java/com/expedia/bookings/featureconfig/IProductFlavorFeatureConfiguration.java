package com.expedia.bookings.featureconfig;

import java.util.List;

import org.apache.http.message.BasicNameValuePair;
import org.joda.time.DateTime;

import android.content.Context;
import android.view.View;

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

	String getOmnitureReportSuiteIds();

	String getOmnitureTrackingServer();

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

	boolean isGoogleWalletPromoEnabled();

	boolean isHangTagProgressBarEnabled();

	boolean isSettingsInMenuVisible();

	String formatDateTimeForHotelUserReviews(Context context, DateTime dateTime);

	int getHotelSalePriceTextColorResourceId(Context context);

	boolean wantsOtherAppsCrossSellInConfirmationScreen();

	void setupOtherAppsCrossSellInConfirmationScreen(final Context context, View view);

	boolean isETPEnabled();

	String getClientShortName();

	boolean isAppSupportUrlEnabled();

	boolean isSigninEnabled();

	boolean isAppCrossSellInHotelShareContentEnabled();

	boolean isAppCrossSellInFlightShareContentEnabled();
}
