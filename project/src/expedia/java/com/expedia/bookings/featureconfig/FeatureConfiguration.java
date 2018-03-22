package com.expedia.bookings.featureconfig;

import org.joda.time.DateTime;

import android.content.Context;
import android.text.format.DateUtils;

import com.expedia.bookings.R;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.data.pos.PointOfSaleId;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.JodaUtils;

public class FeatureConfiguration extends BaseFeatureConfiguration {
	@Override
	public String getServerEndpointsConfigurationPath() {
		return "ExpediaSharedData/ExpediaServerURLs.json";
	}

	@Override
	public String getPOSConfigurationPath() {
		return "ExpediaSharedData/ExpediaPointOfSaleConfig.json";
	}

	@Override
	public String getAppNameForMobiataPushNameHeader() {
		return "ExpediaBookings";
	}

	@Override
	public String getAppSupportUrl(Context context) {
		return context.getString(R.string.app_support_url);
	}

	@Override
	public String getHostnameForShortUrl() {
		return "e.xpda.co";
	}

	@Override
	public boolean wantsCustomHandlingForLocaleConfiguration() {
		return false;
	}

	@Override
	public int getNotificationIndicatorLEDColor() {
		return 0xfbc51e;
	}

	@Override
	public PointOfSaleId getDefaultPOS() {
		return PointOfSaleId.UNITED_KINGDOM;
	}

	@Override
	public String getCopyrightLogoUrl(Context context) {
		return context.getString(R.string.app_copyright_logo_url);
	}

	@Override
	public String formatDateTimeForHotelUserReviews(Context context, DateTime dateTime) {
		return JodaUtils.formatDateTime(context, dateTime, DateUtils.FORMAT_NUMERIC_DATE | DateUtils.FORMAT_SHOW_YEAR);
	}

	@Override
	public String getClientShortName() {
		return "expedia";
	}

	@Override
	public boolean isSplashLoadingAnimationEnabled() {
		return false;
	}

	public boolean isAppIntroEnabled() {
		return true;
	}

	@Override
	public int getRewardsLayoutId() {
		return R.layout.pay_with_points_widget_stub;
	}

	@Override
	public boolean isRewardProgramPointsType() {
		return true;
	}

	private static final String[] rewardTierAPINames = new String[] { "BLUE", "SILVER", "GOLD" };
	private static final String[] rewardTierSupportPhoneNumberConfigNames = new String[] {
		"supportPhoneNumber", "supportPhoneNumberSilver", "supportPhoneNumberGold"
	};
	private static final String[] rewardTierSupportEmailConfigNames = new String[] {
		null, "supportEmailSilver", "supportEmailGold"
	};

	@Override
	public String[] getRewardTierAPINames() {
		return rewardTierAPINames;
	}

	@Override
	public String[] getRewardTierSupportNumberConfigNames() {
		return rewardTierSupportPhoneNumberConfigNames;
	}

	@Override
	public String[] getRewardTierSupportEmailConfigNames() {
		return rewardTierSupportEmailConfigNames;
	}

	@Override
	public PointOfSaleId getUSPointOfSaleId() {
		return PointOfSaleId.UNITED_STATES;
	}

	@Override
	public String getOmnitureEventValue(OmnitureTracking.OmnitureEventName key) {
		String value = null;

		switch (key) {
		case REWARD_PROGRAM_NAME:
			value = "expedia";
			break;
		case HOTEL_CHECKOUT_START_REWARDS_REDEEMABLE:
			value = "event114";
			break;
		case REWARD_APPLIED_PERCENTAGE_TEMPLATE:
			value = "expedia | %d";
			break;
		case NO_REWARDS_USED:
			value = "no points used";
			break;
		case TOTAL_POINTS_BURNED:
			value = "event117";
			break;
		case BRAND_KEY_FOR_OMNITURE:
			value = "Expedia";
			break;
		}

		return value;
	}

	@Override
	public String getSharableFallbackImageURL() {
		return "http://media.expedia.com/mobiata/fb/exp-fb-share.png";
	}

	@Override
	public boolean showUserRewardsEnrollmentCheck() {
		return PointOfSale.getPointOfSale().shouldShowRewards();
	}

	@Override
	public boolean shouldShowPackageIncludesView() {
		return true;
	}

	@Override
	public boolean shouldShowVIPLoyaltyMessage() {
		return true;
	}

	@Override
	public boolean isRecaptchaEnabled() {
		return true;
	}

}
