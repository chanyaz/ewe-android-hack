package com.expedia.bookings.data;

import android.content.Context;

import com.expedia.bookings.R;

public class FlightTrip {

	private String mProductKey;

	private FlightLeg mInboundLeg;

	private FlightLeg mOutboundLeg;

	private Money mBaseFare;
	private Money mTotalFare;
	private Money mTaxes;
	private Money mFees;

	public String getProductKey() {
		return mProductKey;
	}

	public void setProductKey(String productKey) {
		mProductKey = productKey;
	}

	public FlightLeg getInboundLeg() {
		return mInboundLeg;
	}

	public void setInboundLeg(FlightLeg inboundLeg) {
		mInboundLeg = inboundLeg;
	}

	public FlightLeg getOutboundLeg() {
		return mOutboundLeg;
	}

	public void setOutboundLeg(FlightLeg outboundLeg) {
		mOutboundLeg = outboundLeg;
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

	////////////////////////////////////////////////////////////////////////
	// More meta retrieval methods

	public boolean hasPricing() {
		return mBaseFare != null && mTotalFare != null && mTaxes != null && mFees != null;
	}

	public String getAirlineName(Context context) {
		if (mInboundLeg.hasMultipleAirlines() || mOutboundLeg.hasMultipleAirlines()) {
			return context.getString(R.string.multiple_airlines);
		}
		else {
			return mInboundLeg.getAirlineName();
		}
	}
}
