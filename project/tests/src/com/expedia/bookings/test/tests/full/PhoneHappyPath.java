package com.expedia.bookings.test.tests.full;

import org.joda.time.LocalDate;

import android.test.ActivityInstrumentationTestCase2;

import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.activity.SearchActivity;
import com.expedia.bookings.test.tests.pageModels.tablet.Common;
import com.expedia.bookings.test.tests.pageModels.tablet.Settings;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.BillingAddressScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.CVVEntryScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.CardInfoScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.CommonTravelerInformationScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.LaunchScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.flights.FlightLegScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.flights.FlightsCheckoutScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.flights.FlightsConfirmationScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.flights.FlightsSearchResultsScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.flights.FlightsSearchScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.flights.FlightsTravelerInfoScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.hotels.HotelsCheckoutScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.hotels.HotelsConfirmationScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.hotels.HotelsDetailsScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.hotels.HotelsRoomsRatesScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.hotels.HotelsSearchScreen;

public class PhoneHappyPath extends ActivityInstrumentationTestCase2<SearchActivity> {

	public PhoneHappyPath() {
		super(SearchActivity.class);
	}

	@Override
	public void runTest() throws Throwable {
		// These tests are only applicable to phones
		if (!ExpediaBookingApp.useTabletInterface(getInstrumentation().getTargetContext())) {
			Settings.clearPrivateData(getInstrumentation());
			// Point to the mock server
			Settings.setCustomServer(getInstrumentation(), "mocke3.mobiata.com");

			// Espresso will not launch our activity for us, we must launch it via getActivity().
			getActivity();

			super.runTest();
		}
	}

	public void testBookFlight() throws InterruptedException {
		LaunchScreen.launchFlights();
		FlightsSearchScreen.enterDepartureAirport("LAX");
		FlightsSearchScreen.enterArrivalAirport("SFO");
		FlightsSearchScreen.clickSelectDepartureButton();
		LocalDate startDate = LocalDate.now().plusDays(35);
		LocalDate endDate = LocalDate.now().plusDays(40);
		FlightsSearchScreen.clickDate(startDate, endDate);
		FlightsSearchScreen.clickSearchButton();
		FlightsSearchResultsScreen.clickListItem(1);
		FlightLegScreen.clickSelectFlightButton();
		FlightsSearchResultsScreen.clickListItem(1);
		FlightLegScreen.clickSelectFlightButton();
		FlightsCheckoutScreen.clickCheckoutButton();

		FlightsCheckoutScreen.clickTravelerDetails();
		FlightsTravelerInfoScreen.enterFirstName("Mobiata");
		FlightsTravelerInfoScreen.enterLastName("Auto");
		FlightsTravelerInfoScreen.enterPhoneNumber("1112223333");
		Common.closeSoftKeyboard(FlightsTravelerInfoScreen.phoneNumberEditText());
		FlightsTravelerInfoScreen.clickBirthDateButton();
		try {
			FlightsTravelerInfoScreen.clickSetButton();
		}
		catch (Exception e) {
			CommonTravelerInformationScreen.clickDoneString();
		}
		BillingAddressScreen.clickNextButton();
		FlightsTravelerInfoScreen.clickDoneButton();
		Common.pressBack();
		FlightsCheckoutScreen.clickCheckoutButton();
		FlightsCheckoutScreen.clickSelectPaymentButton();
		BillingAddressScreen.typeTextAddressLineOne("123 California Street");
		BillingAddressScreen.typeTextCity("San Francisco");
		BillingAddressScreen.typeTextPostalCode("94105");
		BillingAddressScreen.clickNextButton();

		CardInfoScreen.typeTextCreditCardEditText("4111111111111111");
		Common.closeSoftKeyboard(CardInfoScreen.creditCardNumberEditText());
		CardInfoScreen.clickOnExpirationDateButton();
		CardInfoScreen.clickMonthUpButton();
		CardInfoScreen.clickYearUpButton();
		CardInfoScreen.clickSetButton();
		CardInfoScreen.typeTextNameOnCardEditText("Mobiata Auto");
		CardInfoScreen.typeTextEmailEditText("mobiataauto@gmail.com");
		CardInfoScreen.clickOnDoneButton();

		FlightsCheckoutScreen.slideToCheckout();
		CVVEntryScreen.parseAndEnterCVV("111");
		CVVEntryScreen.clickBookButton();
		FlightsConfirmationScreen.clickDoneButton();
		LaunchScreen.pressShop();
	}

	public void testBookHotel() throws InterruptedException {
		LaunchScreen.launchHotels();
		HotelsSearchScreen.clickSearchEditText();
		HotelsSearchScreen.clickToClearSearchEditText();
		HotelsSearchScreen.enterSearchText("New York, NY");
		HotelsSearchScreen.clickSuggestion(getActivity(), "New York, NY");
		LocalDate startDate = LocalDate.now().plusDays(35);
		LocalDate endDate = LocalDate.now().plusDays(40);
		HotelsSearchScreen.clickOnCalendarButton();
		HotelsSearchScreen.clickDate(startDate, endDate);
		HotelsSearchScreen.clickOnGuestsButton();
		HotelsSearchScreen.guestPicker().clickOnSearchButton();
		HotelsSearchScreen.clickListItem(1);
		HotelsDetailsScreen.clickSelectButton();
		HotelsRoomsRatesScreen.selectRoomItem(0);
		HotelsCheckoutScreen.clickCheckoutButton();

		HotelsCheckoutScreen.clickGuestDetails();
		HotelsCheckoutScreen.clickAddTravelerButton();
		CommonTravelerInformationScreen.enterFirstName("Mobiata");
		CommonTravelerInformationScreen.enterLastName("Auto");
		CommonTravelerInformationScreen.enterPhoneNumber("1112223333");
		CommonTravelerInformationScreen.enterEmailAddress("mobiataauto@gmail.com");
		CommonTravelerInformationScreen.clickDoneButton();

		HotelsCheckoutScreen.clickSelectPaymentButton();
		CardInfoScreen.typeTextCreditCardEditText("4111111111111111");
		Common.closeSoftKeyboard(CardInfoScreen.creditCardNumberEditText());
		CardInfoScreen.clickOnExpirationDateButton();
		CardInfoScreen.clickMonthUpButton();
		CardInfoScreen.clickYearUpButton();
		CardInfoScreen.clickSetButton();
		CardInfoScreen.typeTextPostalCode("94015");
		CardInfoScreen.typeTextNameOnCardEditText("Mobiata Auto");
		CardInfoScreen.clickOnDoneButton();

		HotelsCheckoutScreen.slideToCheckout();
		CVVEntryScreen.parseAndEnterCVV("111");
		CVVEntryScreen.clickBookButton();
		HotelsConfirmationScreen.clickDoneButton();
		LaunchScreen.pressShop();
	}
}
