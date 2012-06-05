package com.expedia.bookings.data;

import java.util.ArrayList;
import java.util.List;

public class FlightTrip {

	private String mProductKey;

	private List<FlightLeg> mLegs = new ArrayList<FlightLeg>();

	private Money mBaseFare;
	private Money mTotalFare;
	private Money mTaxes;
	private Money mFees;

	private int mSeatsRemaining;

	public String getProductKey() {
		return mProductKey;
	}

	public void setProductKey(String productKey) {
		mProductKey = productKey;
	}

	public void addLeg(FlightLeg leg) {
		mLegs.add(leg);
	}

	public int getLegCount() {
		return mLegs.size();
	}

	public FlightLeg getLeg(int position) {
		return mLegs.get(position);
	}

	public List<FlightLeg> getLegs() {
		return mLegs;
	}

	public Money getBaseFare() {
		return mBaseFare;
	}

	public void setBaseFare(Money baseFare) {
		mBaseFare = baseFare;
	}

	public Money getTotalFare() {
		return mTotalFare;
	}

	public void setTotalFare(Money totalFare) {
		mTotalFare = totalFare;
	}

	public Money getTaxes() {
		return mTaxes;
	}

	public void setTaxes(Money taxes) {
		mTaxes = taxes;
	}

	public Money getFees() {
		return mFees;
	}

	public void setFees(Money fees) {
		mFees = fees;
	}

	public int getSeatsRemaining() {
		return mSeatsRemaining;
	}

	public void setSeatsRemaining(int seatsRemaining) {
		mSeatsRemaining = seatsRemaining;
	}

	////////////////////////////////////////////////////////////////////////
	// More meta retrieval methods

	public boolean hasPricing() {
		return mBaseFare != null && mTotalFare != null && mTaxes != null && mFees != null;
	}
}
