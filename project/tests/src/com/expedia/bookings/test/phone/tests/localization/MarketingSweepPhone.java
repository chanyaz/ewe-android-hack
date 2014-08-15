package com.expedia.bookings.test.phone.tests.localization;

import org.joda.time.LocalDate;

import android.test.ActivityInstrumentationTestCase2;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.SearchActivity;
import com.expedia.bookings.test.tablet.pagemodels.Settings;
import com.expedia.bookings.test.phone.pagemodels.common.LogInScreen;
import com.expedia.bookings.test.tablet.pagemodels.Common;
import com.expedia.bookings.test.phone.pagemodels.common.BillingAddressScreen;
import com.expedia.bookings.test.phone.pagemodels.common.CVVEntryScreen;
import com.expedia.bookings.test.phone.pagemodels.common.CardInfoScreen;
import com.expedia.bookings.test.phone.pagemodels.common.CommonCheckoutScreen;
import com.expedia.bookings.test.phone.pagemodels.common.CommonTravelerInformationScreen;
import com.expedia.bookings.test.phone.pagemodels.common.LaunchScreen;
import com.expedia.bookings.test.phone.pagemodels.common.ScreenActions;
import com.expedia.bookings.test.phone.pagemodels.common.SettingsScreen;
import com.expedia.bookings.test.phone.pagemodels.flights.FlightLegScreen;
import com.expedia.bookings.test.phone.pagemodels.flights.FlightsCheckoutScreen;
import com.expedia.bookings.test.phone.pagemodels.flights.FlightsConfirmationScreen;
import com.expedia.bookings.test.phone.pagemodels.flights.FlightsSearchResultsScreen;
import com.expedia.bookings.test.phone.pagemodels.flights.FlightsSearchScreen;
import com.expedia.bookings.test.phone.pagemodels.flights.FlightsTravelerInfoScreen;
import com.expedia.bookings.test.phone.pagemodels.hotels.HotelsCheckoutScreen;
import com.expedia.bookings.test.phone.pagemodels.hotels.HotelsConfirmationScreen;
import com.expedia.bookings.test.phone.pagemodels.hotels.HotelsDetailsScreen;
import com.expedia.bookings.test.phone.pagemodels.hotels.HotelsRoomsRatesScreen;
import com.expedia.bookings.test.phone.pagemodels.hotels.HotelsSearchScreen;
import com.expedia.bookings.test.utils.SpoonScreenshotUtils;
import com.mobiata.android.util.SettingUtils;

/**
 * Created by dmadan on 6/30/14.
 */
public class MarketingSweepPhone extends ActivityInstrumentationTestCase2<SearchActivity> {

	public MarketingSweepPhone() {
		super(SearchActivity.class);
	}

	@Override
	public void runTest() throws Throwable {
		if (Common.isPhone(getInstrumentation())) {
			Settings.clearPrivateData(getInstrumentation());
			SettingUtils.save(getInstrumentation().getTargetContext(), R.string.preference_which_api_to_use_key, "Production");

			// Espresso will not launch our activity for us, we must launch it via getActivity().
			getActivity();

			super.runTest();
		}
	}

