package com.expedia.bookings.featureconfig;

import android.content.Context;

import com.expedia.bookings.data.pos.PointOfSaleId;

public interface IProductFlavorFeatureConfiguration {
	String getServerEndpointsConfigurationPath();
	String getPOSConfigurationPath();
	String getAppNameForMobiataPushNameHeader();
	String getAppSupportUrl(Context context);
	int getCrossSellStringResourceIdForShareEmail();
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
	void contactUsViaWeb(Context context);
	int getResIdForErrorHotelServiceFatalFailure();
	int getResIdForErrorBookingSucceededWithErrors();
}
