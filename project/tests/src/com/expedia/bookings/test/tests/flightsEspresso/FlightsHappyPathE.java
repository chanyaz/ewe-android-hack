package com.expedia.bookings.test.tests.flightsEspresso;

import java.util.Calendar;
import org.joda.time.LocalDate;

import com.expedia.bookings.R;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.BillingAddressScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.CVVEntryScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.CardInfoScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.CommonPaymentMethodScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.LaunchScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.LogInScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.ScreenActions;
import com.expedia.bookings.test.tests.pageModelsEspresso.flights.FlightLegScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.flights.FlightsCheckoutScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.flights.FlightsConfirmationScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.flights.FlightsSearchResultsScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.flights.FlightsSearchScreen;

import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.test.tests.flights.FlightsHappyPath;
import com.expedia.bookings.test.utils.HotelsUserData;

import static com.expedia.bookings.test.utils.EspressoUtils.swipeRight;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

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

		// Launch screen
		ScreenActions.enterLog(TAG, "Launching flights application");
		LaunchScreen.launchFlights();

		// Flights search screen
		ScreenActions.enterLog(TAG, "Set departure airport: " + mUser.getDepartureAirport());
		FlightsSearchScreen.enterDepartureAirport(mUser.getDepartureAirport());
		ScreenActions.enterLog(TAG, "Set arrival airport: " + mUser.getArrivalAirport());
		FlightsSearchScreen.enterArrivalAirport(mUser.getArrivalAirport());
		FlightsSearchScreen.clickSelectDepartureButton();

		Calendar cal = Calendar.getInstance();
		int year = cal.get(cal.YEAR);
		int month = cal.get(cal.MONTH) + 1;
		int date = cal.get(cal.DATE);
		LocalDate mStartDate = new LocalDate(year, month, date + 5);
		LocalDate mEndDate = new LocalDate(year, month, date + 10);
		ScreenActions.enterLog(TAG, "Selecting date with offset of 5 and start,end dates: " + mStartDate.dayOfMonth() + "," + mEndDate.getDayOfMonth());
		FlightsSearchScreen.clickDate(mStartDate, mEndDate);
		ScreenActions.enterLog(TAG, "Click search button");
		FlightsSearchScreen.clickSearchButton();

		// Search results
		ScreenActions.enterLog(TAG, "Flight search results loaded");
		ScreenActions.enterLog(TAG, "Selecting flight at index: " + 1);
		FlightsSearchResultsScreen.clickFirstListItem();

		// Flight leg confirmation
		ScreenActions.enterLog(TAG, "Clicking select flight button");
		FlightLegScreen.clickSelectFlightButton();

		FlightsSearchResultsScreen.clickFirstListItem();
		FlightLegScreen.clickSelectFlightButton();

		// Checkout
		FlightsCheckoutScreen.clickCheckoutButton();
		ScreenActions.enterLog(TAG, "Clicked checkout button");
		FlightsCheckoutScreen.clickLogInButton();

		// Log in
		ScreenActions.enterLog(TAG, "Logging in for this booking using email " + mUser.getLoginEmail());
		LogInScreen.typeTextEmailEditText(mUser.getLoginEmail());
		LogInScreen.typeTextPasswordEditText(mUser.getLoginPassword());
		LogInScreen.clickOnLoginButton();
		ScreenActions.delay(1);

		// Enter new payment
		FlightsCheckoutScreen.clickSelectPaymentButton();
		CommonPaymentMethodScreen.clickOnAddNewCardTextView();

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
		CardInfoScreen.clickOnDoneButton();
		CardInfoScreen.clickNoThanksButton();
		CardInfoScreen.delay();

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
