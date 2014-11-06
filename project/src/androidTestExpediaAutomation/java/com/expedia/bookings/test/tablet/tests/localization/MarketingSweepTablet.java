package com.expedia.bookings.test.tablet.tests.localization;

import java.util.Locale;

import org.joda.time.LocalDate;

import android.view.KeyEvent;

import com.expedia.bookings.data.pos.PointOfSaleId;
import com.expedia.bookings.test.tablet.pagemodels.Checkout;
import com.expedia.bookings.test.tablet.pagemodels.Common;
import com.expedia.bookings.test.tablet.pagemodels.HotelDetails;
import com.expedia.bookings.test.tablet.pagemodels.Launch;
import com.expedia.bookings.test.tablet.pagemodels.Results;
import com.expedia.bookings.test.tablet.pagemodels.Search;
import com.expedia.bookings.test.tablet.pagemodels.SortFilter;
import com.expedia.bookings.test.utils.TabletTestCase;

import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.clearText;

/**
 * Created by dmadan on 7/3/14.
 */
public class MarketingSweepTablet extends TabletTestCase {

	public void bookHotel() throws Throwable {
		screenshot("Launch");
		Launch.clickSearchButton();
		Launch.clickDestinationEditText();
		Launch.clickSuggestionAtPosition(0);

		screenshot("Search");
		Results.swipeUpHotelList();
		screenshot("Search_Results");
		SortFilter.clickHotelSortFilterButton();
		screenshot("Hotel_Sort_Filter");
		Results.clickHotelAtIndex(1);
		screenshot("Details");
		HotelDetails.clickAddHotel();
		Results.clickBookButton();

		Checkout.clickGrandTotalTextView();
		screenshot("Cost_Summary");
		Common.pressBack();

		screenshot("Checkout1");
		Checkout.clickOnEmptyTravelerDetails();
		Common.closeSoftKeyboard(Checkout.firstName());
		screenshot("Traveler_Details");
		Checkout.enterFirstName("Mobiata");
		Checkout.enterLastName("Auto");
		Checkout.enterPhoneNumber("1112223333");
		Checkout.enterEmailAddress("aaa@aaa.com");
		Common.closeSoftKeyboard(Checkout.firstName());
		screenshot("Checkout_Traveler_Entered");
		Checkout.clickOnDone();

		screenshot("Checkout2");
		Checkout.clickOnEnterPaymentInformation();
		Common.closeSoftKeyboard(Checkout.creditCardNumber());
		screenshot("Payment_Details");

		//Enter Amex payment details to get the screenshot of CVV screen
		Checkout.enterCreditCardNumber("345555555555555");
		Common.closeSoftKeyboard(Checkout.creditCardNumber());
		Checkout.setExpirationDate(2020, 12);
		Checkout.enterNameOnCard("Mobiata Auto");
		Checkout.enterPostalCode("95104");
		Common.closeSoftKeyboard(Checkout.postalCode());
		Checkout.clickOnDone();
		try {
			Checkout.clickIAcceptButton();
		}
		catch (Exception e) {
			//no I accept button
		}
		Checkout.slideToPurchase();
		screenshot("American_Express_CVV");

		//go back and checkout using VISA
		Common.pressBack();
		Checkout.clickCreditCardSection();
		Checkout.creditCardNumber().perform(clearText());
		Checkout.enterCreditCardNumber("4111111111111111");
		Common.closeSoftKeyboard(Checkout.creditCardNumber());
		screenshot("Checkout_Payment_Entered");
		Checkout.clickOnDone();

		screenshot("Checkout3");
		try {
			Checkout.clickIAcceptButton();
		}
		catch (Exception e) {
			//no I accept button
		}
		Checkout.slideToPurchase();
		Checkout.enterCvv("111");
		screenshot("CVV");
		Checkout.clickBookButton();
		screenshot("Confirmation");
		Checkout.clickDoneBooking();
	}

	public void bookFlight() throws Throwable {
		screenshot("Launch");
		Launch.clickSearchButton();
		Launch.clickDestinationEditText();
		Launch.clickSuggestionAtPosition(0);

		Search.clickOriginButton();
		Search.typeInOriginEditText("London, England, UK");
		getInstrumentation().sendCharacterSync(KeyEvent.KEYCODE_SPACE);
		Search.clickSuggestionAtPosition(0);
		Search.clickSelectFlightDates();
		LocalDate startDate = LocalDate.now().plusDays(45);
		Search.clickDate(startDate, null);
		screenshot("Search");
		Search.clickSearchPopupDone();

		//get screenshot of lap infant alert
		Search.clickTravelerButton();
		Search.incrementChildButton();
		Search.incrementChildButton();
		Search.clickChild1Spinner();
		Search.selectChildTravelerAgeAt(0, getActivity());
		Search.clickChild2Spinner();
		Search.selectChildTravelerAgeAt(0, getActivity());
		screenshot("Lap_Infant_Alert");
		Search.decrementChildButton();
		Search.decrementChildButton();
		Search.clickSearchPopupDone();

		Results.swipeUpFlightList();
		screenshot("Search_Results");
		Results.clickFlightAtIndex(1);
		screenshot("Details");
		Results.clickAddFlight();
		Results.clickBookButton();

		Checkout.clickGrandTotalTextView();
		screenshot("Cost_Summary");
		Common.pressBack();

		screenshot("Checkout1");
		Checkout.clickOnEmptyTravelerDetails();
		Common.closeSoftKeyboard(Checkout.firstName());
		screenshot("Traveler_Details");
		Checkout.enterFirstName("Mobiata");
		Checkout.enterLastName("Auto");
		Checkout.enterPhoneNumber("1112223333");
		Common.closeSoftKeyboard(Checkout.phoneNumber());
		Checkout.enterDateOfBirth(1970, 1, 1);
		Checkout.enterEmailAddress("aaa@aaa.com");
		Common.closeSoftKeyboard(Checkout.emailAddress());
		screenshot("Checkout_Traveler_Entered");
		Checkout.clickOnDone();

		screenshot("Checkout2");
		Checkout.clickOnEnterPaymentInformation();
		Common.closeSoftKeyboard(Checkout.creditCardNumber());
		screenshot("Payment_Details");
		Checkout.enterCreditCardNumber("4111111111111111");
		Common.closeSoftKeyboard(Checkout.creditCardNumber());
		Checkout.setExpirationDate(2020, 12);
		Checkout.enterNameOnCard("Mobiata Auto");
		Checkout.enterAddress1("123 Main St.");
		Checkout.enterAddress2("Apt. 1");
		Checkout.enterCity("Madison");
		Checkout.enterState("WI");
		Checkout.enterPostalCode("53704");
		Common.closeSoftKeyboard(Checkout.firstName());
		screenshot("Checkout_Payment_Entered");
		Checkout.clickOnDone();

		screenshot("Checkout3");
		try {
			Checkout.clickIAcceptButton();
		}
		catch (Exception e) {
			//no I accept button
		}
		Checkout.slideToPurchase();
		Checkout.enterCvv("111");
		screenshot("CVV");
		Checkout.clickBookButton();
		screenshot("Confirmation");
		Checkout.clickDoneBooking();
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
		setLocale(new Locale("es", "MX"));
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
