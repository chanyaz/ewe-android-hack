package com.expedia.bookings.featureconfig;

import java.util.List;

import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.WebViewActivity;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.data.pos.PointOfSaleId;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.utils.AboutUtils;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.fragment.AboutSectionFragment;
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

	public Boolean isAppCrossSellInActivityShareContentEnabled() {
		return true;
	}

	public Boolean isAppCrossSellInCarShareContentEnabled() {
		return true;
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

	public String getOmnitureTrackingServer() {
		return "om.expedia.com";
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

	public View.OnClickListener getInsuranceLinkViewClickListener(final Context context, final String insuranceTermsUrl) {
		return new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				WebViewActivity.IntentBuilder builder = new WebViewActivity.IntentBuilder(context);
				builder.setUrl(insuranceTermsUrl);
				builder.setTheme(R.style.ItineraryTheme);
				builder.setTitle(R.string.insurance);
				builder.setAllowMobileRedirects(false);
				context.startActivity(builder.getIntent());
			}
		};
	}

	public Boolean isLeanPlumEnabled() {
		return true;
	}

	public Boolean isWeAreHiringInAboutEnabled() {
		return true;
	}

	public Boolean isClearPrivateDataInAboutEnabled() {
		return false;
	}

	public String getCopyrightLogoUrl(Context context) {
		return context.getString(Ui.obtainThemeResID(context, R.attr.skin_aboutInfoUrlString));
	}

	public Boolean areSocialMediaMenuItemsInAboutEnabled() {
		return true;
	}

	public AboutSectionFragment getAboutSectionFragment(Context context) {
		return AboutSectionFragment.buildOtherAppsSection(context);
	}

	public Boolean isLocalExpertEnabled() {
		return true;
	}

	public Boolean isFacebookLoginIntegrationEnabled() {
		return true;
	}

	public Boolean isFacebookShareIntegrationEnabled() {
		return true;
	}

	public Boolean isGoogleWalletPromoEnabled() {
		return true;
	}

	public Boolean isTrackingWithFlightTrackEnabled() {
		return true;
	}

	public Boolean isHandTagProgressBarEnabled() {
		return true;
	}

	public Boolean isSettingsInMenuVisible() {
		return true;
	}

	public Boolean wantsCustomDateFormatForUserReviews() {
		return false;
	}

	public String getCustomDateFormatForUserReviews() {
		throw new UnsupportedOperationException("Expedia does not use custom date format");
	}

	public int getHotelSalePriceTextColorResourceId(Context context) {
		return Ui.obtainThemeColor(context, R.attr.skin_hotelPriceStandardColor);
	}

	public Boolean wantsOtherAppsCrossSellInConfirmationScreen() {
		return false;
	}

	public void setupOtherAppsCrossSellInConfirmationScreen(final Context context, View view) {
	}

	public Boolean isETPEnabled() {
		return true;
	}
}
