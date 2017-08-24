package com.expedia.bookings.data.lx;

import java.util.List;

import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.ValidPayment;
import com.expedia.bookings.data.BaseApiResponse;

public class LXCreateTripResponse extends BaseApiResponse {
	public String itineraryNumber;
	public final String tripId;
	public List<ValidPayment> validFormsOfPayment;
	public final LXExpediaRewards expediaRewards;
	public final Money newTotalPrice;
	public final LXProduct lxProduct;

	public String getRewardsPoints() {
		return expediaRewards != null ? expediaRewards.totalPointsToEarn : "";
	}

	// Injected after receiving response; required for communicating price change
	public final Money originalPrice;
}