	public void testBookFlight() throws Throwable {
		SpoonScreenshotUtils.screenshot("Launch", getInstrumentation());
		LaunchScreen.launchFlights();
		FlightsSearchScreen.enterDepartureAirport("LAX");
		FlightsSearchScreen.enterArrivalAirport("SFO");
		SpoonScreenshotUtils.screenshot("Flights_Search", getInstrumentation());
		FlightsSearchScreen.clickSelectDepartureButton();
		LocalDate startDate = LocalDate.now().plusDays(35);
		LocalDate endDate = LocalDate.now().plusDays(40);
		FlightsSearchScreen.clickDate(startDate, endDate);
		FlightsSearchScreen.clickSearchButton();

		SpoonScreenshotUtils.screenshot("Flights_Search_Results", getInstrumentation());
		FlightsSearchResultsScreen.clickListItem(1);
		SpoonScreenshotUtils.screenshot("Flight_leg_details1", getInstrumentation());
		FlightLegScreen.clickSelectFlightButton();
		SpoonScreenshotUtils.screenshot("Flights_Search_Results2", getInstrumentation());
		FlightsSearchResultsScreen.clickListItem(1);
		SpoonScreenshotUtils.screenshot("Flight_leg_details2", getInstrumentation());
		FlightLegScreen.clickSelectFlightButton();
		SpoonScreenshotUtils.screenshot("Flights_checkout_overview", getInstrumentation());
		FlightsCheckoutScreen.clickCheckoutButton();

		CommonCheckoutScreen.clickLogInButton();
		SpoonScreenshotUtils.screenshot("Log_in_screen", getInstrumentation());
		Common.closeSoftKeyboard(LogInScreen.logInButton());
		Common.pressBack();
		ScreenActions.delay(1);

		FlightsCheckoutScreen.clickTravelerDetails();
		SpoonScreenshotUtils.screenshot("Traveler_Details", getInstrumentation());
		FlightsTravelerInfoScreen.enterFirstName("Mobiata");
		FlightsTravelerInfoScreen.enterLastName("Auto");
		FlightsTravelerInfoScreen.enterPhoneNumber("1112223333");
		Common.closeSoftKeyboard(FlightsTravelerInfoScreen.phoneNumberEditText());
		FlightsTravelerInfoScreen.clickBirthDateButton();
		try {
			FlightsTravelerInfoScreen.clickSetButton();
		}
		catch (Exception e) {
			try {
				CommonTravelerInformationScreen.clickDoneString();
			}
			catch (Exception ex) {
				Common.pressBack();
			}
		}
		BillingAddressScreen.clickNextButton();
		SpoonScreenshotUtils.screenshot("Traveler_Details2", getInstrumentation());
		try {
			BillingAddressScreen.clickNextButton();
		}
		catch (Exception e) {
			// No next button
		}
		FlightsTravelerInfoScreen.clickDoneButton();
		Common.pressBack();
		FlightsCheckoutScreen.clickCheckoutButton();
		FlightsCheckoutScreen.clickSelectPaymentButton();
		try {
			FlightsCheckoutScreen.clickNewPaymentCard();
		}
		catch (Exception e) {
			// No add new card option
		}
		SpoonScreenshotUtils.screenshot("Payment_Details", getInstrumentation());
		try {
			BillingAddressScreen.typeTextAddressLineOne("123 California Street");
			BillingAddressScreen.typeTextCity("San Francisco");
			BillingAddressScreen.typeTextPostalCode("94105");
			BillingAddressScreen.clickNextButton();
		}
		catch (Exception e) {
			//Billing address not needed
		}
		CardInfoScreen.typeTextCreditCardEditText("4111111111111111");
		Common.closeSoftKeyboard(CardInfoScreen.creditCardNumberEditText());
		CardInfoScreen.clickOnExpirationDateButton();
		CardInfoScreen.clickMonthUpButton();
		CardInfoScreen.clickYearUpButton();
		CardInfoScreen.clickSetButton();
		CardInfoScreen.typeTextNameOnCardEditText("Mobiata Auto");
		CardInfoScreen.typeTextEmailEditText("mobiataauto@gmail.com");
		CardInfoScreen.clickOnDoneButton();

		try {
			CommonCheckoutScreen.clickIAcceptButton();
		}
		catch (Exception e) {
			//No I accept button
		}
		SpoonScreenshotUtils.screenshot("Slide_to_checkout", getInstrumentation());
		FlightsCheckoutScreen.slideToCheckout();
		CVVEntryScreen.parseAndEnterCVV("111");
		SpoonScreenshotUtils.screenshot("CVV_Entry", getInstrumentation());
		CVVEntryScreen.clickBookButton();

		SpoonScreenshotUtils.screenshot("Confirmation", getInstrumentation());
		FlightsConfirmationScreen.clickDoneButton();
	}

