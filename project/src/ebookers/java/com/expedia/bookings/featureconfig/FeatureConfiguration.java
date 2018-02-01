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
		return "ExpediaSharedData/EbookersServerURLs.json";
	}

	@Override
	public String getPOSConfigurationPath() {
		return "ExpediaSharedData/EbookersPointOfSaleConfig.json";
	}

	@Override
	public String getAppNameForMobiataPushNameHeader() {
		return "EbookersBookings";
	}

	@Override
	public String getAppSupportUrl(Context context) {
		return context.getString(R.string.app_support_url);
	}

	@Override
	public String getHostnameForShortUrl() {
		return "e.bukr.us";
	}

	@Override
	public int getNotificationIconResourceId() {
		return R.drawable.ic_stat;
	}

	@Override
	public int getNotificationIndicatorLEDColor() {
		return 0xfbc51e;
	}

	@Override
	public PointOfSaleId getDefaultPOS() {
		return PointOfSaleId.EBOOKERS_UNITED_KINGDOM;
	}

	@Override
	public String formatDateTimeForHotelUserReviews(Context context, DateTime dateTime) {
		return JodaUtils.formatDateTime(context, dateTime, DateUtils.FORMAT_NUMERIC_DATE | DateUtils.FORMAT_SHOW_YEAR);
	}

	@Override
	public String getClientShortName() {
		return "ebookers";
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
		switch (key) {
		case REWARD_PROGRAM_NAME:
			return "ebookers";
		case HOTEL_CHECKOUT_START_REWARDS_REDEEMABLE:
			return "event119";
		case REWARD_APPLIED_PERCENTAGE_TEMPLATE:
			return "ebookers | %d";
		case NO_REWARDS_USED:
			return "no bonus+ used";
		case TOTAL_POINTS_BURNED:
			return "event123";
		case BRAND_KEY_FOR_OMNITURE:
			return "EBookers";
		}
		//It should not be the case
		throw new IllegalArgumentException("Unknown enum value");
	}

	@Override
	public boolean shouldDisplayItinTrackAppLink() {
		return false;
	}

	@Override
	public boolean shouldShowItinShare() {
		return false; // TODO : Add "flightshare" deeplink & shortcut when itin share is enabled
	}

	@Override
	public boolean shouldShowPackageIncludesView() {
		return false;
	}
}
