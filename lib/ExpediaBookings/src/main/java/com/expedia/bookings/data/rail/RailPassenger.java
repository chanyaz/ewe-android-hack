package com.expedia.bookings.data.rail;

import com.expedia.bookings.data.Money;

public class RailPassenger {
	public String passengerIndex;
	public final int age;
	public final boolean primaryTraveler;
	public final PassengerAgeGroup passengerAgeGroup;
	public final Money price;

	public enum PassengerAgeGroup {
		ADULT,
		SENIOR,
		YOUTH,
		CHILD
	}
}
