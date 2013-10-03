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
	private static final String TAG = "FlightsHappyPath";

	public static void execute(FlightsTestDriver driver, HotelsUserData user) throws Exception {
		try {
			// Sweepstakes Screen
			driver.landscape();
			driver.portrait();

			try {
				driver.sweepstakesScreen().clickNoThanksButton();
			}
			catch (Throwable e) {
				driver.enterLog(TAG, "No sweepstakes activity to interact with!");
			}

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

			driver.launchScreen().openMenuDropDown();
			driver.launchScreen().pressSettings();
			driver.settingsScreen().clickSelectAPIString();
			driver.settingsScreen().scrollUp();
			driver.settingsScreen().clickOnText(user.getBookingServer());
			if (user.getBookingServer().equals("Proxy") || user.getBookingServer().equals("Mock Server")) {
				driver.settingsScreen().clickServerProxyAddressString();
				driver.settingsScreen().clearServerEditText();
				driver.settingsScreen().enterServerText(user.getServerIP() + ":" + user.getServerPort());
				driver.settingsScreen().clickOKString();
			}
			driver.settingsScreen().goBack();

			// Launch screen
			driver.landscape();
			driver.portrait();
			driver.screenshot("Launch Screen");
			driver.launchScreen().launchFlights();

			// Flights search screen
			driver.landscape();
			driver.portrait();
			driver.screenshot("Flights Search");
			driver.flightsSearchScreen().enterDepartureAirport(user.getDepartureAirport());
			driver.flightsSearchScreen().enterArrivalAirport(user.getArrivalAirport());
			driver.landscape();
			driver.portrait();
			driver.flightsSearchScreen().clickSelectDepartureButton();
			driver.flightsSearchScreen().clickDate(1);
			driver.landscape();
			driver.portrait();
			driver.flightsSearchScreen().clickSearchButton();

			// Searching
			driver.screenshot("Searching for flights");
			driver.waitForStringToBeGone(driver.flightsSearchLoading().getLoadingFlightsString());
			driver.landscape();
			driver.portrait();

			// Search results
			driver.landscape();
			driver.portrait();
			driver.screenshot("Flight search results");
			driver.flightsSearchResultsScreen().selectFlightFromList(1);
			driver.delay(1);

			// Flight leg confirmation
			driver.landscape();
			driver.portrait();
			driver.screenshot("Flight leg screen");
			driver.flightLegScreen().clickSelectFlightButton();
			driver.waitForStringToBeGone(driver.flightLegScreen().checkingForPriceChangesString());

			// Checkout
			driver.landscape();
			driver.portrait();
			driver.commonCheckout().clickCheckoutButton();
			driver.screenshot("Flights checkout");
			driver.commonCheckout().clickLogInButton();

			// Log in
			driver.landscape();
			driver.portrait();
			driver.screenshot("Login Screen");
			driver.logInScreen().typeTextEmailEditText(user.getLoginEmail());
			driver.logInScreen().typeTextPasswordEditText(user.getLoginPassword());
			driver.logInScreen().clickOnLoginButton();
			driver.waitForStringToBeGone(driver.logInScreen().loggingInDialogString());

			// Enter new payment
			driver.landscape();
			driver.portrait();
			driver.commonCheckout().clickSelectPaymentButton();
			driver.screenshot("Select Payment");
			driver.commonPaymentMethodScreen().clickOnAddNewCardTextView();

			if (PointOfSale.getPointOfSale().requiresBillingAddressFlights()) {
				driver.screenshot("Address");
				driver.landscape();
				driver.portrait();
				driver.billingAddressScreen().typeTextAddressLineOne(user.getAddressLine1());
				driver.billingAddressScreen().typeTextCity(user.getAddressCity());
				driver.billingAddressScreen().typeTextState(user.getAddressStateCode());
				driver.billingAddressScreen().typeTextPostalCode(user.getAddressPostalCode());
				driver.billingAddressScreen().clickNextButton();
			}

			driver.landscape();
			driver.portrait();
			driver.screenshot("Card info");
			driver.cardInfoScreen().typeTextCreditCardEditText(user.getCreditCardNumber());
			driver.cardInfoScreen().clickOnExpirationDateButton();
			driver.cardInfoScreen().clickMonthUpButton();
			driver.cardInfoScreen().clickYearUpButton();
			driver.cardInfoScreen().clickSetButton();
			driver.cardInfoScreen().clickOnDoneButton();
			driver.cardInfoScreen().clickNoThanksButton();
			driver.delay(1);

			if (driver.searchText(driver.commonCheckout().addTravelerString(), 1, false, true)) {
				driver.commonCheckout().clickAddTravelerString();
				driver.travelerInformationScreen().clickEnterANewTraveler();
				driver.travelerInformationScreen().enterLastName(user.getLastName());
				driver.travelerInformationScreen().enterFirstName(user.getFirstName());
				driver.travelerInformationScreen().enterPhoneNumber(user.getPhoneNumber());
				driver.travelerInformationScreen().clickBirthDateButton();
				driver.travelerInformationScreen().clickDoneString();
				driver.billingAddressScreen().clickNextButton();
				driver.travelerInformationScreen().clickDoneButton();
				driver.cardInfoScreen().clickNoThanksButton();
			}

			driver.landscape();
			driver.portrait();
			if (driver.searchText(driver.commonCheckout().acceptString(), 1, false, true)) {
				driver.commonCheckout().clickOnAcceptString();
			}
			driver.screenshot("Slide to checkout");
			driver.commonCheckout().slideToCheckout();
			driver.delay();

			driver.screenshot("CVV Entry");
			driver.cvvEntryScreen().parseAndEnterCVV(user.getCCV());
			driver.cvvEntryScreen().clickBookButton();
			driver.delay(1);
			driver.waitForStringToBeGone(driver.cvvEntryScreen().booking());
			driver.delay(1);
			driver.screenshot("Confirmation screen");
			driver.landscape();
			driver.portrait();
			driver.flightsConfirmationScreen().clickDoneButton();
			driver.delay();
			driver.tripsScreen().swipeToLaunchScreen();
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
