package com.expedia.bookings.test.ui.phone.tests.localization;

import org.joda.time.LocalDate;

import com.expedia.bookings.data.pos.PointOfSaleId;
import com.expedia.bookings.test.ui.phone.pagemodels.common.ConfirmationScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.LogInScreen;
import com.expedia.bookings.test.ui.tablet.pagemodels.Common;
import com.expedia.bookings.test.ui.phone.pagemodels.common.BillingAddressScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.CVVEntryScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.CardInfoScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.CommonCheckoutScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.CommonTravelerInformationScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.LaunchScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.ScreenActions;
import com.expedia.bookings.test.ui.phone.pagemodels.common.SettingsScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.flights.FlightLegScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.flights.FlightsSearchResultsScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.flights.FlightsSearchScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.flights.FlightsTravelerInfoScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.hotels.HotelsCheckoutScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.hotels.HotelsConfirmationScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.hotels.HotelsDetailsScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.hotels.HotelsRoomsRatesScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.hotels.HotelsSearchScreen;
import com.expedia.bookings.test.espresso.PhoneTestCase;

import static android.support.test.espresso.action.ViewActions.clearText;
import static com.expedia.bookings.test.ui.phone.pagemodels.common.CommonCheckoutScreen.clickCheckoutButton;
import static com.expedia.bookings.test.ui.phone.pagemodels.common.CommonCheckoutScreen.clickNewPaymentCard;
import static com.expedia.bookings.test.ui.phone.pagemodels.common.CommonCheckoutScreen.clickSelectPaymentButton;
import static com.expedia.bookings.test.ui.phone.pagemodels.common.CommonCheckoutScreen.clickTravelerDetails;
import static com.expedia.bookings.test.ui.phone.pagemodels.common.CommonCheckoutScreen.slideToCheckout;

/**
 * Created by dmadan on 6/30/14.
 */
public class ScreenshotSweepPhone extends PhoneTestCase {

	public void testBookFlight() throws Throwable {
		setLocale(getLocale());
		setPOS(PointOfSaleId.valueOf(getPOS(getLocale())));

		screenshot("Launch");
		LaunchScreen.launchFlights();
		FlightsSearchScreen.enterDepartureAirport("LAX");
		FlightsSearchScreen.enterArrivalAirport("SFO");
		screenshot("Flights_Search");
		FlightsSearchScreen.clickSelectDepartureButton();
		LocalDate startDate = LocalDate.now().plusDays(35);
		LocalDate endDate = LocalDate.now().plusDays(40);
		FlightsSearchScreen.clickDate(startDate, endDate);
		FlightsSearchScreen.clickSearchButton();

		screenshot("Flights_Search_Results");
		FlightsSearchResultsScreen.clickListItem(1);
		screenshot("Flight_leg_details1");
		FlightLegScreen.clickSelectFlightButton();
		screenshot("Flights_Search_Results2");
		FlightsSearchResultsScreen.clickListItem(1);
		screenshot("Flight_leg_details2");
		FlightLegScreen.clickSelectFlightButton();
		screenshot("Flights_checkout_overview");
		clickCheckoutButton();

		CommonCheckoutScreen.clickLogInButton();
		screenshot("Log_in_screen");
		Common.closeSoftKeyboard(LogInScreen.logInButton());
		Common.pressBack();
		ScreenActions.delay(1);

		clickTravelerDetails();
		screenshot("Traveler_Details");
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
		screenshot("Traveler_Details2");
		try {
			BillingAddressScreen.clickNextButton();
		}
		catch (Exception e) {
			// No next button
		}
		FlightsTravelerInfoScreen.clickDoneButton();
		Common.pressBack();
		clickCheckoutButton();
		clickSelectPaymentButton();
		try {
			clickNewPaymentCard();
		}
		catch (Exception e) {
			// No add new card option
		}
		screenshot("Payment_Details");
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
		CardInfoScreen.nameOnCardEditText().perform(clearText());
		CardInfoScreen.emailEditText().perform(clearText());
		CardInfoScreen.typeTextNameOnCardEditText("Mobiata Auto");
		CardInfoScreen.typeTextEmailEditText("mobiataauto@gmail.com");
		CardInfoScreen.clickOnDoneButton();

		try {
			CommonCheckoutScreen.clickIAcceptButton();
		}
		catch (Exception e) {
			//No I accept button
		}
		screenshot("Slide_to_checkout");
		slideToCheckout();
		CVVEntryScreen.parseAndEnterCVV("111");
		screenshot("CVV_Entry");
		CVVEntryScreen.clickBookButton();

		screenshot("Confirmation");
		ConfirmationScreen.clickDoneButton();
	}

	public void testBookHotel() throws Throwable {
		setLocale(getLocale());
		setPOS(PointOfSaleId.valueOf(getPOS(getLocale())));

		screenshot("Launch");
		LaunchScreen.launchHotels();
		HotelsSearchScreen.clickSearchEditText();
		HotelsSearchScreen.clickToClearSearchEditText();
		HotelsSearchScreen.enterSearchText("New York, NY");
		HotelsSearchScreen.clickSuggestionAtIndex(getActivity(), 1);
		LocalDate startDate = LocalDate.now().plusDays(35);
		LocalDate endDate = LocalDate.now().plusDays(40);
		HotelsSearchScreen.clickOnCalendarButton();
		HotelsSearchScreen.clickDate(startDate, endDate);
		screenshot("Hotels_Search");
		HotelsSearchScreen.clickOnGuestsButton();
		HotelsSearchScreen.guestPicker().clickOnSearchButton();
		screenshot("Hotels_Search_Results");
		HotelsSearchScreen.clickListItem(1);
		screenshot("Hotels_Details");
		try {
			HotelsDetailsScreen.clickReviewsTitle();
			screenshot("Hotels_Reviews");
			Common.pressBack();
		}
		catch (Exception e) {
			//no reviews
		}
		HotelsDetailsScreen.clickSelectButton();
		screenshot("Hotel_rooms_rates");
		HotelsRoomsRatesScreen.selectETPRoomItem(1);
		screenshot("Hotel_checkout");
		try {
			SettingsScreen.clickOkString();
		}
		catch (Exception e) {
			//No Great news pop-up
		}
		HotelsCheckoutScreen.clickCheckoutButton();
		CommonCheckoutScreen.clickLogInButton();
		screenshot("Log_in");
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
		screenshot("Traveler_Details");
		CommonTravelerInformationScreen.enterFirstName("Mobiata");
		CommonTravelerInformationScreen.enterLastName("Auto");
		CommonTravelerInformationScreen.enterPhoneNumber("1112223333");
		CommonTravelerInformationScreen.enterEmailAddress("mobiataauto@gmail.com");
		CommonTravelerInformationScreen.clickDoneButton();

		HotelsCheckoutScreen.clickSelectPaymentButton();
		screenshot("Payment_Details");
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
		screenshot("Slide_to_checkout");
		HotelsCheckoutScreen.slideToCheckout();
		CVVEntryScreen.parseAndEnterCVV("111");
		screenshot("CVV_Entry");

		CVVEntryScreen.clickBookButton();
		screenshot("Confirmation");
		HotelsConfirmationScreen.clickDoneButton();

		//go to Launch screen to book a flight
		Common.pressBack();
	}
}
