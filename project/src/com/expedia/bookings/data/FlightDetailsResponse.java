package com.expedia.bookings.data;

public class FlightDetailsResponse extends Response {

	private boolean mIsChangeAllowed;
	private boolean mIsEnrouteChangeAllowed;
	private boolean mIsEnrouteRefundAllowed;
	private boolean mIsRefundable;
	private Money mChangePenaltyAmount;

	public boolean isIsChangeAllowed() {
		return mIsChangeAllowed;
	}

	public void setIsChangeAllowed(boolean isChangeAllowed) {
		mIsChangeAllowed = isChangeAllowed;
	}

	public boolean isIsEnrouteChangeAllowed() {
		return mIsEnrouteChangeAllowed;
	}

	public void setIsEnrouteChangeAllowed(boolean isEnrouteChangeAllowed) {
		mIsEnrouteChangeAllowed = isEnrouteChangeAllowed;
	}

	public boolean isIsEnrouteRefundAllowed() {
		return mIsEnrouteRefundAllowed;
	}

	public void setIsEnrouteRefundAllowed(boolean isEnrouteRefundAllowed) {
		mIsEnrouteRefundAllowed = isEnrouteRefundAllowed;
	}

	public boolean isIsRefundable() {
		return mIsRefundable;
	}

	public void setIsRefundable(boolean isRefundable) {
		mIsRefundable = isRefundable;
	}

	public Money getChangePenaltyAmount() {
		return mChangePenaltyAmount;
	}

	public void setChangePenaltyAmount(Money changePenaltyAmount) {
		mChangePenaltyAmount = changePenaltyAmount;
	}

}
