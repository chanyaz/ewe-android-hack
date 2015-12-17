package com.expedia.bookings.data.hotels;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

public class HotelCheckoutParams {

	@NotNull public String tripId;
	@NotNull public String expectedTotalFare;
	@NotNull public String expectedFareCurrencyCode;
	@NotNull public String firstName;
	@NotNull public String lastName;
	@NotNull public String phone;
	@NotNull public String phoneCountryCode;
	@NotNull public String email;
	@NotNull public boolean sendEmailConfirmation;
	@NotNull public String abacusUserGuid;
	@NotNull public String checkInDate;
	@NotNull public String checkOutDate;
	@NotNull public String nameOnCard;
	@NotNull public String cvv;
	@NotNull public String expirationDateYear;
	@NotNull public String expirationDateMonth;
	@NotNull public String creditCardNumber;
	@NotNull public String postalCode;

	@NotNull
	public Map<String, Object> toQueryMap() {
		Map<String, Object> queryMap = new HashMap<>();
		queryMap.put("tripId", tripId);
		queryMap.put("expectedTotalFare", expectedTotalFare);
		queryMap.put("expectedFareCurrencyCode", expectedFareCurrencyCode);
		queryMap.put("sendEmailConfirmation", false);
		queryMap.put("userId", "");
		queryMap.put("doIThinkImSignedIn", false);
		queryMap.put("storeCreditCardInUserProfile", false);
		queryMap.put("suppressFinalBooking", true);
		queryMap.put("abacusUserGuid", abacusUserGuid);
		queryMap.put("firstName", firstName);
		queryMap.put("lastName", lastName);
		queryMap.put("phone", phone);
		queryMap.put("phoneCountryCode", phoneCountryCode);
		queryMap.put("email", email);
		queryMap.put("sendEmailConfirmation", sendEmailConfirmation);
		queryMap.put("checkInDate", checkInDate);
		queryMap.put("checkOutDate", checkOutDate);
		queryMap.put("cvv", cvv);
		queryMap.put("nameOnCard", nameOnCard);
		queryMap.put("creditCardNumber", creditCardNumber);
		queryMap.put("expirationDateYear", expirationDateYear);
		queryMap.put("expirationDateMonth", expirationDateMonth);
		queryMap.put("postalCode", postalCode);

		return queryMap;
	}
}
