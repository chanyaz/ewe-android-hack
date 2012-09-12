package com.expedia.bookings.data;

public class FlightCheckoutResponse extends Response {

	private String mOrderId;

	private Money mTotalCharges;

	public void setOrderId(String orderId) {
		mOrderId = orderId;
	}

	public String getOrderId() {
		return mOrderId;
	}

	public Money getTotalCharges() {
		return mTotalCharges;
	}

	public void setTotalCharges(Money totalCharges) {
		mTotalCharges = totalCharges;
	}
}
