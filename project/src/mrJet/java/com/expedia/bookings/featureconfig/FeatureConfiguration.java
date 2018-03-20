package com.expedia.bookings.featureconfig;

import org.joda.time.DateTime;

import android.content.Context;
import android.text.format.DateUtils;

import com.expedia.bookings.R;
import com.expedia.bookings.data.pos.PointOfSaleId;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.JodaUtils;

public class FeatureConfiguration extends BaseFeatureConfiguration {
	@Override
	public String getServerEndpointsConfigurationPath() {
		return "ExpediaSharedData/MrJetServerURLs.json";
	}

	@Override
	public String getPOSConfigurationPath() {
		return "ExpediaSharedData/MrJetPointOfSaleConfig.json";
	}

	@Override
	public String getAppNameForMobiataPushNameHeader() {
		return "MrJetBookings";
	}

	@Override
	public String getAppSupportUrl(Context context) {
		return context.getString(R.string.app_support_url);
	}

	@Override
	public String getHostnameForShortUrl() {
		return "s.mret.co";
	}

	@Override
	public int getNotificationIndicatorLEDColor() {
		return 0xfbc51e;
	}

	@Override
	public PointOfSaleId getDefaultPOS() {
		return PointOfSaleId.MRJET_SWEDEN;
	}

	@Override
	public String formatDateTimeForHotelUserReviews(Context context, DateTime dateTime) {
		return JodaUtils.formatDateTime(context, dateTime, DateUtils.FORMAT_NUMERIC_DATE | DateUtils.FORMAT_SHOW_YEAR);
	}

	@Override
	public String getClientShortName() {
		return "mrjet";
	}

	@Override
	public int getRewardsLayoutId() {
		return R.layout.bucks_widget_stub;
	}

	private static final String[] rewardTierAPINames = new String[] { "SILVER", "GOLD", "PLATINUM" };
	private static final String[] rewardTierSupportPhoneNumberConfigNames = new String[] {
			"supportPhoneNumberSilver", "supportPhoneNumberGold", "supportPhoneNumberPlatinum"
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
	public boolean isGoogleAccountChangeEnabled() {
		return true;
	}

	@Override
	public String getOmnitureEventValue(OmnitureTracking.OmnitureEventName key) {
		String value = null;

		switch (key) {
		case REWARD_PROGRAM_NAME:
			value = "mrjet";
			break;
		case HOTEL_CHECKOUT_START_REWARDS_REDEEMABLE:
			value = "event119";
			break;
		case REWARD_APPLIED_PERCENTAGE_TEMPLATE:
			value = "mrjet | %d";
			break;
		case NO_REWARDS_USED:
			value = "no mrjetcash used";
			break;
		case TOTAL_POINTS_BURNED:
			value = "event123";
			break;
		case BRAND_KEY_FOR_OMNITURE:
			value = "Mrjet";
			break;
		}

		return value;
	}

	@Override
	public boolean shouldDisplayItinTrackAppLink() {
		return false;
	}

	@Override
	public boolean shouldShowItinShare() {
		return false; // TODO : Add "flightshare" deeplink & shortcut when itin share is enabled
	}
}
