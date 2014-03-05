package com.expedia.bookings.test.tests.localization;

import java.util.Locale;

import com.expedia.bookings.activity.LaunchActivity;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.test.utils.CustomActivityInstrumentationTestCase;
import com.expedia.bookings.test.utils.FlightsTestDriver;
import com.expedia.bookings.utils.ClearPrivateDataUtil;
import com.mobiata.android.util.SettingUtils;

public class FlightsMarketingSweep extends CustomActivityInstrumentationTestCase<LaunchActivity> {

	private FlightsTestDriver mDriver;
	private static final String TAG = FlightsMarketingSweep.class.getSimpleName();

	public FlightsMarketingSweep() {
		super(LaunchActivity.class);
	}

	protected void setUp() throws Exception {
		super.setUp();
		mPreferences.setScreenshotPermission(true);
		mPreferences.setRotationPermission(false);
		mDriver = new FlightsTestDriver(getInstrumentation(), getActivity(), mRes, mPreferences);
		mUser.setHotelCityToRandomUSCity();
		mUser.setFirstName("John");
		mUser.setLastName("Smith");
		mUser.setAddressLine1("543 Belle Lane");
		mUser.setAirportsToLocaleDefault(Locale.getDefault());
		ClearPrivateDataUtil.clear(mDriver.getCurrentActivity());
		SettingUtils.save(mDriver.getCurrentActivity().getApplicationContext(),
			"suppress_flight_bookings", true);
	}

