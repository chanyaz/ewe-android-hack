package com.expedia.bookings.data.lx;

import org.jetbrains.annotations.NotNull;

import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.TripResponse;

public class LXCreateTripResponseV2 extends TripResponse {

	public String itineraryNumber;
	public final LXExpediaRewards expediaRewards;
	public Money newTotalPrice;
	public LXProduct lxProduct;

	public String getRewardsPoints() {
		return expediaRewards != null ? expediaRewards.totalPointsToEarn : "";
	}

	// Injected after receiving response; required for communicating price change
	public final Money originalPrice;

	@NotNull
	@Override
	public Money getTripTotalExcludingFee() {
		return null;
	}

	@NotNull
	@Override
	public Money tripTotalPayableIncludingFeeIfZeroPayableByPoints() {
		return null;
	}

	@Override
	public boolean isCardDetailsRequiredForBooking() {
		return true;
	}

	@NotNull
	@Override
	public Money getOldPrice() {
		return originalPrice;
	}
}
