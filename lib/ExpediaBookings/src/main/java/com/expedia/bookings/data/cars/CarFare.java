package com.expedia.bookings.data.cars;

public class CarFare {

	public RateTerm rateTerm;
	public Money rate;
	public Money total;

	public enum RateTerm {
		UNKNOWN,
		HOURLY,
		DAILY,
		WEEKLY,
		WEEKEND,
		MONTHLY,
		TOTAL,
	}
}
