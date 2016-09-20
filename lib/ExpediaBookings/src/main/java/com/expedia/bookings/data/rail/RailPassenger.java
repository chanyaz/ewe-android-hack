package com.expedia.bookings.data.rail;

import com.expedia.bookings.data.Money;

public class RailPassenger {
	public String passengerIndex;
	public int age;
	public boolean primaryTraveler;
	public PassengerAgeGroup passengerAgeGroup;
	public Money price;

	public enum PassengerAgeGroup {
		ADULT,
		SENIOR,
		YOUTH,
		CHILD
	}
}
