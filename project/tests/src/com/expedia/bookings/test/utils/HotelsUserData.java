package com.expedia.bookings.test.utils;

import android.app.Instrumentation;
import android.util.Pair;

import com.expedia.bookings.debug.test.R;

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
	private String mCardExpMonth;
	private String mCardExpYear;
	private String mCCV;

	//Search options
	private String mFilterText;

	//Log in Information.
	private String mLoginEmail;
	private String mLoginPassword;

	//Configuration options
	private String mBookingServer;

	//Airports and hotel locations
	private String mDepartureAirport;
	private String mArrivalAirport;
	private String mHotelSearchCity;

	//Settings
	private String mProxyIP;
	private String mProxyPort;
	private boolean mLogInForCheckout;

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
		setCardExpMonth("12");
		setCardExpYear("20");
		setCCV("111");

		setFilterText("Westin");

		setBookingServer("Production");

		setDepartureAirport("SFO");
		setArrivalAirport("LAX");
		setHotelSearchCity("New York City");
		setServerIP("localhost");
		setServerPort("3000");
		setLogInForCheckout(true);
	}

	public void setAirportsToRandomUSAirports() {
		LocationSelectUtils airportSelect = new LocationSelectUtils();
		Pair<String, String> airportPair = airportSelect.getTwoRandomAmericanAirports();
		setDepartureAirport(airportPair.first);
		setArrivalAirport(airportPair.second);
	}

	public void setAirportsToRandomINTLAirports() {
		LocationSelectUtils airportSelect = new LocationSelectUtils();
		Pair<String, String> airportPair = airportSelect.getTwoRandomInternationalAirports();
		setDepartureAirport(airportPair.first);
		setArrivalAirport(airportPair.second);
	}

	public void setAirportsToRandomINTLAndUSAirports() {
		LocationSelectUtils airportSelect = new LocationSelectUtils();
		Pair<String, String> airportPair = airportSelect.getRandomAmericanAndInternationalAirport();
		setDepartureAirport(airportPair.first);
		setArrivalAirport(airportPair.second);
	}

	public void setHotelCityToRandomUSCity() {
		LocationSelectUtils citySelect = new LocationSelectUtils();
		String hotelCity = citySelect.getRandomAmericanAndInternationalCity().first;
		setHotelSearchCity(hotelCity);
	}

	/*
	 *  Getters and setters for all attributes
	 */

	// API settings
	public String getBookingServer() {
		return mBookingServer;
	}

	public void setBookingServer(String bookingServer) {
		mBookingServer = bookingServer;
	}

	public String getServerIP() {
		return mProxyIP;
	}

	public void setServerIP(String proxyIP) {
		mProxyIP = proxyIP;
	}

	public String getServerPort() {
		return mProxyPort;
	}

	public void setServerPort(String proxyPort) {
		mProxyPort = proxyPort;
	}

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

	public String getCardExpMonth() {
		return mCardExpMonth;
	}

	public void setCardExpMonth(String cardExpMonth) {
		mCardExpMonth = cardExpMonth;
	}

	public String getCardExpYear() {
		return mCardExpYear;
	}

	public void setCardExpYear(String cardExpYear) {
		mCardExpYear = cardExpYear;
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

	public void setLoginEmail(String mLoginEmail) {
		this.mLoginEmail = mLoginEmail;
	}

	public String getLoginPassword() {
		return mLoginPassword;
	}

	public void setLoginPassword(String mLoginPassword) {
		this.mLoginPassword = mLoginPassword;
	}

	public boolean getLogInForCheckout() {
		return mLogInForCheckout;
	}

	public void setLogInForCheckout(boolean logInForCheckout) {
		mLogInForCheckout = logInForCheckout;
	}

	// Search Settings

	public String getFilterText() {
		return mFilterText;
	}

	public void setFilterText(String mFilterText) {
		this.mFilterText = mFilterText;
	}

	public String getDepartureAirport() {
		return mDepartureAirport;
	}

	public void setDepartureAirport(String departureAirport) {
		mDepartureAirport = departureAirport;
	}

	public String getArrivalAirport() {
		return mArrivalAirport;
	}

	public void setArrivalAirport(String arrivalAirport) {
		mArrivalAirport = arrivalAirport;
	}

	public String getHotelSearchCity() {
		return mHotelSearchCity;
	}

	public void setHotelSearchCity(String hotelSearchCity) {
		mHotelSearchCity = hotelSearchCity;
	}

}
