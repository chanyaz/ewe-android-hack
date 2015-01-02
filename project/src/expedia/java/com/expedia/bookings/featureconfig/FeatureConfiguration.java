package com.expedia.bookings.featureconfig;

import java.lang.UnsupportedOperationException;
import java.util.List;

import org.apache.http.message.BasicNameValuePair;

import android.content.Context;

import com.expedia.bookings.R;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.data.pos.PointOfSaleId;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.utils.AboutUtils;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.util.AndroidUtils;

public class FeatureConfiguration implements IProductFlavorFeatureConfiguration {
	public String getServerEndpointsConfigurationPath() {
		return "ExpediaSharedData/ExpediaServerURLs.json";
	}

	public String getPOSConfigurationPath() {
		return "ExpediaSharedData/ExpediaPointOfSaleConfig.json";
	}

	public String getAppNameForMobiataPushNameHeader() {
		return "ExpediaBookings";
	}

	public String getAppSupportUrl(Context context) {
		return context.getString(R.string.app_support_url);
	}

	public int getCrossSellStringResourceIdForShareEmail() {
		return R.string.share_template_long_ad;
	}

	public String getHostnameForShortUrl() {
		return "e.xpda.co";
	}

	public Boolean shouldDisplayInsuranceDetailsIfAvailableOnItinCard() {
		return true;
	}

	public String getActionForLocaleChangeEvent() {
		throw new UnsupportedOperationException("Not Required/Implemented for Expedia App");
	}

	public Boolean wantsCustomHandlingForLocaleConfiguration() {
		return false;
	}

	public int getSearchProgressImageResId() {
		return 0;
	}

	public int getNotificationIconResourceId() {
		return R.drawable.ic_stat_expedia;
	}

	public int getNotificationIndicatorLEDColor() {
		return 0xfbc51e;
	}

	public Boolean shouldShowBrandLogoOnAccountButton() {
		return false;
	}

	public int getLoginContainerBackgroundResId(Context context) {
		return Ui.obtainThemeResID(context, R.attr.skin_phoneCheckoutLoginButtonDrawable);
	}

	public Boolean doesLoginTextViewHaveCompoundDrawables() {
		return true;
	}

	public PointOfSaleId getDefaultPOS() {
		return PointOfSaleId.UNITED_KINGDOM;
	}

	public Boolean isAdXEnabled() {
		return true;
	}

	public int getAdXPosIdentifier() {
		return 2601;
	}

	public String getOmnitureReportSuiteIds(Context context) {
		if (AndroidUtils.isRelease(context)) {
			return "expediaglobalapp";
		}
		else {
			return "expediaglobalappdev";
		}
	}

	public void contactUsViaWeb(Context context) {
		AboutUtils.openWebsite(context, PointOfSale.getPointOfSale().getAppSupportUrl(), true);
	}

	public int getResIdForErrorBookingSucceededWithErrors() {
		return R.string.e3_error_checkout_booking_succeeded_with_errors;
	}

	public int getResIdForErrorHotelServiceFatalFailure() {
		return R.string.e3_error_hotel_offers_hotel_service_failure;
	}

	public List<BasicNameValuePair> getAdditionalParamsForReviewsRequest() {
		return null;
	}

	public Boolean shouldUseDotlessDomain(ExpediaServices.EndPoint endpoint) {
		return endpoint != ExpediaServices.EndPoint.PRODUCTION;
	}

	public String touchupE3EndpointUrlIfRequired(String e3EndpointUrl) {
		return e3EndpointUrl;
	}
}
