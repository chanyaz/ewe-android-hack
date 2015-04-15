package com.expedia.bookings.data.lx;

import java.util.List;

import com.expedia.bookings.data.ValidPayment;
import com.expedia.bookings.data.cars.BaseApiResponse;

public class LXCreateTripResponse extends BaseApiResponse {
	public String itineraryNumber;
	public String tripId;
	public List<ValidPayment> validFormsOfPayment;
	public LXExpediaRewards expediaRewards;
	public LXProduct lxProduct;

	public String getRewardsPoints() {
		return expediaRewards != null ? expediaRewards.totalPointsToEarn : "";
	}
}
