package com.expedia.bookings.data.lx;

public class LXCheckoutParams {
	public String streetAddress;
	public String firstName;
	public String lastName;
	public String phone;
	public String checkInDate;
	public int phoneCountryCode;
	public String tripId;
	public String state;
	public String city;
	public String country;
	public String postalCode;
	public String expectedFareCurrencyCode;
	public String expectedTotalFare;
	public String nameOnCard;
	public String creditCardNumber;
	public String expirationDateYear;
	public String expirationDateMonth;
	public String cvv;
	public String email;

	public boolean areRequiredParamsFilled() {
		//TODO - needs to be hooked up
		return false;
	}
}
