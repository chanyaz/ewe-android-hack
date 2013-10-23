package com.expedia.bookings.test.tests.flights;

import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.test.utils.FlightsTestDriver;
import com.expedia.bookings.test.utils.HotelsUserData;

public class FlightsHappyPath {

	/*
	 * This class has one static method, which takes a test driver
	 * and a user as its parameters. Executes a flight happy path
	 * test based upon these parameters.
	 */
	private static final String TAG = FlightsHappyPath.class.getSimpleName();

	public static void execute(FlightsTestDriver driver, HotelsUserData user) throws Exception {
		try {
			// Settings 
			driver.delay();
			driver.launchScreen().openMenuDropDown();
			driver.launchScreen().pressSettings();
			driver.settingsScreen().clickToClearPrivateData();
			if (driver.searchText(driver.settingsScreen().OKString())) {
				driver.settingsScreen().clickOKString();
			}
			else if (driver.searchText(driver.settingsScreen().AcceptString())) {
				driver.settingsScreen().clickAcceptString();
			}
			else {
				driver.clickOnText("OK");
			}
			driver.delay();
			if (driver.searchText(driver.settingsScreen().OKString())) {
				driver.settingsScreen().clickOKString();
			}
			else if (driver.searchText(driver.settingsScreen().AcceptString())) {
				driver.settingsScreen().clickAcceptString();
			}
			else {
				driver.clickOnText("OK");
			}
			driver.settingsScreen().setSpoofBookings();
			driver.settingsScreen().goBack();
			driver.enterLog(TAG, "Cleared private data and set spoof/suppress bookings");

			driver.launchScreen().openMenuDropDown();
			driver.launchScreen().pressSettings();
			driver.settingsScreen().clickSelectAPIString();
			driver.settingsScreen().scrollUp();
			driver.settingsScreen().clickOnText(user.getBookingServer());
			driver.enterLog(TAG, "Set API server to: " + user.getBookingServer());
			if (user.getBookingServer().equals("Proxy") || user.getBookingServer().equals("Mock Server")) {
				driver.enterLog(TAG, "Set server proxy to: " + user.getServerIP() + ":" + user.getServerPort());
				driver.settingsScreen().clickServerProxyAddressString();
				driver.settingsScreen().clearServerEditText();
				driver.settingsScreen().enterServerText(user.getServerIP() + ":" + user.getServerPort());
				driver.settingsScreen().clickOKString();
			}
			driver.settingsScreen().goBack();

			// Launch screen
			driver.screenshot("Launch Screen");
			driver.enterLog(TAG, "Launching flights application");
			driver.launchScreen().launchFlights();

			// Flights search screen
			driver.landscape();
			driver.portrait();
			driver.screenshot("Flights Search");
			driver.enterLog(TAG, "Set departure airport: " + user.getDepartureAirport());
			driver.flightsSearchScreen().enterDepartureAirport(user.getDepartureAirport());
			driver.enterLog(TAG, "Set arrival airport: " + user.getArrivalAirport());
			driver.flightsSearchScreen().enterArrivalAirport(user.getArrivalAirport());
			driver.landscape();
			driver.portrait();
			driver.flightsSearchScreen().clickSelectDepartureButton();
			int dateOffset = 1;
			driver.enterLog(TAG, "Selecting date with offset from current date: " + dateOffset);
			driver.flightsSearchScreen().clickDate(dateOffset);
			driver.landscape();
			driver.portrait();
			driver.enterLog(TAG, "Click search button");
			driver.flightsSearchScreen().clickSearchButton();

			// Searching
			driver.screenshot("Searching for flights");
			driver.waitForStringToBeGone(driver.flightsSearchLoading().getLoadingFlightsString(), 60);
			driver.landscape();
			driver.portrait();

			// Search results
			driver.landscape();
			driver.portrait();
			driver.enterLog(TAG, "Flight search results loaded");
			driver.screenshot("Flight search results");
			int flightIndex = 1;
			driver.enterLog(TAG, "Selecting flight at index: " + flightIndex);
			driver.flightsSearchResultsScreen().selectFlightFromList(flightIndex);
			driver.delay(1);

			// Flight leg confirmation
			driver.landscape();
			driver.portrait();
			driver.screenshot("Flight leg screen");
			driver.enterLog(TAG, "Clicking select flight button");
			driver.flightLegScreen().clickSelectFlightButton();
			driver.waitForStringToBeGone(driver.flightLegScreen().checkingForPriceChangesString(), 60);

			// Checkout
			driver.landscape();
			driver.portrait();
			driver.flightsCheckoutScreen().clickCheckoutButton();
			driver.enterLog(TAG, "Clicked checkout button");
			driver.screenshot("Flights checkout");
			driver.flightsCheckoutScreen().clickLogInButton();

			// Log in
			driver.landscape();
			driver.portrait();
			driver.screenshot("Login Screen");
			driver.enterLog(TAG, "Logging in for this booking using email " + user.getLoginEmail());
			driver.logInScreen().typeTextEmailEditText(user.getLoginEmail());
			driver.logInScreen().typeTextPasswordEditText(user.getLoginPassword());
			driver.logInScreen().clickOnLoginButton();
			driver.waitForStringToBeGone(driver.logInScreen().loggingInDialogString(), 45);

			// Enter new payment
			driver.landscape();
			driver.portrait();
			driver.flightsCheckoutScreen().clickSelectPaymentButton();
			driver.screenshot("Select Payment");
			driver.commonPaymentMethodScreen().clickOnAddNewCardTextView();

			if (PointOfSale.getPointOfSale().requiresBillingAddressFlights()) {
				driver.screenshot("Address");
				driver.landscape();
				driver.portrait();
				driver.enterLog(TAG, "Entering address line 1: " + user.getAddressLine1());
				driver.billingAddressScreen().typeTextAddressLineOne(user.getAddressLine1());
				driver.enterLog(TAG, "Entering address city: " + user.getAddressCity());
				driver.billingAddressScreen().typeTextCity(user.getAddressCity());
				driver.enterLog(TAG, "Entering address state code: " + user.getAddressStateCode());
				driver.billingAddressScreen().typeTextState(user.getAddressStateCode());
				driver.enterLog(TAG, "Entering postal code: " + user.getAddressPostalCode());
				driver.billingAddressScreen().typeTextPostalCode(user.getAddressPostalCode());
				driver.billingAddressScreen().clickNextButton();
			}

			driver.landscape();
			driver.portrait();
			driver.enterLog(TAG, "Using new credit card");
			driver.screenshot("Card info");
			driver.enterLog(TAG, "Entering credit card with number: " + user.getCreditCardNumber());
			driver.cardInfoScreen().typeTextCreditCardEditText(user.getCreditCardNumber());
			driver.cardInfoScreen().clickOnExpirationDateButton();
			driver.enterLog(TAG, "Incrementing credit card exp. month and year by 1");
			driver.cardInfoScreen().clickMonthUpButton();
			driver.cardInfoScreen().clickYearUpButton();
			driver.cardInfoScreen().clickSetButton();
			driver.cardInfoScreen().clickOnDoneButton();
			driver.cardInfoScreen().clickNoThanksButton();
			driver.delay(1);

			if (driver.searchText(driver.flightsCheckoutScreen().addTravelerString(), 1, false, true)) {
				driver.flightsCheckoutScreen().clickAddTravelerString();
				driver.travelerInformationScreen().clickEnterANewTraveler();
				driver.travelerInformationScreen().enterLastName(user.getLastName());
				driver.enterLog(TAG, "Entering last name: " + user.getLastName());
				driver.travelerInformationScreen().enterFirstName(user.getFirstName());
				driver.enterLog(TAG, "Entering first name: " + user.getFirstName());
				driver.travelerInformationScreen().enterPhoneNumber(user.getPhoneNumber());
				driver.enterLog(TAG, "Entering phone number: " + user.getPhoneNumber());
				driver.travelerInformationScreen().clickBirthDateButton();
				driver.travelerInformationScreen().clickDoneString();
				driver.billingAddressScreen().clickNextButton();
				driver.travelerInformationScreen().clickDoneButton();
				driver.cardInfoScreen().clickNoThanksButton();
			}

			driver.landscape();
			driver.portrait();
			if (driver.searchText(driver.flightsCheckoutScreen().acceptString(), 1, false, true)) {
				driver.flightsCheckoutScreen().clickOnAcceptString();
			}
			driver.screenshot("Slide to checkout");
			driver.enterLog(TAG, "Sliding to checkout");
			driver.flightsCheckoutScreen().slideToCheckout();
			driver.delay();

			driver.screenshot("CVV Entry");
			driver.enterLog(TAG, "Entering CCV: " + user.getCCV());
			driver.cvvEntryScreen().parseAndEnterCVV(user.getCCV());
			driver.cvvEntryScreen().clickBookButton();
			driver.delay(1);
			driver.waitForStringToBeGone(driver.cvvEntryScreen().booking(), 60);
			driver.delay(1);
			driver.screenshot("Confirmation Screen");
			driver.landscape();
			driver.portrait();
			driver.enterLog(TAG, "Going back to launch screen.");
			driver.flightsConfirmationScreen().clickDoneButton();
			driver.enterLog(TAG, "Clicking shop tab");
			driver.launchScreen().pressShop();
		}
		catch (Error e) {
			driver.getScreenShotUtility().screenshot(TAG + "-FAILURE");
			throw e;
		}
		catch (Exception e) {
			driver.getScreenShotUtility().screenshot(TAG + "-FAILURE");
			throw e;
		}
	}
}
