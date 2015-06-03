package com.expedia.bookings.test.ui.utils;

import android.app.Instrumentation;

import com.expedia.bookings.test.R;

/* This class serves as a data structure to hold all Hotel Booking info
 * and to provide methods to easily change that data as desired.
 */

public class HotelsUserData {

	//Names
	private String mFirstName;
	private String mLastName;

	//Contact
	private String mPhoneNumber;
	private String mAddressLine1;
	private String mCityName;
	private String mStateCode;
	private String mZIPCode;

	//Credit Card Information
	private String mCreditCardNumber;
	private String mCCV;

	//Log in Information.
	private String mLoginEmail;
	private String mLoginPassword;

	public HotelsUserData(Instrumentation instrumentation) {
		setFirstName("JexperCC");
		setLastName("MobiataTestaverde");

		setPhoneNumber("7342122392");
		setAddressLine1("1234 Test Blvd");
		setAddressCity("Ann Arbor");
		setAddressStateCode("MI");
		setAddressPostalCode("48104");

		setLoginEmail(instrumentation.getContext().getString(R.string.user_name));
		setLoginPassword(instrumentation.getContext().getString(R.string.user_password));

		setCreditCardNumber("4111111111111111");
		setCCV("111");

	}

	/*
	 *  Getters and setters for all attributes
	 */

	// Name

	public String getFirstName() {
		return mFirstName;
	}

	public void setFirstName(String firstName) {
		mFirstName = firstName;
	}

	public String getLastName() {
		return mLastName;
	}

	public void setLastName(String lastName) {
		mLastName = lastName;
	}

	// Credit card info

	public String getCreditCardNumber() {
		return mCreditCardNumber;
	}

	public void setCreditCardNumber(String creditCardNumber) {
		mCreditCardNumber = creditCardNumber;
	}

	public String getCCV() {
		return mCCV;
	}

	public void setCCV(String cvv) {
		mCCV = cvv;
	}

	// Address & Phone

	public String getAddressLine1() {
		return mAddressLine1;
	}

	public void setAddressLine1(String addressLine1) {
		mAddressLine1 = addressLine1;
	}

	public String getAddressCity() {
		return mCityName;
	}

	public void setAddressCity(String cityName) {
		mCityName = cityName;
	}

	public String getAddressStateCode() {
		return mStateCode;
	}

	public void setAddressStateCode(String stateCode) {
		mStateCode = stateCode;
	}

	public String getAddressPostalCode() {
		return mZIPCode;
	}

	public void setAddressPostalCode(String postalCode) {
		mZIPCode = postalCode;
	}

	public String getPhoneNumber() {
		return mPhoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		mPhoneNumber = phoneNumber;
	}

	// Log in info

	public String getLoginEmail() {
		return mLoginEmail;
	}

	public void setLoginEmail(String loginEmail) {
		mLoginEmail = loginEmail;
	}

	public String getLoginPassword() {
		return mLoginPassword;
	}

	public void setLoginPassword(String loginPassword) {
		mLoginPassword = loginPassword;
	}

}
