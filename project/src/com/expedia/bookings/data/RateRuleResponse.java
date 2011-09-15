package com.expedia.bookings.data;

public class RateRuleResponse extends Response {

	private RateRules mRateRules;

	public RateRules getRateRules() {
		return mRateRules;
	}

	public void setRateRules(RateRules rateRules) {
		this.mRateRules = rateRules;
	}
}
