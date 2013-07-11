package com.expedia.bookings.data;

public class SweepstakesResponse extends Response {
	private boolean mSweepstakesPromotionEnabled;

	public boolean getSweepstakesPromotionEnabled() {
		return mSweepstakesPromotionEnabled;
	}

	public void setSweepstakesPromotionEnabled(boolean sweepstakesPromotionEnabled) {
		mSweepstakesPromotionEnabled = sweepstakesPromotionEnabled;
	}
}
