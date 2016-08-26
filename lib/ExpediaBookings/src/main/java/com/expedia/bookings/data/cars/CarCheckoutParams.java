package com.expedia.bookings.data.cars;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.expedia.bookings.data.Money;

public class CarCheckoutParams {
	public String tripId;
	public Money grandTotal;
	public String phoneCountryCode;
	public String phoneNumber;
	public String emailAddress;
	public String firstName;
	public String lastName;

	@Nullable
	public String ccNumber;
	public String ccExpirationYear;
	public String ccExpirationMonth;
	public String ccPostalCode;
	public String ccName;
	public String ccCVV;

	public String storedCCID;

	public boolean suppressFinalBooking;
	public boolean storeCreditCardInUserProfile;

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

		clone.ccNumber = ccNumber;
		clone.ccExpirationYear = ccExpirationYear;
		clone.ccExpirationMonth = ccExpirationMonth;
		clone.ccPostalCode = ccPostalCode;
		clone.ccName = ccName;
		clone.ccCVV = ccCVV;

		clone.storeCreditCardInUserProfile = storeCreditCardInUserProfile;
		clone.storedCCID = storedCCID;
		return clone;
	}

	public Map<String, Object> toQueryMap() {
		Map<String, Object> map = new HashMap<>();
		map.put("suppressFinalBooking", suppressFinalBooking);
		map.put("tripId", tripId);
		map.put("expectedTotalFare", grandTotal.amount.toString());
		map.put("expectedFareCurrencyCode", grandTotal.currencyCode);
		map.put("mainMobileTraveler.phoneCountryCode", phoneCountryCode);
		map.put("mainMobileTraveler.phone", phoneNumber);
		map.put("mainMobileTraveler.email", emailAddress);
		map.put("mainMobileTraveler.firstName", firstName);
		map.put("mainMobileTraveler.lastName", lastName);
		if (ccNumber != null) {
			map.put("creditCardNumber", ccNumber);
		}
		if (ccExpirationYear != null) {
			map.put("expirationDateYear", ccExpirationYear);
		}
		if (ccExpirationMonth != null) {
			map.put("expirationDateMonth", ccExpirationMonth);
		}
		if (ccPostalCode != null) {
			map.put("postalCode", ccPostalCode);
		}
		if (ccName != null) {
			map.put("nameOnCard", ccName);
		}
		if (ccCVV != null) {
			map.put("cvv", ccCVV);
		}
		if (storedCCID != null) {
			map.put("storedCreditCardId", storedCCID);
		}
		map.put("storeCreditCardInUserProfile", storeCreditCardInUserProfile);

		return map;
	}

}
