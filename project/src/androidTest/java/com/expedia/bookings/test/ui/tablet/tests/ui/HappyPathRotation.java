package com.expedia.bookings.test.ui.tablet.tests.ui;

import org.joda.time.LocalDate;

import com.expedia.bookings.test.ui.tablet.pagemodels.Checkout;
import com.expedia.bookings.test.ui.tablet.pagemodels.Common;
import com.expedia.bookings.test.ui.tablet.pagemodels.HotelDetails;
import com.expedia.bookings.test.ui.tablet.pagemodels.Launch;
import com.expedia.bookings.test.ui.tablet.pagemodels.Results;
import com.expedia.bookings.test.ui.tablet.pagemodels.Search;
import com.expedia.bookings.test.espresso.TabletTestCase;

/**
 * Created by dmadan on 8/13/14.
 */
public class HappyPathRotation extends TabletTestCase {

	public void testBookHotel() throws Throwable {
		screenshot("Launch");
		rotateScreenTwice();
		Launch.clickSearchButton();
		rotateScreenTwice();
		Launch.clickDestinationEditText();
		Launch.typeInDestinationEditText("Detroit, MI");
		Launch.clickSuggestion("Detroit, MI");
		screenshot("Search_Results");
		rotateScreenTwice();
		Results.swipeUpHotelList();
		rotateScreenTwice();
		Results.clickHotelWithName("happypath");
		screenshot("Details");
		rotateScreenTwice();
		HotelDetails.clickAddHotel();
		screenshot("Trip_Bucket");
		rotateScreenTwice();
		Results.clickBookHotel();
		rotateScreenTwice();

		// Grand total summary
		Checkout.clickGrandTotalTextView();
		screenshot("Cost_Summary");
		rotateScreenTwice();
		Common.pressBack();

		Checkout.clickOnEmptyTravelerDetails();
		Checkout.enterFirstName("Mobiata");
		Checkout.enterLastName("Auto");
		Checkout.enterPhoneNumber("1112223333");
		Checkout.enterEmailAddress("aaa@aaa.com");
		Common.closeSoftKeyboard(Checkout.firstName());
		screenshot("Checkout_Traveler_Entered");
		rotateScreenTwice();
		Checkout.clickOnDone();

		Checkout.clickOnEnterPaymentInformation();
		Checkout.enterCreditCardNumber("4111111111111111");
		Common.closeSoftKeyboard(Checkout.creditCardNumber());
		Checkout.setExpirationDate(2020, 12);
		Checkout.enterNameOnCard("Mobiata Auto");
		Checkout.enterPostalCode("95104");
		Common.closeSoftKeyboard(Checkout.postalCode());
		screenshot("Checkout_Payment_Entered");
		rotateScreenTwice();
		Checkout.clickOnDone();

		screenshot("Slide_To_Purchase");
		rotateScreenTwice();
		Checkout.slideToPurchase();
		Checkout.enterCvv("111");
		screenshot("CVV");

		Checkout.clickBookButton();
		screenshot("Confirmation");
		rotateScreenTwice();
		Checkout.clickDoneBooking();
	}

	public void testBookFlight() throws Throwable {
		screenshot("Launch");
		rotateScreenTwice();
		Launch.clickSearchButton();
		rotateScreenTwice();
		Launch.clickDestinationEditText();
		Launch.typeInDestinationEditText("Detroit, MI");
		Launch.clickSuggestion("Detroit, MI");

		rotateScreenTwice();
		Search.clickOriginButton();
		Search.typeInOriginEditText("San Francisco, CA");
		Search.clickSuggestion("San Francisco, CA");
		rotateScreenTwice();
		Search.clickSelectFlightDates();
		LocalDate startDate = LocalDate.now().plusDays(35);
		Search.clickDate(startDate, null);
		screenshot("Search");
		rotateScreenTwice();
		Search.clickSearchPopupDone();
		screenshot("Search_Results");
		rotateScreenTwice();

		//select travelers
		Search.clickTravelerButton();
		Search.incrementChildButton();
		Search.incrementChildButton();
		rotateScreenTwice();
		Search.clickChild1Spinner();
		Search.selectChildTravelerAgeAt(0, getActivity());
		Search.clickChild2Spinner();
		Search.selectChildTravelerAgeAt(0, getActivity());
		screenshot("Lap_Infant_Alert");
		rotateScreenTwice();
		Search.decrementChildButton();
		Search.decrementChildButton();
		Search.clickSearchPopupDone();

		Results.swipeUpFlightList();
		screenshot("Flight_Search_Results");
		rotateScreenTwice();
		Results.clickFlightAtIndex(1);
		screenshot("Details");
		rotateScreenTwice();
		Results.clickAddFlight();
		screenshot("Trip_Bucket");
		rotateScreenTwice();
		Results.clickBookFlight();

		rotateScreenTwice();
		Checkout.clickOnEmptyTravelerDetails();
		Checkout.enterFirstName("Mobiata");
		Checkout.enterLastName("Auto");
		Checkout.enterPhoneNumber("1112223333");
		Checkout.enterEmailAddress("aaa@aaa.com");
		Common.closeSoftKeyboard(Checkout.emailAddress());
		Checkout.enterDateOfBirth(1970, 1, 1);
		screenshot("Checkout_Traveler_Entered");
		rotateScreenTwice();
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
		screenshot("Checkout_Payment_Entered");
		rotateScreenTwice();
		Checkout.clickOnDone();

		screenshot("Slide_To_Purchase");
		rotateScreenTwice();
		Checkout.slideToPurchase();
		Checkout.enterCvv("111");
		screenshot("CVV");

		Checkout.clickBookButton();
		screenshot("Confirmation");
		rotateScreenTwice();
		Checkout.clickDoneBooking();
	}

