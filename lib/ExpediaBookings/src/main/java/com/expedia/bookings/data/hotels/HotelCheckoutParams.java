package com.expedia.bookings.data.hotels;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

import com.expedia.bookings.utils.Strings;

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
	@NotNull public String tealeafTransactionId;
	public boolean suppressFinalBooking = true;
	public boolean storeCreditCardInUserProfile = false;
	public String expirationDateYear;
	public String expirationDateMonth;
	public String storedCreditCardId;
	public String creditCardNumber;
	public String postalCode;
	public String emailOptIn;

	@NotNull
	public Map<String, Object> toQueryMap() {
		Map<String, Object> queryMap = new HashMap<>();
		queryMap.put("tripId", tripId);
		queryMap.put("expectedTotalFare", expectedTotalFare);
		queryMap.put("expectedFareCurrencyCode", expectedFareCurrencyCode);
		queryMap.put("sendEmailConfirmation", false);
		queryMap.put("storeCreditCardInUserProfile", storeCreditCardInUserProfile);
		queryMap.put("suppressFinalBooking", suppressFinalBooking);
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
		queryMap.put("storedCreditCardId", storedCreditCardId);
		queryMap.put("creditCardNumber", creditCardNumber);
		queryMap.put("expirationDateYear", expirationDateYear);
		queryMap.put("expirationDateMonth", expirationDateMonth);
		if (!Strings.isEmpty(postalCode)) {
			queryMap.put("postalCode", postalCode);
		}
		queryMap.put("tealeafTransactionId", tealeafTransactionId);
		queryMap.put("emailOptIn", emailOptIn);

		return queryMap;
	}
}
