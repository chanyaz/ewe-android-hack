package com.expedia.bookings.test.ui.tablet.tests.localization;

import java.util.Locale;

import org.joda.time.LocalDate;

import com.expedia.bookings.R;
import com.expedia.bookings.data.pos.PointOfSaleId;
import com.expedia.bookings.test.ui.phone.pagemodels.common.SettingsScreen;
import com.expedia.bookings.test.ui.tablet.pagemodels.Checkout;
import com.expedia.bookings.test.ui.tablet.pagemodels.Common;
import com.expedia.bookings.test.ui.tablet.pagemodels.HotelDetails;
import com.expedia.bookings.test.ui.tablet.pagemodels.Launch;
import com.expedia.bookings.test.ui.tablet.pagemodels.Results;
import com.expedia.bookings.test.ui.tablet.pagemodels.Search;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.TabletTestCase;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static com.expedia.bookings.test.espresso.ViewActions.setText;

/**
 * Created by dmadan on 1/23/15.
 */
public class FieldValidationTest extends TabletTestCase {

	/*
	 * #373 eb_tp test plan
	 */

	public void bookHotel() {
		Launch.clickSearchButton();
		Launch.clickDestinationEditText();
		Launch.typeInDestinationEditText("Detroit, MI");
		Launch.clickSuggestionAtPosition(1);

		Results.swipeUpHotelList();
		Results.clickHotelAtIndex(1);
		HotelDetails.clickAddHotel();
		Results.clickBookButton();

		Checkout.clickOnTravelerDetails();

		Checkout.firstName().perform(clearText());
		Checkout.lastName().perform(clearText());
		Checkout.emailAddress().perform(clearText());

		//test field validation for multibyte character
		onView(withId(R.id.edit_first_name)).perform(setText("ш"));
		assertPopup();
		onView(withId(R.id.edit_last_name)).perform(setText("ш"));
		assertPopup();
		onView(withId(R.id.edit_email_address)).perform(setText("ш"));
		assertPopup();

		Checkout.enterFirstName("Mobiata");
		Checkout.enterLastName("Auto");
		Checkout.enterPhoneNumber("1112223333");
		Checkout.enterEmailAddress("aaa@aaa.com");
		Common.closeSoftKeyboard(Checkout.firstName());
		Checkout.clickOnDone();

		Checkout.clickCreditCardSection();

		Checkout.postalCode().perform(clearText());
		Checkout.nameOnCard().perform(clearText());
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
		Checkout.enterCvv("111");
		Checkout.clickBookButton();

		Checkout.clickDoneBooking();
	}

	public void bookFlight() {
		Launch.clickSearchButton();
		Launch.clickDestinationEditText();
		Launch.typeInDestinationEditText("Detroit, MI");
		Launch.clickSuggestionAtPosition(1);

		Search.clickOriginButton();
		Search.typeInOriginEditText("San Francisco, CA");
		Launch.clickSuggestionAtPosition(1);
		Search.clickSelectFlightDates();
		int randomOffset = 20 + (int) (Math.random() * 100);
		LocalDate startDate = LocalDate.now().plusDays(randomOffset);
		Search.clickDate(startDate, null);
		Search.clickSearchPopupDone();
		Results.swipeUpFlightList();
		Results.clickFlightAtIndex(1);
		Results.clickAddFlight();
		Results.clickBookButton();

		Checkout.clickOnEmptyTravelerDetails();

		//test field validation for multibyte character
		onView(withId(R.id.edit_first_name)).perform(setText("ш"));
		assertPopup();
		onView(withId(R.id.edit_last_name)).perform(setText("ш"));
		assertPopup();

		Checkout.enterFirstName("Mobiata");
		Checkout.enterLastName("Auto");
		Checkout.enterPhoneNumber("1112223333");
		Checkout.enterEmailAddress("aaa@aaa.com");
		Common.closeSoftKeyboard(Checkout.emailAddress());
		Checkout.enterDateOfBirth(1970, 1, 1);
		Checkout.clickOnDone();

		Checkout.clickOnEnterPaymentInformation();
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
		Checkout.clickOnDone();

		try {
			Checkout.clickIAcceptButton();
		}
		catch (Exception e) {
			//no I accept button
		}
		Checkout.slideToPurchase();
		Checkout.enterCvv("111");
		Checkout.clickBookButton();

		Checkout.clickDoneBooking();
		Common.pressBack();
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
