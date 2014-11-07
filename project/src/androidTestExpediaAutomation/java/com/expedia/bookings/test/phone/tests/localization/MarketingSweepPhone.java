package com.expedia.bookings.test.phone.tests.localization;

import java.util.Locale;

import org.joda.time.LocalDate;

import com.expedia.bookings.data.pos.PointOfSaleId;
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
import com.expedia.bookings.test.utils.PhoneTestCase;

/**
 * Created by dmadan on 6/30/14.
 */
public class MarketingSweepPhone extends PhoneTestCase {

	public void bookFlight() throws Throwable {
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
		FlightsCheckoutScreen.clickCheckoutButton();

		CommonCheckoutScreen.clickLogInButton();
		screenshot("Log_in_screen");
		Common.closeSoftKeyboard(LogInScreen.logInButton());
		Common.pressBack();
		ScreenActions.delay(1);

		FlightsCheckoutScreen.clickTravelerDetails();
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
		FlightsCheckoutScreen.clickCheckoutButton();
		FlightsCheckoutScreen.clickSelectPaymentButton();
		try {
			FlightsCheckoutScreen.clickNewPaymentCard();
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
		FlightsCheckoutScreen.slideToCheckout();
		CVVEntryScreen.parseAndEnterCVV("111");
		screenshot("CVV_Entry");
		CVVEntryScreen.clickBookButton();

		screenshot("Confirmation");
		FlightsConfirmationScreen.clickDoneButton();
	}

	public void bookHotel() throws Throwable {
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
		HotelsDetailsScreen.clickReviewsTitle();
		screenshot("Hotels_Reviews");
		Common.pressBack();
		HotelsDetailsScreen.clickSelectButton();
		screenshot("Hotel_rooms_rates");
		HotelsRoomsRatesScreen.selectRoomItem(0);
		screenshot("Hotel_checkout");
		try {
			SettingsScreen.clickOKString();
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


	public void testARGENTINA() throws Throwable {
		setPOS(PointOfSaleId.ARGENTINA);
		setLocale(new Locale("es", "AR"));
		bookHotel();
		bookFlight();
	}

	public void testAUSTRALIA() throws Throwable {
		setPOS(PointOfSaleId.AUSTRALIA);
		setLocale(new Locale("en", "AU"));
		bookHotel();
		bookFlight();
	}

	public void testAUSTRIA() throws Throwable {
		setPOS(PointOfSaleId.AUSTRIA);
		setLocale(new Locale("de", "AT"));
		bookHotel();
		bookFlight();
	}

	public void testBELGIUM() throws Throwable {
		setPOS(PointOfSaleId.BELGIUM);
		setLocale(new Locale("nl", "BE"));
		bookHotel();
		bookFlight();
	}

	public void testBRAZIL() throws Throwable {
		setPOS(PointOfSaleId.BRAZIL);
		setLocale(new Locale("pt", "BR"));
		bookHotel();
		bookFlight();
	}

	public void testCANADA() throws Throwable {
		setPOS(PointOfSaleId.CANADA);
		setLocale(new Locale("en", "CA"));
		bookHotel();
		bookFlight();
	}

	public void testDENMARK() throws Throwable {
		setPOS(PointOfSaleId.DENMARK);
		setLocale(new Locale("da", "DK"));
		bookHotel();
		bookFlight();
	}

	public void testFRANCE() throws Throwable {
		setPOS(PointOfSaleId.FRANCE);
		setLocale(new Locale("fr", "FR"));
		bookHotel();
		bookFlight();
	}

	public void testGERMANY() throws Throwable {
		setPOS(PointOfSaleId.GERMANY);
		setLocale(new Locale("de", "DE"));
		bookHotel();
		bookFlight();
	}

	public void testHONG_KONG() throws Throwable {
		setPOS(PointOfSaleId.HONG_KONG);
		setLocale(new Locale("en", "HK"));
		bookHotel();
		bookFlight();
	}

	public void testINDIA() throws Throwable {
		setPOS(PointOfSaleId.INDIA);
		setLocale(new Locale("en", "IN"));
		bookHotel();
		bookFlight();
	}

	public void testINDONESIA() throws Throwable {
		setPOS(PointOfSaleId.INDONESIA);
		setLocale(new Locale("id", "ID"));
		bookHotel();
		bookFlight();
	}

	public void testIRELAND() throws Throwable {
		setPOS(PointOfSaleId.IRELAND);
		setLocale(new Locale("en", "IE"));
		bookHotel();
		bookFlight();
	}

	public void testITALY() throws Throwable {
		setPOS(PointOfSaleId.ITALY);
		setLocale(new Locale("it", "IT"));
		bookHotel();
		bookFlight();
	}


	public void testJAPAN() throws Throwable {
		setPOS(PointOfSaleId.JAPAN);
		setLocale(new Locale("ja", "jp"));
		bookHotel();
		bookFlight();
	}

	public void testSOUTH_KOREA() throws Throwable {
		setPOS(PointOfSaleId.SOUTH_KOREA);
		setLocale(new Locale("ko", "KR"));
		bookHotel();
		bookFlight();
	}

	public void testMALAYSIA() throws Throwable {
		setPOS(PointOfSaleId.MALAYSIA);
		setLocale(new Locale("ms", "MY"));
		bookHotel();
		bookFlight();
	}

	public void testMEXICO() throws Throwable {
		setPOS(PointOfSaleId.MEXICO);
		setLocale(new Locale("en", "US"));
		bookHotel();
		bookFlight();
	}

	public void testNETHERLANDS() throws Throwable {
		setPOS(PointOfSaleId.NETHERLANDS);
		setLocale(new Locale("nl", "NL"));
		bookHotel();
		bookFlight();
	}

	public void testNORWAY() throws Throwable {
		setPOS(PointOfSaleId.NORWAY);
		setLocale(new Locale("nb", "NO"));
		bookHotel();
		bookFlight();
	}

	public void testPHILIPPINES() throws Throwable {
		setPOS(PointOfSaleId.PHILIPPINES);
		setLocale(new Locale("en", "PH"));
		bookHotel();
		bookFlight();
	}

	public void testSINGAPORE() throws Throwable {
		setPOS(PointOfSaleId.SINGAPORE);
		setLocale(new Locale("en", "SG"));
		bookHotel();
		bookFlight();
	}

	public void testSPAIN() throws Throwable {
		setPOS(PointOfSaleId.SPAIN);
		setLocale(new Locale("es", "ES"));
		bookHotel();
		bookFlight();
	}

	public void testSWEDEN() throws Throwable {
		setPOS(PointOfSaleId.SWEDEN);
		setLocale(new Locale("sv", "SE"));
		bookHotel();
		bookFlight();
	}

	public void testTAIWAN() throws Throwable {
		setPOS(PointOfSaleId.TAIWAN);
		setLocale(new Locale("zh", "TW"));
		bookHotel();
		bookFlight();
	}

	public void testTHAILAND() throws Throwable {
		setPOS(PointOfSaleId.THAILAND);
		setLocale(new Locale("th", "TH"));
		bookHotel();
		bookFlight();
	}

	public void testUNITED_KINGDOM() throws Throwable {
		setPOS(PointOfSaleId.UNITED_KINGDOM);
		setLocale(new Locale("en", "UK"));
		bookHotel();
		bookFlight();
	}

	public void testUNITED_STATES() throws Throwable {
		setPOS(PointOfSaleId.UNITED_STATES);
		setLocale(new Locale("en", "US"));
		bookHotel();
		bookFlight();
	}

	public void testVIETNAM() throws Throwable {
		setPOS(PointOfSaleId.VIETNAM);
		setLocale(new Locale("vi", "VN"));
		bookHotel();
		bookFlight();
	}
}
