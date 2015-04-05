package com.expedia.bookings.data.lx;

import java.util.List;

import com.expedia.bookings.data.ValidPayment;

public class LXCreateTripResponse {
	public String activityId;
	public String itineraryNumber;
	public String tripId;
	public List<ValidPayment> validFormsOfPayment;
	public LXExpediaRewards expediaRewards;


	public String getRewardsPoints() {
		return expediaRewards != null ? expediaRewards.totalPointsToEarn : "";
	}
}
