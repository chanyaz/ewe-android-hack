package com.expedia.bookings.featureconfig;


import android.support.annotation.NonNull;

import org.joda.time.DateTime;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.data.pos.PointOfSaleId;
import com.expedia.bookings.launch.viewholder.JoinRewardsLaunchViewHolder;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.JodaUtils;

public class FeatureConfiguration extends BaseFeatureConfiguration {
	@Override
	public String getServerEndpointsConfigurationPath() {
		return "ExpediaSharedData/OrbitzServerURLs.json";
	}

	@Override
	public String getPOSConfigurationPath() {
		return "ExpediaSharedData/OrbitzPointOfSaleConfig.json";
	}

	@Override
	public String getAppNameForMobiataPushNameHeader() {
		return "OrbitzBookings";
	}

	@Override
	public String getAppSupportUrl(Context context) {
		return context.getString(R.string.app_support_url);
	}

	@Override
	public String getHostnameForShortUrl() {
		return "s.obtz.co";
	}

	@Override
	public int getNotificationIndicatorLEDColor() {
		return 0xfbc51e;
	}

	@Override
	public PointOfSaleId getDefaultPOS() {
		return PointOfSaleId.ORBITZ;
	}

	@Override
	public String formatDateTimeForHotelUserReviews(Context context, DateTime dateTime) {
		return JodaUtils.formatDateTime(context, dateTime, DateUtils.FORMAT_NUMERIC_DATE | DateUtils.FORMAT_SHOW_YEAR);
	}

	@Override
	public String getClientShortName() {
		return "orbitz";
	}

	@Override
	public int getRewardsLayoutId() {
		return R.layout.bucks_widget_stub;
	}

	@Override
	public RecyclerView.ViewHolder getJoinRewardsViewHolder(@NonNull AppCompatActivity appCompatActivity, @NonNull ViewGroup parent) {
		View view = LayoutInflater.from(appCompatActivity).inflate(R.layout.join_rewards_launch_card, parent, false);
		return new JoinRewardsLaunchViewHolder(view, appCompatActivity);
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
	public PointOfSaleId getUSPointOfSaleId() {
		return PointOfSaleId.ORBITZ;
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
			value = "orbitz";
			break;
		case HOTEL_CHECKOUT_START_REWARDS_REDEEMABLE:
			value = "event119";
			break;
		case REWARD_APPLIED_PERCENTAGE_TEMPLATE:
			value = "orbitz | %d";
			break;
		case NO_REWARDS_USED:
			value = "no orbucks used";
			break;
		case TOTAL_POINTS_BURNED:
			value = "event123";
			break;
		case BRAND_KEY_FOR_OMNITURE:
			value = "Orbitz";
			break;
		}

		return value;
	}

	@Override
	public boolean shouldSetExistingUserForTune() {
		return true;
	}

	@Override
	public boolean isRewardsCardEnabled() {
		return true;
	}

	@Override
	public String getRewardsCardUrl(Context context) {
		return context.getString(R.string.rewards_card_url);
	}
}
