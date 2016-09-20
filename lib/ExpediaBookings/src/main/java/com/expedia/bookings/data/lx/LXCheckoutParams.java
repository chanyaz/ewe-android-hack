package com.expedia.bookings.data.lx;

import java.util.HashMap;
import java.util.Map;

import com.expedia.bookings.utils.Strings;

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
	public boolean suppressFinalBooking;
	public boolean storeCreditCardInUserProfile;

	public LXCheckoutParams firstName(String firstName) {
		this.firstName = firstName;
		return this;
	}

	public LXCheckoutParams lastName(String lastName) {
		this.lastName = lastName;
		return this;
	}

	public LXCheckoutParams phone(String phone) {
		this.phone = phone;
		return this;
	}

	public LXCheckoutParams phoneCountryCode(String phoneCountryCode) {
		this.phoneCountryCode = phoneCountryCode;
		return this;
	}

	public LXCheckoutParams tripId(String tripId) {
		this.tripId = tripId;
		return this;
	}

	public LXCheckoutParams postalCode(String postalCode) {
		this.postalCode = postalCode;
		return this;
	}

	public LXCheckoutParams expectedFareCurrencyCode(String expectedFareCurrencyCode) {
		this.expectedFareCurrencyCode = expectedFareCurrencyCode;
		return this;
	}

	public LXCheckoutParams expectedTotalFare(String expectedTotalFare) {
		this.expectedTotalFare = expectedTotalFare;
		return this;
	}

	public LXCheckoutParams nameOnCard(String nameOnCard) {
		this.nameOnCard = nameOnCard;
		return this;
	}

	public LXCheckoutParams creditCardNumber(String creditCardNumber) {
		this.creditCardNumber = creditCardNumber;
		return this;
	}

	public LXCheckoutParams expirationDateYear(String expirationDateYear) {
		this.expirationDateYear = expirationDateYear;
		return this;
	}

	public LXCheckoutParams expirationDateMonth(String expirationDateMonth) {
		this.expirationDateMonth = expirationDateMonth;
		return this;
	}

	public LXCheckoutParams cvv(String cvv) {
		this.cvv = cvv;
		return this;
	}

	public LXCheckoutParams email(String email) {
		this.email = email;
		return this;
	}

	public LXCheckoutParams storedCreditCardId(String storedCreditCardId) {
		this.storedCreditCardId = storedCreditCardId;
		return this;
	}

	public LXCheckoutParams suppressFinalBooking(boolean suppressFinalBooking) {
		this.suppressFinalBooking = suppressFinalBooking;
		return this;
	}

	public LXCheckoutParams storeCreditCardInUserProfile(boolean storeCreditCardInUserProfile) {
		this.storeCreditCardInUserProfile = storeCreditCardInUserProfile;
		return this;
	}

	public boolean areRequiredParamsFilled() {
		boolean paramsFilled =
			Strings.isNotEmpty(firstName) && Strings.isNotEmpty(lastName) && Strings.isNotEmpty(phoneCountryCode)
				&& Strings.isNotEmpty(phone) && Strings.isNotEmpty(email) && Strings.isNotEmpty(tripId) && Strings
				.isNotEmpty(expectedTotalFare) && Strings.isNotEmpty(expectedFareCurrencyCode) && Strings
				.isNotEmpty(cvv);
		return paramsFilled;
	}

	public String getEmailAddress() {
		return email;
	}

	public Map<String, Object> toQueryMap() {
		Map<String, Object> params = new HashMap<>();
		params.put("firstName", firstName);
		params.put("lastName", lastName);
		params.put("phone", phone);
		params.put("phoneCountryCode", phoneCountryCode);
		params.put("tripId", tripId);
		if (postalCode != null) {
			params.put("postalCode", postalCode);
		}
		params.put("expectedFareCurrencyCode", expectedFareCurrencyCode);
		params.put("expectedFareCurrencyCode", expectedFareCurrencyCode);
		params.put("expectedTotalFare", expectedTotalFare);
		if (nameOnCard != null) {
			params.put("nameOnCard", nameOnCard);
		}
		if (creditCardNumber != null) {
			params.put("creditCardNumber", creditCardNumber);
		}
		if (expirationDateYear != null) {
			params.put("expirationDateYear", expirationDateYear);
		}
		if (expirationDateMonth != null) {
			params.put("expirationDateMonth", expirationDateMonth);
		}
		if (cvv != null) {
			params.put("cvv", cvv);
		}
		if (email != null) {
			params.put("email", email);
		}
		if (storedCreditCardId != null) {
			params.put("storedCreditCardId", storedCreditCardId);
		}
		params.put("suppressFinalBooking", suppressFinalBooking);
		params.put("storeCreditCardInUserProfile", storeCreditCardInUserProfile);
		return params;
	}
}
