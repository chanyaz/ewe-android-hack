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
	private String mCCNumber;
	private String mCCExpirationYear;
	private String mCCExpirationMonth;
	private String mCCPostalCode;
	private String mCCName;
	private String mCCCVV;
	private String mStoredCCID;
	private boolean mSuppressFinalBooking;
	public boolean storeCreditCardInUserProfile;

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

	public CarCheckoutParamsBuilder ccNumber(String number) {
		mCCNumber = number;
		return this;
	}

	public CarCheckoutParamsBuilder expirationYear(String year) {
		mCCExpirationYear = year;
		return this;
	}

	public CarCheckoutParamsBuilder expirationMonth(String month) {
		mCCExpirationMonth = month;
		return this;
	}

	public CarCheckoutParamsBuilder ccPostalCode(String code) {
		mCCPostalCode = code;
		return this;
	}

	public CarCheckoutParamsBuilder ccName(String name) {
		mCCName = name;
		return this;
	}

	public CarCheckoutParamsBuilder cvv(String cvv) {
		mCCCVV = cvv;
		return this;
	}

	public CarCheckoutParamsBuilder storedCCID(String id) {
		mStoredCCID = id;
		return this;
	}

	public CarCheckoutParamsBuilder suppressFinalBooking(boolean suppress) {
		mSuppressFinalBooking = suppress;
		return this;
	}

	public CarCheckoutParamsBuilder guid(String guid) {
		return this;
	}

	public CarCheckoutParamsBuilder storeCreditCardInUserProfile(boolean storeCreditCard) {
		storeCreditCardInUserProfile = storeCreditCard;
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
		params.ccNumber = mCCNumber;
		params.ccExpirationYear = mCCExpirationYear;
		params.ccExpirationMonth = mCCExpirationMonth;
		params.ccPostalCode = mCCPostalCode;
		params.ccName = mCCName;
		params.ccCVV = mCCCVV;
		params.storedCCID = mStoredCCID;
		params.suppressFinalBooking = mSuppressFinalBooking;
		params.storeCreditCardInUserProfile = storeCreditCardInUserProfile;

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

	public String getEmailAddress() {
		return mEmailAddress;
	}
}
