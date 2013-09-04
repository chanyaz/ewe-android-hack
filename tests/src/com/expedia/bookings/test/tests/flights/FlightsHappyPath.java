package com.expedia.bookings.test.tests.flights;

import com.expedia.bookings.activity.LaunchActivity;
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
			driver.launchScreen().openMenuDropDown();
			driver.launchScreen().pressSettings();
			driver.settingsScreen().clickToClearPrivateData();
			driver.settingsScreen().clickOKString();
			driver.settingsScreen().clickOKString();
			driver.settingsScreen().setSpoofBookings();
			driver.settingsScreen().goBack();

			driver.launchScreen().openMenuDropDown();
			driver.launchScreen().pressSettings();
			driver.settingsScreen().clickSelectAPIString();
			driver.settingsScreen().scrollUp();
			driver.settingsScreen().clickOnText(user.mServerName);
			if (user.mServerName.equals("Proxy")) {
				driver.settingsScreen().clickServerProxyAddressString();
				driver.settingsScreen().clearServerEditText();
				driver.settingsScreen().enterServerText(user.mProxyIP + ":" + user.mProxyPort);
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
			driver.flightsSearchScreen().enterDepartureAirport(user.mDepartureAirport);
			driver.flightsSearchScreen().enterArrivalAirport(user.mArrivalAirport);
			driver.landscape();
			driver.portrait();
			driver.flightsSearchScreen().clickSelectDepartureButton();
			driver.flightsSearchScreen().clickDate(1);
			driver.landscape();
			driver.portrait();
			driver.flightsSearchScreen().clickSearchButton();

			// Searching
			driver.screenshot("Searching for flights");
			driver.waitForViewToBeGone(driver.flightsSearchLoading().getLoadingDialogueView(), 30);
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
			driver.logInScreen().typeTextEmailEditText(user.mLoginEmail);
			driver.logInScreen().typeTextPasswordEditText(user.mLoginPassword);
			driver.logInScreen().clickOnLoginButton();
			driver.waitForStringToBeGone(driver.logInScreen().loggingInDialogString());

			// Enter new payment
			driver.landscape();
			driver.portrait();
			driver.commonCheckout().clickSelectPaymentButton();
			driver.screenshot("Select Payment");
			driver.commonPaymentMethodScreen().clickOnAddNewCardTextView();
			driver.screenshot("Add new card");
			driver.billingAddressScreen().typeTextAddressLineOne(user.mAddressLine1);
			driver.billingAddressScreen().typeTextCity(user.mCityName);
			driver.billingAddressScreen().typeTextState(user.mStateCode);
			driver.billingAddressScreen().typeTextPostalCode(user.mZIPCode);
			driver.billingAddressScreen().clickNextButton();

			driver.landscape();
			driver.portrait();
			driver.screenshot("Card info");
			driver.cardInfoScreen().typeTextCreditCardEditText(user.mCreditCardNumber);
			driver.cardInfoScreen().clickOnExpirationDateButton();
			driver.cardInfoScreen().clickMonthUpButton();
			driver.cardInfoScreen().clickYearUpButton();
			driver.cardInfoScreen().clickSetButton();
			driver.cardInfoScreen().clickOnDoneButton();
			driver.cardInfoScreen().clickNoThanksButton();

			driver.landscape();
			driver.portrait();
			driver.screenshot("Slide to checkout");
			driver.commonCheckout().slideToCheckout();
			driver.delay();

			driver.screenshot("CVV Entry");
			driver.cvvEntryScreen().parseAndEnterCVV(user.mCCV);
			driver.cvvEntryScreen().clickBookButton();
			driver.delay();
		}
		catch (Error e) {
			driver.takeScreenshotUponFailure(e, TAG);
			throw e;
		}
		catch (Exception e) {
			driver.takeScreenshotUponFailure(e, TAG);
			throw e;
		}
	}
}
