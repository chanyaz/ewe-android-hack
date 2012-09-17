package com.expedia.bookings.test.utils;

/* This class serves as a data structure to hold all Hotel Booking info
 * and to provide methods to easily change that data as desired.
 */

public class HotelsBookingUtils {
	//Names
	public String mFirstName;
	public String mLastName;

	//Contact
	public String mPhoneNumber;
	public String mAddressLine1;
	public String mCityName;
	public String mStateCode;
	public String mZIPCode;

	//Credit Card Information
	public String mCreditCardNumber;
	public String mCardExpMonth;
	public String mCardExpYear;
	public String mCCV;

	//Search options
	public String mSearchLocation;
	public String mFilterText;

	//Log in Information.
	private String mLoginEmail;
	private String mLoginPassword;

	//Default Constructor sets info
	// to the qa-ehcc@mobiata.com account info
	HotelsBookingUtils() {
		mFirstName = "JexperCC";
		mLastName = "MobiataTestaverde";

		mPhoneNumber = "7342122392";
		mAddressLine1 = "1234 Test Blvd";
		mCityName = "Ann Arbor";
		mStateCode = "MI";
		mZIPCode = "48104";

		mLoginEmail = "qa-ehcc@mobiata.com";
		mLoginPassword = "3xp3d1acc";

		mCreditCardNumber = "4111111111111111";
		mCardExpMonth = "12";
		mCardExpYear = "20";
		mCCV = "111";

		mSearchLocation = "New York City";
		mFilterText = "Westin";
	}

	//All fields can be directly accessed/modified
	//except for Email & Password

	public String getEmail() {
		return mLoginEmail;
	}

	public void setEmail(String newEmail) {
		mLoginEmail = newEmail;
	}

	public void setPassword(String newPassword) {
		mLoginPassword = newPassword;
	}

	public String getPassword() {
		return mLoginPassword;
	}

}