	public void testMethod() throws Exception {
		mDriver.delay(20);
		// Launch screen
		mDriver.screenshot("Launch Screen");
		mDriver.delay(2);
		mDriver.screenshot("Launch Screen 2");
		mDriver.delay(2);
		mDriver.screenshot("Launch Screen 3");
		mDriver.delay(2);
		mDriver.screenshot("Launch Screen 4");
		mDriver.enterLog(TAG, "Launching flights application");
		mDriver.launchScreen().launchFlights();

		// Flights search screen
		if (PointOfSale.getPointOfSale().displayFlightDropDownRoutes()) {
			mDriver.flightsSearchScreen().clickDepartureSpinner();
			mDriver.clickOnText(mUser.getDepartureAirport());
			mDriver.flightsSearchScreen().clickArrivalSpinner();
			mDriver.clickOnText(mUser.getArrivalAirport());
		}
		else {
			mDriver.enterLog(TAG, "Set departure airport: " + mUser.getDepartureAirport());
			mDriver.flightsSearchScreen().enterDepartureAirport(mUser.getDepartureAirport());
			mDriver.enterLog(TAG, "Set arrival airport: " + mUser.getArrivalAirport());
			mDriver.flightsSearchScreen().enterArrivalAirport(mUser.getArrivalAirport());
		}

		mDriver.flightsSearchScreen().clickSelectDepartureButton();
		int dateOffset = 15;
		mDriver.enterLog(TAG, "Selecting date with offset from current date: " + dateOffset);
		mDriver.flightsSearchScreen().clickDate(dateOffset);
		mDriver.flightsSearchScreen().clickDate(dateOffset + 2);
		mDriver.delay();
		mDriver.screenshot("Flights Search");
		mDriver.enterLog(TAG, "Click search button");
		mDriver.flightsSearchScreen().clickSearchButton();

		// Searching
		mDriver.waitForStringToBeGone(mDriver.flightsSearchLoading().getLoadingFlightsString(), 60);

		// Search results
		mDriver.enterLog(TAG, "Flight search results loaded");
		mDriver.screenshot("Flight search results");
		int flightIndex = 0;
		mDriver.enterLog(TAG, "Selecting flight at index: " + flightIndex);
		mDriver.flightsSearchResultsScreen().selectFlightFromList(flightIndex);
		mDriver.delay(1);
		mDriver.screenshot("Flight leg details 1");
		mDriver.flightLegScreen().clickSelectFlightButton();


		// Flight leg confirmation
		mDriver.screenshot("Flight search results 2");
		mDriver.flightsSearchResultsScreen().selectFlightFromList(0);
		mDriver.delay();
		mDriver.screenshot("Flight leg screen 2");
		mDriver.flightLegScreen().clickSelectFlightButton();
		mDriver.waitForStringToBeGone(mDriver.flightLegScreen().checkingForPriceChangesString(), 60);
		mDriver.screenshot("Flights checkout overview");

		// Checkout
		mDriver.flightsCheckoutScreen().clickCheckoutButton();
		mDriver.enterLog(TAG, "Clicked checkout button");
		mDriver.screenshot("Flights checkout 2");
		mDriver.delay();

		// Log in
		mDriver.flightsCheckoutScreen().clickLogInButton();
		mDriver.delay();
		mDriver.screenshot("Log in screen");
		mDriver.goBack();
		mDriver.goBack();

		// Open Traveler information
		mDriver.flightsCheckoutScreen().clickTravelerDetailsButton();
		mDriver.delay();
		mDriver.hideSoftKeyboard();
		mDriver.delay(1);
		mDriver.screenshot("Traveler Details");
		mDriver.travelerInformationScreen().enterFirstName(mUser.getFirstName());
		mDriver.travelerInformationScreen().enterLastName(mUser.getLastName());
		mDriver.travelerInformationScreen().enterPhoneNumber(mUser.getPhoneNumber());
		mDriver.travelerInformationScreen().clickBirthDateButton();
		try {
			mDriver.travelerInformationScreen().clickDoneString();
		}
		catch (Error e) {
			mDriver.travelerInformationScreen().clickOnButton(0);
		}
		mDriver.travelerInformationScreen().clickNextButton();
		mDriver.hideSoftKeyboard();
		mDriver.delay();
		mDriver.screenshot("Traveler Details 2");
		try {
			mDriver.travelerInformationScreen().clickDoneButton();
		}
		catch (Error e) {
			mDriver.travelerInformationScreen().clickNextButton();
			if (mDriver.searchText(mDriver.travelerInformationScreen().passportString())) {
				mDriver.screenshot("Passport screen");
			}
			mDriver.travelerInformationScreen().clickDoneButton();
		}
		mDriver.delay();

		// Open billing information
		mDriver.flightsCheckoutScreen().clickSelectPaymentButton();
		mDriver.hideSoftKeyboard();
		mDriver.delay(3);
		mDriver.screenshot("Payment Details");
		if (PointOfSale.getPointOfSale().requiresBillingAddressFlights()) {
			mDriver.screenshot("Address");
			mDriver.enterLog(TAG, "Entering address line 1: " + mUser.getAddressLine1());
			mDriver.billingAddressScreen().typeTextAddressLineOne(mUser.getAddressLine1());
			mDriver.enterLog(TAG, "Entering address city: " + mUser.getAddressCity());
			mDriver.billingAddressScreen().typeTextCity(mUser.getAddressCity());
			if (mDriver.billingAddressScreen().stateEditText().getText().equals("")) {
				mDriver.enterLog(TAG, "Entering address state code: " + mUser.getAddressStateCode());
				mDriver.billingAddressScreen().typeTextState(mUser.getAddressStateCode());
			}
			mDriver.enterLog(TAG, "Entering postal code: " + mUser.getAddressPostalCode());
			mDriver.billingAddressScreen().typeTextPostalCode(mUser.getAddressPostalCode());
			mDriver.billingAddressScreen().clickNextButton();
		}
		mDriver.hideSoftKeyboard();
		mDriver.delay();
		mDriver.enterLog(TAG, "Using new credit card");
		mDriver.screenshot("Card info");
		mDriver.enterLog(TAG, "Entering credit card with number: " + mUser.getCreditCardNumber());
		mDriver.cardInfoScreen().typeTextCreditCardEditText(mUser.getCreditCardNumber());
		mDriver.cardInfoScreen().clickOnExpirationDateButton();
		mDriver.enterLog(TAG, "Incrementing credit card exp. month and year by 1");
		mDriver.cardInfoScreen().clickMonthUpButton();
		mDriver.cardInfoScreen().clickYearUpButton();
		mDriver.cardInfoScreen().clickSetButton();
		mDriver.cardInfoScreen().typeTextNameOnCardEditText(mUser.getFirstName() + " " + mUser.getLastName());
		mDriver.cardInfoScreen().typeTextEmailEditText(mUser.getLoginEmail());
		mDriver.cardInfoScreen().clickOnDoneButton();
		mDriver.delay();

		if (mDriver.searchText(mDriver.flightsCheckoutScreen().acceptString(), 1, false, true)) {
			mDriver.screenshot("Accept TOS");
			mDriver.flightsCheckoutScreen().clickOnAcceptString();
		}
		mDriver.screenshot("Slide to checkout");
		mDriver.flightsCheckoutScreen().slideToCheckout();
		mDriver.delay();
		mDriver.screenshot("CVV Entry");
	}

	@Override
	protected void tearDown() throws Exception {
		mDriver.enterLog(TAG, "tearing down...");
		mDriver.finishOpenedActivities();
	}
}