	public void testBookHotelAndFlight() throws Throwable {
		screenshot("Launch");
		Launch.clickSearchButton();
		Launch.clickDestinationEditText();
		Launch.typeInDestinationEditText("Detroit, MI");
		Launch.clickSuggestion("Detroit, MI");

		Search.clickOriginButton();
		Search.typeInOriginEditText("San Francisco, CA");
		Search.clickSuggestion("San Francisco, CA");
		Search.clickSelectFlightDates();
		LocalDate startDate = LocalDate.now().plusDays(35);
		LocalDate endDate = LocalDate.now().plusDays(40);
		Search.clickDate(startDate, endDate);
		screenshot("Search");
		Search.clickSearchPopupDone();
		screenshot("Search_Results");

		// Add Hotel to trip bucket
		Results.swipeUpHotelList();
		screenshot("Hotel_Search_Results");
		Results.clickHotelWithName("happypath");
		screenshot("Hotel_Details");
		HotelDetails.clickAddHotel();
		rotateScreenTwice();

		// Add Flight to trip bucket
		Results.swipeUpFlightList();
		screenshot("Flight_Search_Results");
		Results.clickFlightAtIndex(1);
		screenshot("Flight_Details_Departure");
		Results.clickAddFlight();
		Results.clickFlightAtIndex(1);
		screenshot("Flight_Details_Return");
		Results.clickAddFlight();
		rotateScreenTwice();

		screenshot("Trip_Bucket");

		// Book Hotel
		Results.clickBookHotel();

		screenshot("Start_Hotel_Checkout");
		rotateScreenTwice();

		Checkout.clickOnEmptyTravelerDetails();
		Checkout.enterFirstName("Mobiata");
		Checkout.enterLastName("Auto");
		Checkout.enterPhoneNumber("1112223333");
		Checkout.enterEmailAddress("aaa@aaa.com");
		Common.closeSoftKeyboard(Checkout.firstName());
		screenshot("Checkout_Traveler_Entered");
		rotateScreenTwice();
		Checkout.clickOnDone();

		Checkout.clickOnEnterPaymentInformation();
		Checkout.enterCreditCardNumber("4111111111111111");
		Common.closeSoftKeyboard(Checkout.creditCardNumber());
		Checkout.setExpirationDate(2020, 12);
		Checkout.enterNameOnCard("Mobiata Auto");
		Checkout.enterPostalCode("95104");
		Common.closeSoftKeyboard(Checkout.postalCode());
		screenshot("Checkout_Payment_Entered");
		Checkout.clickOnDone();

		screenshot("Slide_To_Purchase");
		rotateScreenTwice();
		Checkout.slideToPurchase();
		Checkout.enterCvv("111");
		screenshot("CVV");

		Checkout.clickBookButton();
		screenshot("Confirmation");
		rotateScreenTwice();
		Checkout.clickBookNextItem();

		// Book Flight
		screenshot("Start_Flight_Checkout");
		rotateScreenTwice();

		Checkout.clickOnTravelerDetails();
		Checkout.enterDateOfBirth(1970, 1, 1);
		screenshot("Checkout_Traveler_Entered");
		rotateScreenTwice();
		Checkout.clickOnDone();

		Checkout.clickOnEnterPaymentInformation();
		// Credit card number should come from hotel entry
		Common.closeSoftKeyboard(Checkout.creditCardNumber());
		Checkout.setExpirationDate(2020, 12);
		// Name on card should come from hotel entry
		Checkout.enterAddress1("123 Main St.");
		Checkout.enterAddress2("Apt. 1");
		Checkout.enterCity("Madison");
		Checkout.enterState("WI");
		Common.closeSoftKeyboard(Checkout.creditCardNumber());
		// Postal code should come from hotel entry
		screenshot("Checkout_Payment_Entered");
		rotateScreenTwice();
		Checkout.clickOnDone();

		screenshot("Slide_To_Purchase");
		rotateScreenTwice();
		Checkout.slideToPurchase();
		Checkout.enterCvv("111");
		screenshot("CVV");

		Checkout.clickBookButton();
		screenshot("Confirmation");
		rotateScreenTwice();

		Checkout.clickDoneBooking();
	}
}
