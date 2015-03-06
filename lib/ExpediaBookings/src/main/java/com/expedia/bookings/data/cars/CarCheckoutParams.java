package com.expedia.bookings.data.cars;

import com.expedia.bookings.data.Money;

public class CarCheckoutParams {
	public String tripId;
	public Money grandTotal;
	public String phoneCountryCode;
	public String phoneNumber;
	public String emailAddress;
	public String firstName;
	public String lastName;
	//TODO gaf about credit cards

	@Override
	public CarCheckoutParams clone() {
		CarCheckoutParams clone = new CarCheckoutParams();
		clone.tripId = tripId;
		clone.grandTotal = grandTotal;
		clone.phoneCountryCode = phoneCountryCode;
		clone.phoneNumber = phoneNumber;
		clone.emailAddress = emailAddress;
		clone.firstName = firstName;
		clone.lastName = lastName;
		return clone;
	}
}
