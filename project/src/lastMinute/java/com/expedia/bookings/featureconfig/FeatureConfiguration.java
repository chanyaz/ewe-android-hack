package com.expedia.bookings.featureconfig;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.message.BasicNameValuePair;
import org.joda.time.DateTime;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.format.DateUtils;
import android.view.View;

import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.expedia.bookings.activity.LastMinuteLocaleChangeReceiver;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.data.pos.PointOfSaleId;
import com.expedia.bookings.server.EndPoint;
import com.expedia.bookings.utils.AboutUtils;
import com.expedia.bookings.utils.JodaUtils;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.fragment.AboutSectionFragment;

public class FeatureConfiguration implements IProductFlavorFeatureConfiguration {
	@Override
	public String getServerEndpointsConfigurationPath() {
		return "ExpediaSharedData/LastMinuteServerURLs.json";
	}

	@Override
	public String getPOSConfigurationPath() {
		return "ExpediaSharedData/LastMinutePointOfSaleConfig.json";
	}

	@Override
	public String getAppNameForMobiataPushNameHeader() {
		return "LMBookings";
	}

	@Override
	public String getAppSupportUrl(Context context) {
		return context.getString(R.string.app_support_url_lastminute);
	}

	@Override
	public boolean isAppCrossSellInActivityShareContentEnabled() {
		return false;
	}

	@Override
	public boolean isAppCrossSellInCarShareContentEnabled() {
		return false;
	}

	@Override
	public String getHostnameForShortUrl() {
		return "l.ast.mn";
	}

	@Override
	public boolean shouldDisplayInsuranceDetailsIfAvailableOnItinCard() {
		return true;
	}

	@Override
	public String getActionForLocaleChangeEvent() {
		return LastMinuteLocaleChangeReceiver.ACTION_LOCALE_CHANGED;
	}

	@Override
	public boolean wantsCustomHandlingForLocaleConfiguration() {
		return false;
	}

	@Override
	public int getSearchProgressImageResId() {
		return R.id.search_progress_image_lm;
	}

	@Override
	public int getNotificationIconResourceId() {
		return R.drawable.ic_stat_lm;
	}

	@Override
	public int getNotificationIndicatorLEDColor() {
		return 0x072b61;
	}

	@Override
	public boolean shouldShowBrandLogoOnAccountButton() {
		return true;
	}

	@Override
	public PointOfSaleId getDefaultPOS() {
		return PointOfSaleId.LASTMINUTE;
	}

	@Override
	public boolean isAdXEnabled() {
		return true;
	}

	@Override
	public int getAdXPosIdentifier() {
		//TODO Add correct AdX Identifier.
		int adXPosIdentifier = 13292;

		if (PointOfSale.getPointOfSale().getPointOfSaleId() == PointOfSaleId.LASTMINUTE_NZ) {
			adXPosIdentifier = 14776;
		}

		return adXPosIdentifier;
	}

	@Override
	public String getOmnitureReportSuiteIds() {
		if (BuildConfig.RELEASE) {
			return "expediaglobalapp";
		}
		else {
			return "expediaglobalappdev";
		}
	}

	@Override
	public String getOmnitureTrackingServer() {
		return "om.expedia.com";
	}

	@Override
	public void contactUsViaWeb(Context context) {
		AboutUtils.openWebsite(context, PointOfSale.getPointOfSale().getAppSupportUrl(), true);
	}

	@Override
	public List<BasicNameValuePair> getAdditionalParamsForReviewsRequest() {
		List<BasicNameValuePair> additionalParamsForReviewsRequest = new ArrayList<BasicNameValuePair>();
		additionalParamsForReviewsRequest.add(new BasicNameValuePair("caller", "LastMinute"));
		additionalParamsForReviewsRequest.add(new BasicNameValuePair("locale", PointOfSale.getPointOfSale().getLocaleIdentifier()));
		return additionalParamsForReviewsRequest;
	}

	@Override
	public boolean shouldUseDotlessDomain(EndPoint endpoint) {
		return endpoint != EndPoint.PRODUCTION;
	}

	@Override
	public String touchupE3EndpointUrlIfRequired(String e3EndpointUrl) {
		return e3EndpointUrl;
	}

	@Override
	public View.OnClickListener getInsuranceLinkViewClickListener(final Context context, final String insuranceTermsUrl) {
		return new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent viewInsuranceIntent = new Intent(Intent.ACTION_VIEW);
				viewInsuranceIntent.setData(Uri.parse(insuranceTermsUrl));
				context.startActivity(viewInsuranceIntent);
			}
		};
	}

	@Override
	public boolean isLeanPlumEnabled() {
		return false;
	}

	@Override
	public boolean isTuneEnabled() {
		return false;
	}

	@Override
	public boolean isWeAreHiringInAboutEnabled() {
		return false;
	}

	@Override
	public boolean isClearPrivateDataInAboutEnabled() {
		return false;
	}

	@Override
	public String getCopyrightLogoUrl(Context context) {
		return PointOfSale.getPointOfSale().getWebsiteUrl();
	}

	@Override
	public boolean areSocialMediaMenuItemsInAboutEnabled() {
		return false;
	}

	@Override
	public AboutSectionFragment getAboutSectionFragment(Context context) {
		return null;
	}

	@Override
	public boolean isFacebookLoginIntegrationEnabled() {
		return false;
	}

	@Override
	public boolean isFacebookShareIntegrationEnabled() {
		return false;
	}

	@Override
	public boolean isGoogleWalletPromoEnabled() {
		return false;
	}

	@Override
	public boolean isHangTagProgressBarEnabled() {
		return false;
	}

	@Override
	public boolean isSettingsInMenuVisible() {
		return true;
	}

	@Override
	public String formatDateTimeForHotelUserReviews(Context context, DateTime dateTime) {
		return JodaUtils.formatDateTime(context, dateTime, DateUtils.FORMAT_NUMERIC_DATE);
	}

	@Override
	public int getHotelSalePriceTextColorResourceId(Context context) {
		return Ui.obtainThemeColor(context, R.attr.skin_hotelPriceStandardColor);
	}

	@Override
	public boolean wantsOtherAppsCrossSellInConfirmationScreen() {
		return false;
	}

	@Override
	public void setupOtherAppsCrossSellInConfirmationScreen(final Context context, View view) {
	}

	@Override
	public boolean isETPEnabled() {
		return true;
	}

	@Override
	public String getClientShortName() {
		return "lastminute";
	}

	@Override
	public String getAdXKey() {
		//TODO Add correct AdX key.
		return "";
	}

	@Override
	public boolean isAppSupportUrlEnabled() {
		return true;
	}

	@Override
	public boolean isSigninEnabled() {
		return true;
	}

	@Override
	public boolean isAppCrossSellInHotelShareContentEnabled() {
		return false;
	}

	@Override
	public boolean isAppCrossSellInFlightShareContentEnabled() {
		return false;
	}
}

