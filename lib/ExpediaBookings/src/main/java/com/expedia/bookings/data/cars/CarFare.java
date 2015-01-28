package com.expedia.bookings.data.cars;

import com.expedia.bookings.data.Money;

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
