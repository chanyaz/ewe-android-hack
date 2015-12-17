package com.expedia.bookings.test.happy;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import com.expedia.bookings.R;
import com.expedia.bookings.test.phone.cars.CarScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.BillingAddressScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.CVVEntryScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.CardInfoScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.CheckoutViewModel;
import com.expedia.bookings.test.ui.phone.pagemodels.common.CommonCheckoutScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.CommonPaymentMethodScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.CommonTravelerInformationScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.LaunchScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.LogInScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.ScreenActions;
import com.expedia.bookings.test.ui.phone.pagemodels.flights.FlightLegScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.flights.FlightsSearchResultsScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.flights.FlightsSearchScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.flights.FlightsTravelerInfoScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.hotels.HotelsCheckoutScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.hotels.HotelsDetailsScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.hotels.HotelsRoomsRatesScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.hotels.HotelsSearchScreen;
import com.expedia.bookings.test.ui.tablet.pagemodels.Common;
import com.expedia.bookings.test.espresso.PhoneTestCase;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

public class PhoneHappyPathTrunk extends PhoneTestCase {


	public void testBookFlightGuestUser() throws Throwable {
		goToFlightCheckout();

		CommonCheckoutScreen.clickTravelerDetails();
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
		screenshot("Checkout_Traveler_Entered");
		ScreenActions.delay(1);
		BillingAddressScreen.clickNextButton();
		BillingAddressScreen.clickNextButton();
		FlightsTravelerInfoScreen.clickDoneButton();
		Common.pressBack();
		CommonCheckoutScreen.clickCheckoutButton();
		CommonCheckoutScreen.clickSelectPaymentButton();
		BillingAddressScreen.typeTextAddressLineOne("123 California Street");
		BillingAddressScreen.typeTextCity("San Francisco");
		BillingAddressScreen.typeTextPostalCode("94105");
		screenshot("Checkout_Address_Entered");
		BillingAddressScreen.clickNextButton();

		CardInfoScreen.typeTextCreditCardEditText("4111111111111111");
		Common.closeSoftKeyboard(CardInfoScreen.creditCardNumberEditText());
		CardInfoScreen.clickOnExpirationDateButton();
		CardInfoScreen.clickMonthUpButton();
		CardInfoScreen.clickYearUpButton();
		CardInfoScreen.clickSetButton();
		CardInfoScreen.typeTextNameOnCardEditText("Mobiata Auto");
		CardInfoScreen.typeTextEmailEditText("mobiataauto@gmail.com");
		screenshot("Checkout_Payment_Card_Entered");
		CardInfoScreen.clickOnDoneButton();

		screenshot("Slide_To_Purchase");
		CommonCheckoutScreen.slideToCheckout();
		CVVEntryScreen.parseAndEnterCVV("111");
		CVVEntryScreen.clickBookButton();
		screenshot("Confirmation");
	}

	public void testBookFlightLoggedUser() throws Throwable {
		goToFlightCheckout();

		HotelsCheckoutScreen.clickLogInButton();
		LogInScreen.typeTextEmailEditText(getInstrumentation().getContext().getString(com.expedia.bookings.test.R.string.user_name));
		LogInScreen.typeTextPasswordEditText(getInstrumentation().getContext().getString(com.expedia.bookings.test.R.string.user_password));
		LogInScreen.clickOnLoginButton();

		HotelsCheckoutScreen.clickSelectPaymentButton();
		try {
			CommonPaymentMethodScreen.clickOnAddNewCardTextView();
		}
		catch (Exception e) {
			//ignore
		}

		BillingAddressScreen.typeTextAddressLineOne("123 main st");
		BillingAddressScreen.typeTextState("CA");
		BillingAddressScreen.typeTextCity("SF");
		BillingAddressScreen.typeTextPostalCode("2323");
		BillingAddressScreen.clickNextButton();

		CardInfoScreen.typeTextCreditCardEditText("4111111111111111");
		CardInfoScreen.clickOnExpirationDateButton();
		CardInfoScreen.clickMonthUpButton();
		CardInfoScreen.clickYearUpButton();
		CardInfoScreen.clickSetButton();
		CardInfoScreen.clickOnDoneButton();
		CardInfoScreen.clickNoThanksButton();

		screenshot("Slide_To_Purchase");
		CommonCheckoutScreen.slideToCheckout();
		CVVEntryScreen.parseAndEnterCVV("111");
		CVVEntryScreen.clickBookButton();
		screenshot("Confirmation");
	}

	public void testBookHotelGuestUser() throws Throwable {
		goToHotelCheckout();

		HotelsCheckoutScreen.clickGuestDetails();
		CommonTravelerInformationScreen.enterFirstName("Mobiata");
		CommonTravelerInformationScreen.enterLastName("Auto");
		CommonTravelerInformationScreen.enterPhoneNumber("1112223333");
		CommonTravelerInformationScreen.enterEmailAddress("mobiataauto@gmail.com");
		screenshot("Checkout_Traveler_Entered");
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
		screenshot("Checkout_Payment_Entered");
		CardInfoScreen.clickOnDoneButton();

		screenshot("Slide_To_Purchase");
		HotelsCheckoutScreen.slideToCheckout();
		CVVEntryScreen.parseAndEnterCVV("111");
		CVVEntryScreen.clickBookButton();
		screenshot("Confirmation");
	}

