package com.expedia.bookings.featureconfig;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.message.BasicNameValuePair;

import android.content.Context;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.VSCLocaleChangeReceiver;
import com.expedia.bookings.data.pos.PointOfSaleId;
import com.expedia.bookings.utils.AboutUtils;
import com.mobiata.android.util.AndroidUtils;

public class FeatureConfiguration implements IProductFlavorFeatureConfiguration {
	public String getServerEndpointsConfigurationPath() {
		return "ExpediaSharedData/VSCServerURLs.json";
	}

	public String getPOSConfigurationPath() {
		return "ExpediaSharedData/VSCPointOfSaleConfig.json";
	}

	public String getAppNameForMobiataPushNameHeader() {
		return "VSCBookings";
	}

	public String getAppSupportUrl(Context context) {
		return context.getString(R.string.app_support_url_vsc);
	}

	public int getCrossSellStringResourceIdForShareEmail() {
		return R.string.share_template_long_ad_vsc;
	}

	public String getHostnameForShortUrl() {
		return "v.vygs.co";
	}

	public Boolean shouldDisplayInsuranceDetailsIfAvailableOnItinCard() {
		return true;
	}

	public String getActionForLocaleChangeEvent() {
		return VSCLocaleChangeReceiver.ACTION_LOCALE_CHANGED;
	}

	public Boolean wantsCustomHandlingForLocaleConfiguration() {
		return true;
	}

	public int getSearchProgressImageResId() {
		return 0;
	}

	public int getNotificationIconResourceId() {
		return R.drawable.ic_stat_vsc;
	}

	public int getNotificationIndicatorLEDColor() {
		return 0xfbc51e;
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
		return PointOfSaleId.VSC;
	}

	public Boolean isAdXEnabled() {
		return false;
	}

	public int getAdXPosIdentifier() {
		throw new UnsupportedOperationException("AdX not enabled for VSC.");
	}

	public String getOmnitureReportSuiteIds(Context context) {
		if (AndroidUtils.isRelease(context)) {
			return "expediaglobalapp" + ",expedia7androidapp";
		}
		else {
			return "expediaglobalappdev" + ",expedia7androidappdev";
		}
	}

	public void contactUsViaWeb(Context context) {
		AboutUtils.openWebsite(context, "http://voyages-sncf.mobi/aide-appli-2/aide-appli-hotel/pagecontactandroid.html", false, false);
	}

	public int getResIdForErrorBookingSucceededWithErrors() {
		return R.string.e3_error_checkout_booking_succeeded_with_errors_vsc;
	}

	public int getResIdForErrorHotelServiceFatalFailure() {
		return R.string.e3_error_hotel_offers_hotel_service_failure_vsc;
	}

	public List<BasicNameValuePair> getAdditionalParamsForReviewsRequest() {
		List<BasicNameValuePair> additionalParamsForReviewsRequest = new ArrayList<BasicNameValuePair>();
		additionalParamsForReviewsRequest.add(new BasicNameValuePair("origin", "VSC"));
		return additionalParamsForReviewsRequest;
	}
}
