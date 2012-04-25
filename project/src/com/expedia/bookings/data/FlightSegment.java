package com.expedia.bookings.data;

import android.text.format.Time;

public class FlightSegment {

	private Time mDepartureTime;
	private Time mArrivalTime;

	private String mArrivalAirportCode;
	private String mDepartureAirportCode;

	private String mAirlineCode;
	private String mFlightNumber;

	public Time getDepartureTime() {
		return mDepartureTime;
	}

	public void setDepartureTime(Time departureTime) {
		mDepartureTime = departureTime;
	}

	public Time getArrivalTime() {
		return mArrivalTime;
	}

	public void setArrivalTime(Time arrivalTime) {
		mArrivalTime = arrivalTime;
	}

	public String getArrivalAirportCode() {
		return mArrivalAirportCode;
	}

	public void setArrivalAirportCode(String arrivalAirportCode) {
		mArrivalAirportCode = arrivalAirportCode;
	}

	public String getDepartureAirportCode() {
		return mDepartureAirportCode;
	}

	public void setDepartureAirportCode(String departureAirportCode) {
		mDepartureAirportCode = departureAirportCode;
	}

	public String getAirlineCode() {
		return mAirlineCode;
	}

	public void setAirlineCode(String airlineCode) {
		mAirlineCode = airlineCode;
	}

	public String getFlightNumber() {
		return mFlightNumber;
	}

	public void setFlightNumber(String flightNumber) {
		mFlightNumber = flightNumber;
	}
}
