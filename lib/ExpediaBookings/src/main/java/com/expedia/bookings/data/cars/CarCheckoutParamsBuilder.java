package com.expedia.bookings.data.cars;

import com.expedia.bookings.data.Money;
import com.expedia.bookings.utils.Strings;

public class CarCheckoutParamsBuilder {
	private String mTripId;
	private Money mGrandTotal;
	private String mPhoneCountryCode;
	private String mPhoneNumber;
	private String mEmailAddress;
	private String mFirstName;
	private String mLastName;

	public CarCheckoutParamsBuilder tripId(String tripId) {
		mTripId = tripId;
		return this;
	}

	public CarCheckoutParamsBuilder grandTotal(Money grandTotal) {
		mGrandTotal = grandTotal;
		return this;
	}

	public CarCheckoutParamsBuilder phoneCountryCode(String countryCode) {
		mPhoneCountryCode = countryCode;
		return this;
	}

	public CarCheckoutParamsBuilder phoneNumber(String phoneNumber) {
		mPhoneNumber = phoneNumber;
		return this;
	}

	public CarCheckoutParamsBuilder emailAddress(String emailAddress) {
		mEmailAddress = emailAddress;
		return this;
	}

	public CarCheckoutParamsBuilder firstName(String firstName) {
		mFirstName = firstName;
		return this;
	}

	public CarCheckoutParamsBuilder lastName(String lastName) {
		mLastName = lastName;
		return this;
	}

	public CarCheckoutParams build() {
		CarCheckoutParams params = new CarCheckoutParams();
		params.tripId = mTripId;
		params.phoneCountryCode = mPhoneCountryCode;
		params.phoneNumber = mPhoneNumber;
		params.emailAddress = mEmailAddress;
		params.firstName = mFirstName;
		params.lastName = mLastName;
		params.grandTotal = mGrandTotal;
		return params;
	}

	public boolean areRequiredParamsFilled() {
		return Strings.isNotEmpty(mTripId)
			&& Strings.isNotEmpty(mPhoneCountryCode)
			&& Strings.isNotEmpty(mPhoneNumber)
			&& Strings.isNotEmpty(mEmailAddress)
			&& Strings.isNotEmpty(mFirstName)
			&& Strings.isNotEmpty(mLastName)
			&& mGrandTotal != null;
	}

}
