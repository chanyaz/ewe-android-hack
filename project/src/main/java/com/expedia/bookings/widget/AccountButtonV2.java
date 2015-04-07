package com.expedia.bookings.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Traveler;

public class AccountButtonV2 extends AccountButton {

	public AccountButtonV2(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public AccountButtonV2(Context context) {
		super(context);
	}

	@Override
	protected void setLogoutContainerBackground(View logoutContainer) {
	}

	@Override
	protected void setRewardsContainerBackground(View rewardsContainer, Traveler.LoyaltyMembershipTier membershipTier) {
		int rewardsBgResId = 0;
		switch (membershipTier) {
		case BLUE:
			rewardsBgResId = R.color.expedia_plus_blue;
			break;
		case SILVER:
			rewardsBgResId = R.color.expedia_plus_silver;
			break;
		case GOLD:
			rewardsBgResId = R.color.expedia_plus_gold;
			break;
		}

		rewardsContainer.setBackgroundResource(rewardsBgResId);
	}
}
