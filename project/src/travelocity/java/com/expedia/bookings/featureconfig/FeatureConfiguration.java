package com.expedia.bookings.featureconfig;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.message.BasicNameValuePair;

import android.content.Context;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.TravelocityLocaleChangeReceiver;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.data.pos.PointOfSaleId;
import com.expedia.bookings.utils.AboutUtils;
import com.mobiata.android.util.AndroidUtils;

public class FeatureConfiguration implements IProductFlavorFeatureConfiguration {
	public String getServerEndpointsConfigurationPath() {
		return "ExpediaSharedData/TVLYServerURLs.json";
	}

	public String getPOSConfigurationPath() {
		return "ExpediaSharedData/TravelocityPointOfSaleConfig.json";
	}

	public String getAppNameForMobiataPushNameHeader() {
		return "TravelocityBookings";
	}

	public String getAppSupportUrl(Context context) {
		return PointOfSale.getPointOfSale().getAppSupportUrl();
	}

	public int getCrossSellStringResourceIdForShareEmail() {
		return R.string.share_template_long_ad_tvly;
	}

	public String getHostnameForShortUrl() {
		return "t.tvly.co";
	}

	public Boolean shouldDisplayInsuranceDetailsIfAvailableOnItinCard() {
		return true;
	}

	public String getActionForLocaleChangeEvent() {
		return TravelocityLocaleChangeReceiver.ACTION_LOCALE_CHANGED;
	}

	public Boolean wantsCustomHandlingForLocaleConfiguration() {
		return true;
	}

	public int getSearchProgressImageResId() {
		return R.id.search_progress_image_tvly;
	}

	public int getNotificationIconResourceId() {
		return R.drawable.ic_stat_travelocity;
	}

	public int getNotificationIndicatorLEDColor() {
		return 0x072b61;
	}

	public Boolean shouldShowBrandLogoOnAccountButton() {
		return false;
	}

	public int getLoginContainerBackgroundResId(Context context) {
		return R.drawable.btn_login_hotels;
	}

	public Boolean doesLoginTextViewHaveCompoundDrawables() {
		return true;
	}

	public PointOfSaleId getDefaultPOS() {
		return PointOfSaleId.TRAVELOCITY;
	}

	public Boolean isAdXEnabled() {
		return true;
	}

	public int getAdXPosIdentifier() {
		int adXPosIdentifier = 13292;

		// For Travelocity canada setting a separate ID.
		if (PointOfSale.getPointOfSale().getPointOfSaleId() == PointOfSaleId.TRAVELOCITY_CA) {
			adXPosIdentifier = 14776;
		}

		return adXPosIdentifier;
	}

	public String getOmnitureReportSuiteIds(Context context) {
		if (AndroidUtils.isRelease(context)) {
			return "tvlglobalapp";
		}
		else {
			return "tvlglobalappdev";
		}
	}

	public void contactUsViaWeb(Context context) {
		AboutUtils.openWebsite(context, PointOfSale.getPointOfSale().getAppSupportUrl(), true);
	}

	public int getResIdForErrorBookingSucceededWithErrors() {
		return R.string.e3_error_checkout_booking_succeeded_with_errors_tvly;
	}

	public int getResIdForErrorHotelServiceFatalFailure() {
		return R.string.e3_error_hotel_offers_hotel_service_failure_tvly;
	}

	public List<BasicNameValuePair> getAdditionalParamsForReviewsRequest() {
		List<BasicNameValuePair> additionalParamsForReviewsRequest = new ArrayList<BasicNameValuePair>();
		additionalParamsForReviewsRequest.add(new BasicNameValuePair("origin", "TRAVELOCITY"));
		return additionalParamsForReviewsRequest;
	}
}
