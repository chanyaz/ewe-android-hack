package com.expedia.bookings.test.tablet.sweep;

import org.joda.time.LocalDate;

import android.view.KeyEvent;

import com.expedia.bookings.data.pos.PointOfSaleId;
import com.expedia.bookings.test.tablet.pagemodels.Checkout;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.tablet.pagemodels.HotelDetails;
import com.expedia.bookings.test.tablet.pagemodels.Launch;
import com.expedia.bookings.test.tablet.pagemodels.Results;
import com.expedia.bookings.test.tablet.pagemodels.Search;
import com.expedia.bookings.test.tablet.pagemodels.SortFilter;
import com.expedia.bookings.test.espresso.TabletTestCase;

import static android.support.test.espresso.action.ViewActions.clearText;

/**
 * Created by dmadan on 7/3/14.
 */
public class TabletScreenshotSweep extends TabletTestCase {

	public void testBookHotel() throws Throwable {
		Common.setLocale(getLocale());
		Common.setPOS(PointOfSaleId.valueOf(getPOS(getLocale())));

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

	public void testBookFlight() throws Throwable {
		Common.setLocale(getLocale());
		Common.setPOS(PointOfSaleId.valueOf(getPOS(getLocale())));

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
}