	public void testBookHotelLoggedUser() throws Throwable {
		goToHotelCheckout();

		HotelsCheckoutScreen.clickLogInButton();
		LogInScreen.typeTextEmailEditText(getInstrumentation().getContext().getString(com.expedia.bookings.test.R.string.user_name));
		LogInScreen.typeTextPasswordEditText(getInstrumentation().getContext().getString(com.expedia.bookings.test.R.string.user_password));
		LogInScreen.clickOnLoginButton();

		HotelsCheckoutScreen.clickSelectPaymentButton();
		try {
			CommonPaymentMethodScreen.clickOnAddNewCardTextView();
		}
		catch (Exception e) {
			//ignore
		}
		CardInfoScreen.typeTextCreditCardEditText("4111111111111111");
		BillingAddressScreen.typeTextPostalCode("1234");
		CardInfoScreen.typeTextNameOnCardEditText("Expedia Auto");
		CardInfoScreen.clickOnExpirationDateButton();
		CardInfoScreen.clickMonthUpButton();
		CardInfoScreen.clickYearUpButton();
		CardInfoScreen.clickSetButton();
		CardInfoScreen.clickOnDoneButton();
		CardInfoScreen.clickNoThanksButton();

		HotelsCheckoutScreen.slideToCheckout();
		CVVEntryScreen.parseAndEnterCVV("111");
		CVVEntryScreen.clickBookButton();
		screenshot("Confirmation");
	}

	public void testBookCarGuestUser() throws Throwable {
		goToCarCheckout();

		ScreenActions.delay(3);
		CheckoutViewModel.enterTravelerInfo();
		try {
			CheckoutViewModel.enterPaymentInfo();
			CheckoutViewModel.pressClose();
		}
		catch (Exception e) {
			//payment details not required.
		}
		CheckoutViewModel.performSlideToPurchase();
		try {
			CVVEntryScreen.parseAndEnterCVV("111");
			CVVEntryScreen.clickBookButton();
		}
		catch (Exception e) {
			//cvv not required
		}
	}

	public void testBookCarLoggedUser() throws Throwable {
		goToCarCheckout();

		onView(withId(R.id.login_widget)).perform(click());
		LogInScreen.typeTextEmailEditText(getInstrumentation().getContext().getString(com.expedia.bookings.test.R.string.user_name));
		LogInScreen.typeTextPasswordEditText(getInstrumentation().getContext().getString(com.expedia.bookings.test.R.string.user_password));
		LogInScreen.clickOnLoginButton();

		try {
			CheckoutViewModel.enterPaymentInfo();
			CheckoutViewModel.pressClose();
		}
		catch (Exception e) {
			//payment details not required.
		}
		CheckoutViewModel.performSlideToPurchase();
		try {
			CVVEntryScreen.parseAndEnterCVV("111");
			CVVEntryScreen.clickBookButton();
		}
		catch (Exception e) {
			//cvv not required
		}
	}

	//helper methods

	public void goToFlightCheckout() throws Throwable {
		screenshot("Launch");
		LaunchScreen.launchFlights();
		FlightsSearchScreen.enterDepartureAirport("MKE");
		FlightsSearchScreen.enterArrivalAirport("LON");
		FlightsSearchScreen.clickSelectDepartureButton();
		LocalDate startDate = LocalDate.now().plusDays(35);
		LocalDate endDate = LocalDate.now().plusDays(40);
		FlightsSearchScreen.clickDate(startDate, endDate);
		FlightsSearchScreen.clickSearchButton();
		screenshot("Search_Results");
		FlightsSearchResultsScreen.clickListItem(1);
		FlightLegScreen.clickSelectFlightButton();
		FlightsSearchResultsScreen.clickListItem(1);
		FlightLegScreen.clickSelectFlightButton();
		CommonCheckoutScreen.clickCheckoutButton();
	}

	public void goToHotelCheckout() throws Throwable {
		screenshot("Launch");
		LaunchScreen.launchHotels();
		HotelsSearchScreen.clickSearchEditText();
		HotelsSearchScreen.clickToClearSearchEditText();
		HotelsSearchScreen.enterSearchText("MKE");
		HotelsSearchScreen.clickSuggestionAtIndex(getActivity(), 1);
		LocalDate startDate = LocalDate.now().plusDays(35);
		LocalDate endDate = LocalDate.now().plusDays(40);
		HotelsSearchScreen.clickOnCalendarButton();
		HotelsSearchScreen.clickDate(startDate, endDate);
		HotelsSearchScreen.clickOnGuestsButton();
		HotelsSearchScreen.guestPicker().clickOnSearchButton();
		screenshot("Search_Results");
		HotelsSearchScreen.clickListItem(1);
		HotelsDetailsScreen.clickSelectButton();
		HotelsRoomsRatesScreen.selectETPRoomItem(1);
		HotelsCheckoutScreen.clickCheckoutButton();
	}

	public void goToCarCheckout() throws Throwable {
		LaunchScreen.launchCars();

		final DateTime startDateTime = DateTime.now().plusDays(30).withTimeAtStartOfDay();
		final DateTime endDateTime = startDateTime.plusDays(5);
		CarScreen.pickupLocation().perform(typeText("MKE"));
		CarScreen.selectPickupLocation("Milwaukee, WI");
		CarScreen.selectDateButton().perform(click());
		CarScreen.selectDates(startDateTime.toLocalDate(), endDateTime.toLocalDate());

		CarScreen.searchButton().perform(click());
		ScreenActions.delay(4);
		CarScreen.selectCarCategory(2);
		CarScreen.selectCarOffer(1);
	}

}
