package com.expedia.bookings.test.ui.phone.tests.localization;

import java.util.Locale;

import org.joda.time.LocalDate;

import com.expedia.bookings.R;
import com.expedia.bookings.data.pos.PointOfSaleId;
import com.expedia.bookings.test.ui.phone.pagemodels.common.BillingAddressScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.CVVEntryScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.CardInfoScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.CommonCheckoutScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.CommonTravelerInformationScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.LaunchScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.ScreenActions;
import com.expedia.bookings.test.ui.phone.pagemodels.common.SettingsScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.flights.FlightLegScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.flights.FlightsCheckoutScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.flights.FlightsConfirmationScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.flights.FlightsSearchResultsScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.flights.FlightsSearchScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.flights.FlightsTravelerInfoScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.hotels.HotelsCheckoutScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.hotels.HotelsConfirmationScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.hotels.HotelsDetailsScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.hotels.HotelsRoomsRatesScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.hotels.HotelsSearchScreen;
import com.expedia.bookings.test.ui.tablet.pagemodels.Common;
import com.expedia.bookings.test.ui.utils.EspressoUtils;
import com.expedia.bookings.test.ui.utils.PhoneTestCase;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static com.expedia.bookings.test.ui.espresso.ViewActions.setText;

/**
 * Created by dmadan on 1/14/15.
 */
public class FieldValidationTest extends PhoneTestCase {

	/*
 	* #373 eb_tp test plan
 	*/

	public void bookFlight() throws Throwable {
		LaunchScreen.launchFlights();
		FlightsSearchScreen.enterDepartureAirport("LAX");
		FlightsSearchScreen.enterArrivalAirport("SFO");
		FlightsSearchScreen.clickSelectDepartureButton();
		LocalDate startDate = LocalDate.now().plusDays(35);
		LocalDate endDate = LocalDate.now().plusDays(36);
		FlightsSearchScreen.clickDate(startDate, endDate);
		FlightsSearchScreen.clickSearchButton();
		FlightsSearchResultsScreen.clickListItem(1);
		FlightLegScreen.clickSelectFlightButton();
		FlightsSearchResultsScreen.clickListItem(1);
		FlightLegScreen.clickSelectFlightButton();
		FlightsCheckoutScreen.clickCheckoutButton();

		FlightsCheckoutScreen.clickTravelerDetails();

		//test field validation for multibyte character
		onView(withId(R.id.edit_first_name)).perform(setText("ш"));
		assertPopup();
		onView(withId(R.id.edit_last_name)).perform(setText("ш"));
		assertPopup();

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
		try {
			FlightsCheckoutScreen.clickNewPaymentCard();
		}
		catch (Exception e) {
			// No add new card option
		}
		try {
			//test field validation for multibyte character
			onView(withId(R.id.edit_address_line_one)).perform(setText("ш"));
			assertPopup();
			onView(withId(R.id.edit_address_city)).perform(setText("ш"));
			assertPopup();
			onView(withId(R.id.edit_address_postal_code)).perform(setText("ш"));
			assertPopup();

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

		//test field validation for multibyte character
		onView(withId(R.id.edit_name_on_card)).perform(setText("ш"));
		assertPopup();
		onView(withId(R.id.edit_email_address)).perform(setText("ш"));
		assertPopup();

		CardInfoScreen.typeTextNameOnCardEditText("Mobiata Auto");
		CardInfoScreen.nameOnCardEditText().perform(click());
		CardInfoScreen.typeTextEmailEditText("mobiataauto@gmail.com");
		CardInfoScreen.clickOnDoneButton();
		try {
			CommonCheckoutScreen.clickIAcceptButton();
		}
		catch (Exception e) {
			//No I accept
		}
		FlightsCheckoutScreen.slideToCheckout();
		CVVEntryScreen.parseAndEnterCVV("111");
		CVVEntryScreen.clickBookButton();
		FlightsConfirmationScreen.clickDoneButton();
		Common.pressBack();
		ScreenActions.delay(1);
	}

	public void bookHotel() throws Throwable {
		LaunchScreen.launchHotels();
		HotelsSearchScreen.clickSearchEditText();
		HotelsSearchScreen.clickToClearSearchEditText();
		HotelsSearchScreen.enterSearchText("New York, NY");
		HotelsSearchScreen.clickSuggestionAtIndex(getActivity(), 1);
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
		try {
			HotelsCheckoutScreen.clickEnterInfoButton();
		}
		catch (Exception e) {
			//No Enter info manually button
		}
		//test field validation for multibyte character
		onView(withId(R.id.edit_first_name)).perform(setText("ш"));
		assertPopup();
		onView(withId(R.id.edit_last_name)).perform(setText("ш"));
		assertPopup();
		onView(withId(R.id.edit_email_address)).perform(setText("ш"));
		assertPopup();

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

		//test field validation for multibyte character
		onView(withId(R.id.edit_address_postal_code)).perform(setText("ш"));
		assertPopup();
		onView(withId(R.id.edit_name_on_card)).perform(setText("ш"));
		assertPopup();

		CardInfoScreen.typeTextPostalCode("94015");
		CardInfoScreen.typeTextNameOnCardEditText("Mobiata Auto");
		CardInfoScreen.clickOnDoneButton();
		try {
			CommonCheckoutScreen.clickIAcceptButton();
		}
		catch (Exception e) {
			//No I accept
		}
		HotelsCheckoutScreen.slideToCheckout();
		CVVEntryScreen.parseAndEnterCVV("111");
		CVVEntryScreen.clickBookButton();
		HotelsConfirmationScreen.clickDoneButton();
	}

	private void assertPopup() {
		EspressoUtils.assertViewWithTextIsDisplayed(mRes.getString(R.string.please_use_the_roman_alphabet));
		SettingsScreen.clickOkString();
	}

	public void testBookingInfoUSPOS() throws Throwable {
		setPOS(PointOfSaleId.UNITED_STATES);
		setLocale(new Locale("en", "US"));
		bookFlight();
		bookHotel();
	}

	public void testBookingInfoUKPOS() throws Throwable {
		setPOS(PointOfSaleId.UNITED_KINGDOM);
		setLocale(new Locale("en", "UK"));
		bookFlight();
		bookHotel();
	}

	public void testBookingInfoBrazilPOS() throws Throwable {
		setPOS(PointOfSaleId.BRAZIL);
		setLocale(new Locale("pt", "BR"));
		bookFlight();
		bookHotel();
	}

	public void testBookingInfoAustraliaPOS() throws Throwable {
		setPOS(PointOfSaleId.AUSTRALIA);
		setLocale(new Locale("en", "AU"));
		bookFlight();
		bookHotel();
	}

	public void testBookingInfoCanadaPOS() throws Throwable {
		setPOS(PointOfSaleId.CANADA);
		setLocale(new Locale("fr", "CA"));
		bookFlight();
		bookHotel();
	}

	public void testBookingInfoJapanPOS() throws Throwable {
		setPOS(PointOfSaleId.JAPAN);
		setLocale(new Locale("ja", "JP"));
		bookFlight();
		bookHotel();
	}
}
