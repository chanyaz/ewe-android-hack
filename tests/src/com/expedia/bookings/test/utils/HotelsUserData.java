package com.expedia.bookings.test.utils;

import android.util.Pair;

/* This class serves as a data structure to hold all Hotel Booking info
 * and to provide methods to easily change that data as desired.
 */

public class HotelsUserData {

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
	public String mFilterText;

	//Log in Information.
	public String mLoginEmail;
	public String mLoginPassword;

	//Configuration options
	public String mBookingServer;

	//Airports and hotel locations
	public String mDepartureAirport;
	public String mArrivalAirport;
	public String mHotelSearchCity;

	//Settings
	public String mServerName;
	public String mProxyIP;
	public String mProxyPort;

	//Default Constructor sets info
	// to the qa-ehcc@mobiata.com account info
	public HotelsUserData() {
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

		mFilterText = "Westin";

		mBookingServer = "Production";

		mDepartureAirport = "SFO";
		mArrivalAirport = "LAX";
		mHotelSearchCity = "New York City";
		mServerName = "Production";
		mProxyIP = null;
		mProxyPort = null;
	}

	public void setAirportsToRandomUSAirports() {
		LocationSelectUtils airportSelect = new LocationSelectUtils();
		Pair<String, String> airportPair = airportSelect.getTwoRandomAmericanAirports();
		mDepartureAirport = airportPair.first;
		mArrivalAirport = airportPair.second;
	}

	public void setAirportsToRandomINTLAirports() {
		LocationSelectUtils airportSelect = new LocationSelectUtils();
		Pair<String, String> airportPair = airportSelect.getTwoRandomInternationalAirports();
		mDepartureAirport = airportPair.first;
		mArrivalAirport = airportPair.second;
	}

	public void setAirportsToRandomINTLAndUSAirports() {
		LocationSelectUtils airportSelect = new LocationSelectUtils();
		Pair<String, String> airportPair = airportSelect.getRandomAmericanAndInternationalAirport();
		mDepartureAirport = airportPair.first;
		mArrivalAirport = airportPair.second;
	}

	public void setHotelCityToRandomUSCity() {
		LocationSelectUtils citySelect = new LocationSelectUtils();
		String hotelCity = citySelect.getRandomAmericanAndInternationalCity().first;
		mHotelSearchCity = hotelCity;
	}

}
