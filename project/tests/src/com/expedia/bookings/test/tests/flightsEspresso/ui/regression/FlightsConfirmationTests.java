package com.expedia.bookings.test.tests.flightsEspresso.ui.regression;

import org.joda.time.LocalDate;

import com.expedia.bookings.R;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.BillingAddressScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.CVVEntryScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.CardInfoScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.CommonPaymentMethodScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.LaunchScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.LogInScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.ScreenActions;
import com.expedia.bookings.test.tests.pageModelsEspresso.flights.FlightLegScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.flights.FlightsCheckoutScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.flights.FlightsSearchResultsScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.flights.FlightsSearchScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.hotels.HotelsCheckoutScreen;
import com.expedia.bookings.test.utils.EspressoUtils;
import com.expedia.bookings.test.utils.HotelsUserData;
import com.expedia.bookings.test.utils.PhoneTestCase;

/**
 * Created by dmadan on 4/30/14.
 */
public class FlightsConfirmationTests extends PhoneTestCase {

	private static final String TAG = FlightsConfirmationTests.class.getSimpleName();

	private HotelsUserData mUser;

	public void testMethod() throws Exception {
		mUser = new HotelsUserData(getInstrumentation());
		ScreenActions.enterLog(TAG, "START TEST: Testing confirmation screen for guest flight booking");
		getToCheckout();
	}

	private void getToCheckout() throws Exception {

		// Launch screen
		ScreenActions.enterLog(TAG, "Launching flights application");
		LaunchScreen.launchFlights();

		// Flights search screen
		FlightsSearchScreen.enterDepartureAirport("SFO");
		FlightsSearchScreen.enterArrivalAirport("LAS");
		FlightsSearchScreen.clickSelectDepartureButton();
		LocalDate startDate = LocalDate.now().plusDays(40);
		FlightsSearchScreen.clickDate(startDate);
		ScreenActions.enterLog(TAG, "Click search button");
		FlightsSearchScreen.clickSearchButton();

		// Search results
		ScreenActions.enterLog(TAG, "Flight search results loaded");
		FlightsSearchResultsScreen.clickListItem(1);
		FlightLegScreen.clickSelectFlightButton();
		String airlineName = EspressoUtils.getText(R.id.airline_text_view);
		String departureTime = EspressoUtils.getText(R.id.departure_time_text_view);
		String arrivalTime = EspressoUtils.getText(R.id.arrival_time_text_view);

		// Checkout
		FlightsCheckoutScreen.clickCheckoutButton();
		ScreenActions.enterLog(TAG, "Clicked checkout button");

		// Log in
		HotelsCheckoutScreen.clickLogInButton();
		LogInScreen.typeTextEmailEditText(mUser.getLoginEmail());
		LogInScreen.typeTextPasswordEditText(mUser.getLoginPassword());
		LogInScreen.clickOnLoginButton();

		// Enter payment as logged in user
		HotelsCheckoutScreen.clickSelectPaymentButton();
		ScreenActions.enterLog(TAG, "Using new credit card");
		try {
			CommonPaymentMethodScreen.clickOnAddNewCardTextView();
		}
		catch (Exception e) {
			ScreenActions.enterLog(TAG, "No Add New Card button. Proceeding anyway.");
		}

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

		//Slide to purchase
		ScreenActions.enterLog(TAG, "Sliding to checkout");
		FlightsCheckoutScreen.slideToCheckout();

		// CVV Entry
		ScreenActions.enterLog(TAG, "Entering CCV: " + mUser.getCCV());
		CVVEntryScreen.parseAndEnterCVV(mUser.getCCV());
		CVVEntryScreen.clickBookButton();
		EspressoUtils.assertViewWithTextIsDisplayed(airlineName);
		EspressoUtils.assertViewWithTextIsDisplayed(departureTime);
		EspressoUtils.assertViewWithTextIsDisplayed(arrivalTime);
		EspressoUtils.assertViewWithTextIsDisplayed("Booking Complete");
	}
}
