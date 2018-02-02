package com.expedia.bookings.featureconfig;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.message.BasicNameValuePair;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import android.content.Context;

import com.expedia.bookings.R;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.data.pos.PointOfSaleId;
import com.expedia.bookings.utils.AboutUtils;

public class FeatureConfiguration extends BaseFeatureConfiguration {
	@Override
	public String getServerEndpointsConfigurationPath() {
		return "ExpediaSharedData/VSCServerURLs.json";
	}

	@Override
	public String getPOSConfigurationPath() {
		return "ExpediaSharedData/VSCPointOfSaleConfig.json";
	}

	@Override
	public String getAppNameForMobiataPushNameHeader() {
		return "VSCBookings";
	}

	@Override
	public String getAppSupportUrl(Context context) {
		return PointOfSale.getPointOfSale().getBookingSupportUrl();
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
		return "v.vygs.co";
	}

	@Override
	public int getNotificationIconResourceId() {
		return R.drawable.ic_stat_vsc;
	}

	@Override
	public int getNotificationIndicatorLEDColor() {
		return 0xfbc51e;
	}

	@Override
	public PointOfSaleId getDefaultPOS() {
		return PointOfSaleId.VSC;
	}

	@Override
	public void contactUsViaWeb(Context context) {
		AboutUtils.openWebsite(context, "http://voyages-sncf.mobi/aide-appli-2/aide-appli-hotel/pagecontactandroid.html", false);
	}

	@Override
	public List<BasicNameValuePair> getAdditionalParamsForReviewsRequest() {
		List<BasicNameValuePair> additionalParamsForReviewsRequest = new ArrayList<>();
		additionalParamsForReviewsRequest.add(new BasicNameValuePair("origin", "VSC"));
		return additionalParamsForReviewsRequest;
	}

	@Override
	public boolean isTuneEnabled() {
		return false;
	}

	@Override
	public String getCopyrightLogoUrl(Context context) {
		return context.getString(R.string.app_copyright_logo_url);
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
	public String formatDateTimeForHotelUserReviews(Context context, DateTime dateTime) {
		//1608. VSC - Apparently since we are forcing FR locale the dateUtils is not formatting appropriately.
		//Ugly hack to ensure European date format.
		String customDateFormat = "dd/MM/yyyy";
		DateTimeFormatter dtf = DateTimeFormat.forPattern(customDateFormat);
		return dtf.print(dateTime);
	}

	@Override
	public String getClientShortName() {
		return "vsc";
	}

	@Override
	public int getLaunchScreenActionLogo() {
		return R.drawable.ic_action_bar_logo;
	}

	@Override
	public boolean isFacebookTrackingEnabled() {
		return false;
	}

	@Override
	public boolean isCommunicateSectionEnabled() {
		return false;
	}

	@Override
	public boolean isRateOurAppEnabled() {
		return false;
	}

	@Override
	public boolean showUserRewardsEnrollmentCheck() {
		return false;
	}

	@Override
	public boolean showHotelLoyaltyEarnMessage() {
		return false;
	}

	@Override
	public boolean shouldShowUserReview() {
		return false;
	}

	@Override
	public String getPosURLToShow(String posUrl) {
		return "agence" + posUrl;
	}

	@Override
	public boolean isCarnivalEnabled() {
		return false;
	}

}