	public void testBookHotel() throws Throwable {
		SpoonScreenshotUtils.screenshot("Launch", getInstrumentation());
		LaunchScreen.launchHotels();
		HotelsSearchScreen.clickSearchEditText();
		HotelsSearchScreen.clickToClearSearchEditText();
		HotelsSearchScreen.enterSearchText("New York, NY");
		HotelsSearchScreen.clickSuggestion(getActivity(), "New York");
		LocalDate startDate = LocalDate.now().plusDays(35);
		LocalDate endDate = LocalDate.now().plusDays(40);
		HotelsSearchScreen.clickOnCalendarButton();
		HotelsSearchScreen.clickDate(startDate, endDate);
		SpoonScreenshotUtils.screenshot("Hotels_Search", getInstrumentation());
		HotelsSearchScreen.clickOnGuestsButton();
		HotelsSearchScreen.guestPicker().clickOnSearchButton();
		SpoonScreenshotUtils.screenshot("Hotels_Search_Results", getInstrumentation());
		HotelsSearchScreen.clickListItem(1);
		SpoonScreenshotUtils.screenshot("Hotels_Details", getInstrumentation());
		HotelsDetailsScreen.clickReviewsTitle();
		SpoonScreenshotUtils.screenshot("Hotels_Reviews", getInstrumentation());
		Common.pressBack();
		HotelsDetailsScreen.clickSelectButton();
		SpoonScreenshotUtils.screenshot("Hotel_rooms_rates", getInstrumentation());
		HotelsRoomsRatesScreen.selectRoomItem(0);
		SpoonScreenshotUtils.screenshot("Hotel_checkout", getInstrumentation());
		try {
			SettingsScreen.clickOKString();
		}
		catch (Exception e) {
			//No Great news pop-up
		}
		HotelsCheckoutScreen.clickCheckoutButton();
		CommonCheckoutScreen.clickLogInButton();
		SpoonScreenshotUtils.screenshot("Log_in", getInstrumentation());
		Common.closeSoftKeyboard(LogInScreen.logInButton());
		Common.pressBack();
		ScreenActions.delay(1);
		HotelsCheckoutScreen.clickGuestDetails();
		try {
			HotelsCheckoutScreen.clickEnterInfoButton();
		}
		catch (Exception e) {
			//No Enter info manually button
		}
		SpoonScreenshotUtils.screenshot("Traveler_Details", getInstrumentation());
		CommonTravelerInformationScreen.enterFirstName("Mobiata");
		CommonTravelerInformationScreen.enterLastName("Auto");
		CommonTravelerInformationScreen.enterPhoneNumber("1112223333");
		CommonTravelerInformationScreen.enterEmailAddress("mobiataauto@gmail.com");
		CommonTravelerInformationScreen.clickDoneButton();

		HotelsCheckoutScreen.clickSelectPaymentButton();
		SpoonScreenshotUtils.screenshot("Payment_Details", getInstrumentation());
		CardInfoScreen.typeTextCreditCardEditText("4111111111111111");
		Common.closeSoftKeyboard(CardInfoScreen.creditCardNumberEditText());
		CardInfoScreen.clickOnExpirationDateButton();
		CardInfoScreen.clickMonthUpButton();
		CardInfoScreen.clickYearUpButton();
		CardInfoScreen.clickSetButton();
		CardInfoScreen.typeTextPostalCode("94015");
		CardInfoScreen.typeTextNameOnCardEditText("Mobiata Auto");
		CardInfoScreen.clickOnDoneButton();
		try {
			CommonCheckoutScreen.clickIAcceptButton();
		}
		catch (Exception e) {
			//No I accept button
		}
		SpoonScreenshotUtils.screenshot("Slide_to_checkout", getInstrumentation());
		HotelsCheckoutScreen.slideToCheckout();
		CVVEntryScreen.parseAndEnterCVV("111");
		SpoonScreenshotUtils.screenshot("CVV_Entry", getInstrumentation());

		CVVEntryScreen.clickBookButton();
		SpoonScreenshotUtils.screenshot("Confirmation", getInstrumentation());
		HotelsConfirmationScreen.clickDoneButton();
	}

	@Override
	public void tearDown() throws Exception {
		super.tearDown();
		if (Common.isPhone(getInstrumentation())) {
			Common.pressBackOutOfApp();
		}
	}
}
