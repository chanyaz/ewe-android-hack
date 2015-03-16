package com.expedia.bookings.data.lx;

import com.expedia.bookings.utils.Strings;

public class LXCheckoutParamsBuilder {
	private String firstName;
	private String lastName;
	private String phone;
	private String phoneCountryCode;
	private String tripId;
	private String postalCode;
	private String expectedFareCurrencyCode;
	private String expectedTotalFare;
	private String nameOnCard;
	private String creditCardNumber;
	private String expirationDateYear;
	private String expirationDateMonth;
	private String cvv;
	private String email;
	private String storedCreditCardId;

	public LXCheckoutParamsBuilder firstName(String firstName) {
		this.firstName = firstName;
		return this;
	}

	public LXCheckoutParamsBuilder lastName(String lastName) {
		this.lastName = lastName;
		return this;
	}

	public LXCheckoutParamsBuilder phone(String phone) {
		this.phone = phone;
		return this;
	}

	public LXCheckoutParamsBuilder phoneCountryCode(String phoneCountryCode) {
		this.phoneCountryCode = phoneCountryCode;
		return this;
	}

	public LXCheckoutParamsBuilder tripId(String tripId) {
		this.tripId = tripId;
		return this;
	}

	public LXCheckoutParamsBuilder postalCode(String postalCode) {
		this.postalCode = postalCode;
		return this;
	}

	public LXCheckoutParamsBuilder expectedFareCurrencyCode(String expectedFareCurrencyCode) {
		this.expectedFareCurrencyCode = expectedFareCurrencyCode;
		return this;
	}

	public LXCheckoutParamsBuilder expectedTotalFare(String expectedTotalFare) {
		this.expectedTotalFare = expectedTotalFare;
		return this;
	}

	public LXCheckoutParamsBuilder nameOnCard(String nameOnCard) {
		this.nameOnCard = nameOnCard;
		return this;
	}

	public LXCheckoutParamsBuilder creditCardNumber(String creditCardNumber) {
		this.creditCardNumber = creditCardNumber;
		return this;
	}

	public LXCheckoutParamsBuilder expirationDateYear(String expirationDateYear) {
		this.expirationDateYear = expirationDateYear;
		return this;
	}

	public LXCheckoutParamsBuilder expirationDateMonth(String expirationDateMonth) {
		this.expirationDateMonth = expirationDateMonth;
		return this;
	}

	public LXCheckoutParamsBuilder cvv(String cvv) {
		this.cvv = cvv;
		return this;
	}

	public LXCheckoutParamsBuilder email(String email) {
		this.email = email;
		return this;
	}

	public LXCheckoutParamsBuilder storedCreditCardId(String storedCreditCardId) {
		this.storedCreditCardId = storedCreditCardId;
		return this;
	}

	public LXCheckoutParams build() {
		LXCheckoutParams checkoutParams = new LXCheckoutParams();
		checkoutParams.firstName = firstName;
		checkoutParams.lastName = lastName;
		checkoutParams.phone = phone;
		checkoutParams.phoneCountryCode = phoneCountryCode;
		checkoutParams.tripId = tripId;
		checkoutParams.postalCode = postalCode;
		checkoutParams.expectedFareCurrencyCode = expectedFareCurrencyCode;
		checkoutParams.expectedTotalFare = expectedTotalFare;
		checkoutParams.nameOnCard = nameOnCard;
		checkoutParams.creditCardNumber = creditCardNumber;
		checkoutParams.expirationDateYear = expirationDateYear;
		checkoutParams.expirationDateMonth = expirationDateMonth;
		checkoutParams.cvv = cvv;
		checkoutParams.email = email;
		checkoutParams.storedCreditCardId = storedCreditCardId;
		return checkoutParams;
	}
	public boolean areRequiredParamsFilled() {
		boolean paramsFilled =
			Strings.isNotEmpty(firstName) && Strings.isNotEmpty(lastName) && Strings.isNotEmpty(phoneCountryCode)
				&& Strings.isNotEmpty(phone) && Strings.isNotEmpty(email) && Strings.isNotEmpty(tripId) && Strings
				.isNotEmpty(expectedTotalFare) && Strings.isNotEmpty(expectedFareCurrencyCode) && Strings
				.isNotEmpty(cvv);
		return paramsFilled;
	}
}
