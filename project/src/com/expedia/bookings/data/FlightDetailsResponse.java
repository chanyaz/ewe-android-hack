package com.expedia.bookings.data;

public class FlightDetailsResponse extends Response {

	private FlightTrip mOffer;
	private FlightTrip mOldOffer;
	private Money mPriceChangeAmount;

	private boolean mIsChangeAllowed;
	private boolean mIsEnrouteChangeAllowed;
	private boolean mIsEnrouteRefundAllowed;
	private boolean mIsRefundable;
	private Money mChangePenaltyAmount;

	public boolean hasPriceChanged() {
		return mPriceChangeAmount != null;
	}

	public void setOffer(FlightTrip flightTrip) {
		mOffer = flightTrip;
	}

	public FlightTrip getOffer() {
		return mOffer;
	}

	public void setOldOffer(FlightTrip flightTrip) {
		mOldOffer = flightTrip;
	}

	public FlightTrip getOldOffer() {
		return mOldOffer;
	}

	public void setPriceChangeAmount(Money priceChangeAmount) {
		mPriceChangeAmount = priceChangeAmount;
	}

	public Money getPriceChangeAmount() {
		return mPriceChangeAmount;
	}

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
