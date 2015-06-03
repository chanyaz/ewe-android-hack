package com.expedia.bookings.test.ui.phone.tests.flights;

import android.support.test.espresso.Espresso;

import com.expedia.bookings.R;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.test.ui.phone.pagemodels.common.BillingAddressScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.CVVEntryScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.CardInfoScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.CommonCheckoutScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.CommonPaymentMethodScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.LaunchScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.LogInScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.ScreenActions;
import com.expedia.bookings.test.ui.phone.pagemodels.flights.FlightLegScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.flights.FlightsSearchResultsScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.flights.FlightsSearchScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.hotels.HotelsCheckoutScreen;
import com.expedia.bookings.test.ui.utils.HotelsUserData;
import com.expedia.bookings.test.ui.utils.PhoneTestCase;

import org.joda.time.LocalDate;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static com.expedia.bookings.test.ui.utils.EspressoUtils.assertViewWithTextIsDisplayed;
import static com.expedia.bookings.test.ui.utils.EspressoUtils.getText;

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
		ScreenActions.enterLog(TAG, "START TEST: Testing confirmation screen for air attach messaging");
		testAirAttach();
	}

	private void testAirAttach() {
		assertViewWithTextIsDisplayed(R.id.itin_air_attach_text_view, "Because you booked a flight");
		assertViewWithTextIsDisplayed(R.id.itin_air_attach_savings_text_view, "Save up to 55% on a hotel.");
		assertViewWithTextIsDisplayed(R.id.itin_air_attach_expires_text_view, "Offer expires in");
		assertViewWithTextIsDisplayed(R.id.itin_air_attach_expiration_date_text_view, "10 days");
		assertViewWithTextIsDisplayed(R.id.action_text_view, "Add a hotel");
		onView(withId(R.id.action_text_view)).perform(click());
		// assert we're seeing hotel search results
		assertViewWithTextIsDisplayed("vip_hotel");
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
		FlightsSearchResultsScreen.clickListItem(7);
		FlightLegScreen.clickSelectFlightButton();
		String airlineName = getText(R.id.airline_text_view);
		String departureTime = getText(R.id.departure_time_text_view);
		String arrivalTime = getText(R.id.arrival_time_text_view);

		// Checkout
		CommonCheckoutScreen.clickCheckoutButton();
		ScreenActions.enterLog(TAG, "Clicked checkout button");

		// Log in
		HotelsCheckoutScreen.clickLogInButton();
		LogInScreen.typeTextEmailEditText(mUser.getLoginEmail());
		LogInScreen.typeTextPasswordEditText(mUser.getLoginPassword());
		LogInScreen.clickOnLoginButton();
		Espresso.pressBack();
		CommonCheckoutScreen.clickCheckoutButton();

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
		CommonCheckoutScreen.slideToCheckout();

		// CVV Entry
		ScreenActions.enterLog(TAG, "Entering CCV: " + mUser.getCCV());
		CVVEntryScreen.parseAndEnterCVV(mUser.getCCV());
		CVVEntryScreen.clickBookButton();
		assertViewWithTextIsDisplayed(airlineName);
		assertViewWithTextIsDisplayed(departureTime);
		assertViewWithTextIsDisplayed(arrivalTime);
		assertViewWithTextIsDisplayed("Booking Complete");
	}
}
