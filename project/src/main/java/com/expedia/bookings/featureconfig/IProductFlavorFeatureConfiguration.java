package com.expedia.bookings.featureconfig;

import java.util.List;

import org.apache.http.message.BasicNameValuePair;

import android.content.Context;

import com.expedia.bookings.data.pos.PointOfSaleId;
import com.expedia.bookings.server.ExpediaServices;

public interface IProductFlavorFeatureConfiguration {
	String getServerEndpointsConfigurationPath();

	String getPOSConfigurationPath();

	String getAppNameForMobiataPushNameHeader();

	String getAppSupportUrl(Context context);

	int getCrossSellStringResourceIdForShareEmail();

	Boolean isAppCrossSellInActivityShareContentEnabled();

	Boolean isAppCrossSellInCarShareContentEnabled();

	String getHostnameForShortUrl();

	Boolean shouldDisplayInsuranceDetailsIfAvailableOnItinCard();

	String getActionForLocaleChangeEvent();

	Boolean wantsCustomHandlingForLocaleConfiguration();

	int getSearchProgressImageResId();

	int getNotificationIconResourceId();

	int getNotificationIndicatorLEDColor();

	Boolean shouldShowBrandLogoOnAccountButton();

	int getLoginContainerBackgroundResId(Context context);

	Boolean doesLoginTextViewHaveCompoundDrawables();

	PointOfSaleId getDefaultPOS();

	Boolean isAdXEnabled();

	int getAdXPosIdentifier();

	String getOmnitureReportSuiteIds(Context context);

	String getOmnitureTrackingServer();

	void contactUsViaWeb(Context context);

	int getResIdForErrorHotelServiceFatalFailure();

	int getResIdForErrorBookingSucceededWithErrors();

	List<BasicNameValuePair> getAdditionalParamsForReviewsRequest();

	Boolean shouldUseDotlessDomain(ExpediaServices.EndPoint endpoint);

	String touchupE3EndpointUrlIfRequired(String e3EndpointUrl);
}
