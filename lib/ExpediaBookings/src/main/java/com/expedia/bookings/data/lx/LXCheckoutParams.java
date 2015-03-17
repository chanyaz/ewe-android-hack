package com.expedia.bookings.data.lx;

import java.util.HashMap;
import java.util.Map;

public class LXCheckoutParams {
	public String firstName;
	public String lastName;
	public String phone;
	public String phoneCountryCode;
	public String tripId;
	public String postalCode;
	public String expectedFareCurrencyCode;
	public String expectedTotalFare;
	public String nameOnCard;
	public String creditCardNumber;
	public String expirationDateYear;
	public String expirationDateMonth;
	public String cvv;
	public String email;
	public String storedCreditCardId;

	public Map<String, Object> toQueryMap() {
		Map<String, Object> params = new HashMap<>();
		params.put("firstName", firstName);
		params.put("lastName", lastName);
		params.put("phone", phone);
		params.put("phoneCountryCode", phoneCountryCode);
		params.put("tripId", tripId);
		params.put("postalCode", postalCode);
		params.put("expectedFareCurrencyCode", expectedFareCurrencyCode);
		params.put("expectedFareCurrencyCode", expectedFareCurrencyCode);
		params.put("expectedTotalFare", expectedTotalFare);
		params.put("nameOnCard", nameOnCard);
		params.put("creditCardNumber", creditCardNumber);
		params.put("expirationDateYear", expirationDateYear);
		params.put("expirationDateMonth", expirationDateMonth);
		params.put("cvv", cvv);
		params.put("email", email);
		params.put("storedCreditCardId", storedCreditCardId);
		return params;
	}
}
