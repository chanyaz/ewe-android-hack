package com.expedia.bookings.test.happy;

import org.joda.time.LocalDate;

import com.expedia.bookings.test.tests.pageModels.tablet.Common;
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
import com.expedia.bookings.test.utils.PhoneTestCase;
import com.expedia.bookings.test.utils.SpoonScreenshotUtils;

public class PhoneHappyPath extends PhoneTestCase {
	public void testBookFlight() throws Throwable {
		SpoonScreenshotUtils.screenshot("Launch", getInstrumentation());
		LaunchScreen.launchFlights();
		FlightsSearchScreen.enterDepartureAirport("LAX");
		FlightsSearchScreen.enterArrivalAirport("SFO");
		FlightsSearchScreen.clickSelectDepartureButton();
		LocalDate startDate = LocalDate.now().plusDays(35);
		LocalDate endDate = LocalDate.now().plusDays(40);
		FlightsSearchScreen.clickDate(startDate, endDate);
		SpoonScreenshotUtils.screenshot("Search", getInstrumentation());
		FlightsSearchScreen.clickSearchButton();
		SpoonScreenshotUtils.screenshot("Search_Results", getInstrumentation());
		FlightsSearchResultsScreen.clickListItem(1);
		SpoonScreenshotUtils.screenshot("Details1", getInstrumentation());
		FlightLegScreen.clickSelectFlightButton();
		SpoonScreenshotUtils.screenshot("Details2", getInstrumentation());
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
		SpoonScreenshotUtils.screenshot("Checkout_Traveler_Entered", getInstrumentation());
		BillingAddressScreen.clickNextButton();
		FlightsTravelerInfoScreen.clickDoneButton();
		Common.pressBack();
		FlightsCheckoutScreen.clickCheckoutButton();
		FlightsCheckoutScreen.clickSelectPaymentButton();
		BillingAddressScreen.typeTextAddressLineOne("123 California Street");
		BillingAddressScreen.typeTextCity("San Francisco");
		BillingAddressScreen.typeTextPostalCode("94105");
		SpoonScreenshotUtils.screenshot("Checkout_Payment_Entered", getInstrumentation());
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

		SpoonScreenshotUtils.screenshot("Slide_To_Purchase", getInstrumentation());
		FlightsCheckoutScreen.slideToCheckout();
		CVVEntryScreen.parseAndEnterCVV("111");
		SpoonScreenshotUtils.screenshot("CVV", getInstrumentation());
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
		HotelsSearchScreen.clickSuggestion(getActivity(), "New York, NY");
		LocalDate startDate = LocalDate.now().plusDays(35);
		LocalDate endDate = LocalDate.now().plusDays(40);
		HotelsSearchScreen.clickOnCalendarButton();
		HotelsSearchScreen.clickDate(startDate, endDate);
		SpoonScreenshotUtils.screenshot("Search", getInstrumentation());
		HotelsSearchScreen.clickOnGuestsButton();
		HotelsSearchScreen.guestPicker().clickOnSearchButton();
		SpoonScreenshotUtils.screenshot("Search_Results", getInstrumentation());
		HotelsSearchScreen.clickListItem(1);
		SpoonScreenshotUtils.screenshot("Details", getInstrumentation());
		HotelsDetailsScreen.clickSelectButton();
		SpoonScreenshotUtils.screenshot("RoomsAndRates", getInstrumentation());
		HotelsRoomsRatesScreen.selectRoomItem(0);
		HotelsCheckoutScreen.clickCheckoutButton();

		HotelsCheckoutScreen.clickGuestDetails();
		HotelsCheckoutScreen.clickAddTravelerButton();
		CommonTravelerInformationScreen.enterFirstName("Mobiata");
		CommonTravelerInformationScreen.enterLastName("Auto");
		CommonTravelerInformationScreen.enterPhoneNumber("1112223333");
		CommonTravelerInformationScreen.enterEmailAddress("mobiataauto@gmail.com");
		SpoonScreenshotUtils.screenshot("Checkout_Traveler_Entered", getInstrumentation());
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
		SpoonScreenshotUtils.screenshot("Checkout_Payment_Entered", getInstrumentation());
		CardInfoScreen.clickOnDoneButton();

		SpoonScreenshotUtils.screenshot("Slide_To_Purchase", getInstrumentation());
		HotelsCheckoutScreen.slideToCheckout();
		CVVEntryScreen.parseAndEnterCVV("111");
		SpoonScreenshotUtils.screenshot("CVV", getInstrumentation());
		CVVEntryScreen.clickBookButton();
		SpoonScreenshotUtils.screenshot("Confirmation", getInstrumentation());
		HotelsConfirmationScreen.clickDoneButton();
	}
}
