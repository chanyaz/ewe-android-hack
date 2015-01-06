package com.expedia.bookings.featureconfig;

import java.lang.UnsupportedOperationException;
import java.util.List;

import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.AirAsiaGoLocaleChangeReceiver;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.data.pos.PointOfSaleId;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.utils.AboutUtils;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.fragment.AboutSectionFragment;
import com.mobiata.android.util.AndroidUtils;

public class FeatureConfiguration implements IProductFlavorFeatureConfiguration {
	public String getServerEndpointsConfigurationPath() {
		return "ExpediaSharedData/AirAsiaGoServerURLs.json";
	}

	public String getPOSConfigurationPath() {
		return "ExpediaSharedData/AirAsiaGoPointOfSaleConfig.json";
	}

	public String getAppNameForMobiataPushNameHeader() {
		return "AAGBookings";
	}

	public String getAppSupportUrl(Context context) {
		return context.getString(R.string.app_support_url_aag);
	}

	public int getCrossSellStringResourceIdForShareEmail() {
		return R.string.share_template_long_ad_aag;
	}

	public Boolean isAppCrossSellInActivityShareContentEnabled() {
		return false;
	}

	public Boolean isAppCrossSellInCarShareContentEnabled() {
		return false;
	}

	public String getHostnameForShortUrl() {
		return "a.aago.co";
	}

	public Boolean shouldDisplayInsuranceDetailsIfAvailableOnItinCard() {
		return false;
	}

	public String getActionForLocaleChangeEvent() {
		return AirAsiaGoLocaleChangeReceiver.ACTION_LOCALE_CHANGED;
	}

	public Boolean wantsCustomHandlingForLocaleConfiguration() {
		return true;
	}

	public int getSearchProgressImageResId() {
		return R.id.search_progress_image_aag;
	}

	public int getNotificationIconResourceId() {
		return R.drawable.ic_stat_aag;
	}

	public int getNotificationIndicatorLEDColor() {
		return 0xfbc51e;
	}

	public Boolean shouldShowBrandLogoOnAccountButton() {
		return true;
	}

	public int getLoginContainerBackgroundResId(Context context) {
		return Ui.obtainThemeResID(context, R.attr.skin_phoneCheckoutLoginButtonDrawable);
	}

	public Boolean doesLoginTextViewHaveCompoundDrawables() {
		return false;
	}

	public PointOfSaleId getDefaultPOS() {
		return PointOfSaleId.AIRASIAGO_MALAYSIA;
	}

	public Boolean isAdXEnabled() {
		return true;
	}

	public int getAdXPosIdentifier() {
		int adXPosIdentifier = 6259;

		// For AirAsiaGo Thailand setting a separate ID.
		if (PointOfSale.getPointOfSale().getTwoLetterCountryCode().toLowerCase().equals("th")) {
			adXPosIdentifier = 6258;
		}

		return adXPosIdentifier;
	}

	public String getOmnitureReportSuiteIds(Context context) {
		if (AndroidUtils.isRelease(context)) {
			return "expediaglobalapp";
		}
		else {
			return "expediaglobalappdev";
		}
	}

	public String getOmnitureTrackingServer() {
		return "om.expedia.com";
	}

	public void contactUsViaWeb(Context context) {
		AboutUtils.openWebsite(context, PointOfSale.getPointOfSale().getAppSupportUrl(), true);
	}

	public int getResIdForErrorBookingSucceededWithErrors() {
		return R.string.e3_error_checkout_booking_succeeded_with_errors_aag;
	}

	public int getResIdForErrorHotelServiceFatalFailure() {
		return R.string.e3_error_hotel_offers_hotel_service_failure_aag;
	}

	public List<BasicNameValuePair> getAdditionalParamsForReviewsRequest() {
		return null;
	}

	public Boolean shouldUseDotlessDomain(ExpediaServices.EndPoint endpoint) {
		return endpoint != ExpediaServices.EndPoint.PRODUCTION;
	}

	public String touchupE3EndpointUrlIfRequired(String e3EndpointUrl) {
		//Domain name for AAG Thailand is thailand.airasiago.com, so removing www from URL.
		if(PointOfSale.getPointOfSale().getPointOfSaleId().equals(PointOfSaleId.AIRASIAGO_THAILAND)) {
			e3EndpointUrl = e3EndpointUrl.replaceFirst("w{3}\\.?", "");
		}
		return e3EndpointUrl;
	}

	public View.OnClickListener getInsuranceLinkViewClickListener(final Context context, final String insuranceTermsUrl) {
		throw new UnsupportedOperationException("Insurance not supported on Air Asia Go.");
	}

	public Boolean isLeanPlumEnabled() {
		return false;
	}

	public Boolean isWeAreHiringInAboutEnabled() {
		return false;
	}

	public Boolean isClearPrivateDataInAboutEnabled() {
		return false;
	}

	public String getCopyrightLogoUrl(Context context) {
		return PointOfSale.getPointOfSale().getWebsiteUrl();
	}

	public Boolean areSocialMediaMenuItemsInAboutEnabled() {
		return false;
	}

	public AboutSectionFragment getAboutSectionFragment(Context context) {
		return null;
	}

	public Boolean isLocalExpertEnabled() {
		return false;
	}

	public Boolean isFacebookLoginIntegrationEnabled() {
		return false;
	}

	public Boolean isFacebookShareIntegrationEnabled() {
		return false;
	}

	public Boolean isGoogleWalletPromoEnabled() {
		return false;
	}

	public Boolean isTrackingWithFlightTrackEnabled() {
		return false;
	}

	public Boolean isHandTagProgressBarEnabled() {
		return false;
	}
}
