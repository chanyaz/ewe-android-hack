package com.expedia.bookings.data;

public class FlightCheckoutResponse extends Response {

	private String mItineraryNumber;
	private String mTravelRecordLocator;
	private String mTripId;

	private Money mTotalCharges;

	public String getItineraryNumber() {
		return mItineraryNumber;
	}

	public void setItineraryNumber(String itineraryNumber) {
		mItineraryNumber = itineraryNumber;
	}

	public String getTravelRecordLocator() {
		return mTravelRecordLocator;
	}

	public void setTravelRecordLocator(String travelRecordLocator) {
		mTravelRecordLocator = travelRecordLocator;
	}

	public String getTripId() {
		return mTripId;
	}

	public void setTripId(String tripId) {
		mTripId = tripId;
	}

	public Money getTotalCharges() {
		return mTotalCharges;
	}

	public void setTotalCharges(Money totalCharges) {
		mTotalCharges = totalCharges;
	}

}
