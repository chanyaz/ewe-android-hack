package com.expedia.bookings.test.tests.flightsEspresso;

import java.util.Calendar;

import org.joda.time.LocalDate;

import com.expedia.bookings.test.tests.pageModelsEspresso.flights.FlightsConfirmationScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.flights.FlightsTravelerInfoScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.BillingAddressScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.CVVEntryScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.CardInfoScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.LaunchScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.ScreenActions;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.SettingsScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.flights.FlightLegScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.flights.FlightsCheckoutScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.flights.FlightsSearchResultsScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.flights.FlightsSearchScreen;

import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.test.tests.flights.FlightsHappyPath;
import com.expedia.bookings.test.utils.HotelsUserData;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.pressBack;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;

/**
 * Created by dmadan on 4/9/14.
 */
public class FlightsHappyPathE {
	/*
	 * This class has one static method, which takes a test driver
	 * and a user as its parameters. Executes a flight happy path
	 * test based upon these parameters.
	 */
	private static final String TAG = FlightsHappyPath.class.getSimpleName();

	public static void execute(HotelsUserData mUser) throws Exception {

		//Settings
		LaunchScreen.openMenuDropDown();
		LaunchScreen.pressSettings();
		SettingsScreen.clickStubConfigPage();
		SettingsScreen.clickMobileFlightCheckoutScenario();
		pressBack();

		// Launch screen
		ScreenActions.enterLog(TAG, "Launching flights application");
		LaunchScreen.launchFlights();

		// Flights search screen
		ScreenActions.enterLog(TAG, "Set departure airport: " + mUser.getDepartureAirport());
		FlightsSearchScreen.enterDepartureAirport("SEA");
		ScreenActions.enterLog(TAG, "Set arrival airport: " + mUser.getArrivalAirport());
		FlightsSearchScreen.enterArrivalAirport("ORD");
		FlightsSearchScreen.clickSelectDepartureButton();

		Calendar cal = Calendar.getInstance();
		int year = cal.get(cal.YEAR);
		int month = cal.get(cal.MONTH) + 1;
		LocalDate mStartDate = new LocalDate(year, month, 5);
		LocalDate mEndDate = new LocalDate(year, month, 10);
		ScreenActions.enterLog(TAG, "Selecting date with start,end dates: " + mStartDate.dayOfMonth() + "," + mEndDate.getDayOfMonth());
		FlightsSearchScreen.clickDate(mStartDate, mEndDate);
		ScreenActions.enterLog(TAG, "Click search button");
		FlightsSearchScreen.clickSearchButton();

		// Search results
		ScreenActions.enterLog(TAG, "Flight search results loaded");
		ScreenActions.enterLog(TAG, "Selecting flight at index: " + 1);
		FlightsSearchResultsScreen.clickListItem(1);

		// Flight leg confirmation
		ScreenActions.enterLog(TAG, "Clicking select flight button");
		FlightLegScreen.clickSelectFlightButton();

		FlightsSearchResultsScreen.clickListItem(1);
		FlightLegScreen.clickSelectFlightButton();

		// Checkout
		FlightsCheckoutScreen.clickCheckoutButton();
		ScreenActions.enterLog(TAG, "Clicked checkout button");

		//Add traveler details
		onView(withText("Traveler details")).perform(click());
		FlightsTravelerInfoScreen.enterLastName(mUser.getLastName());
		ScreenActions.enterLog(TAG, "Entering last name: " + mUser.getLastName());
		FlightsTravelerInfoScreen.enterFirstName(mUser.getFirstName());
		ScreenActions.enterLog(TAG, "Entering first name: " + mUser.getFirstName());
		FlightsTravelerInfoScreen.enterPhoneNumber(mUser.getPhoneNumber());
		ScreenActions.enterLog(TAG, "Entering phone number: " + mUser.getPhoneNumber());
		FlightsTravelerInfoScreen.clickBirthDateButton();
		FlightsTravelerInfoScreen.clickSetButton();
		BillingAddressScreen.clickNextButton();
		FlightsTravelerInfoScreen.clickDoneButton();
		ScreenActions.delay(1);

		// Enter new payment
		FlightsCheckoutScreen.clickSelectPaymentButton();
		if (PointOfSale.getPointOfSale().requiresBillingAddressFlights()) {
			ScreenActions.enterLog(TAG, "Entering address line 1: " + mUser.getAddressLine1());
			BillingAddressScreen.typeTextAddressLineOne(mUser.getAddressLine1());
			ScreenActions.enterLog(TAG, "Entering address city: " + mUser.getAddressCity());
			BillingAddressScreen.typeTextCity(mUser.getAddressCity());
			ScreenActions.enterLog(TAG, "Entering postal code: " + mUser.getAddressPostalCode());
			BillingAddressScreen.typeTextPostalCode(mUser.getAddressPostalCode());
			BillingAddressScreen.clickNextButton();
		}
		ScreenActions.enterLog(TAG, "Using new credit card");
		ScreenActions.enterLog(TAG, "Entering credit card with number: " + mUser.getCreditCardNumber());
		CardInfoScreen.typeTextCreditCardEditText(mUser.getCreditCardNumber());
		CardInfoScreen.clickOnExpirationDateButton();
		ScreenActions.enterLog(TAG, "Incrementing credit card exp. month and year by 1");
		CardInfoScreen.clickMonthUpButton();
		CardInfoScreen.clickYearUpButton();
		CardInfoScreen.clickSetButton();
		CardInfoScreen.typeTextNameOnCardEditText(mUser.getFirstName() + " " + mUser.getLastName());
		CardInfoScreen.typeTextEmailEditText(mUser.getLoginEmail());
		CardInfoScreen.clickOnDoneButton();

		//Slide to purchase
		ScreenActions.enterLog(TAG, "Sliding to checkout");
		FlightsCheckoutScreen.slideToCheckout();
		ScreenActions.enterLog(TAG, "Checked out");

		ScreenActions.enterLog(TAG, "Entering CCV: " + mUser.getCCV());
		CVVEntryScreen.parseAndEnterCVV(mUser.getCCV());
		CVVEntryScreen.clickBookButton();
		ScreenActions.enterLog(TAG, "Going back to launch screen.");
		FlightsConfirmationScreen.clickDoneButton();
		ScreenActions.enterLog(TAG, "Clicking shop tab");
		LaunchScreen.pressShop();
	}
}
