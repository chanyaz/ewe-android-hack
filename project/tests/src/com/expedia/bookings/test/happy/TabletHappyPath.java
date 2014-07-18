package com.expedia.bookings.test.happy;

import org.joda.time.LocalDate;

import com.expedia.bookings.test.tests.pageModels.tablet.Checkout;
import com.expedia.bookings.test.tests.pageModels.tablet.Common;
import com.expedia.bookings.test.tests.pageModels.tablet.Launch;
import com.expedia.bookings.test.tests.pageModels.tablet.Results;
import com.expedia.bookings.test.utils.TabletTestCase;

public class TabletHappyPath extends TabletTestCase {

	public void testBookHotel() throws Throwable {
		screenshot("Launch");
		Launch.clickSearchButton();
		Launch.clickDestinationEditText();
		Launch.typeInDestinationEditText("Detroit, MI");
		Launch.clickSuggestion("Detroit, MI");
		screenshot("Search_Results");

		Results.swipeUpHotelList();
		Results.clickHotelWithName("happy_path");
		screenshot("Details");
		Results.clickAddHotel();
		screenshot("Trip_Bucket");
		Results.clickBookHotel();

		Checkout.clickOnTravelerDetails();
		Checkout.enterFirstName("Mobiata");
		Checkout.enterLastName("Auto");
		Checkout.enterPhoneNumber("1112223333");
		Checkout.enterEmailAddress("aaa@aaa.com");
		Common.closeSoftKeyboard(Checkout.firstName());
		screenshot("Checkout_Traveler_Entered");
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
		Checkout.slideToPurchase();
		Checkout.enterCvv("111");
		screenshot("CVV");
		Checkout.clickBookButton();

		screenshot("Confirmation");
		Checkout.clickDoneBooking();
	}

	public void testBookFlight() throws Throwable {
		screenshot("Launch");
		Launch.clickSearchButton();
		Launch.clickDestinationEditText();
		Launch.typeInDestinationEditText("Detroit, MI");
		Launch.clickSuggestion("Detroit, MI");

		Common.pressBack();
		Results.clickOriginButton();
		Results.typeInOriginEditText("San Francisco, CA");
		Results.clickSuggestion("San Francisco, CA");
		Results.clickSelectFlightDates();
		LocalDate startDate = LocalDate.now().plusDays(35);
		Results.clickDate(startDate, null);
		screenshot("Search");
		Results.clickSearchPopupDone();
		Results.swipeUpFlightList();
		screenshot("Search_Results");
		Results.clickFlightAtIndex(1);
		screenshot("Details");
		Results.clickAddFlight();
		screenshot("Trip_Bucket");
		Results.clickBookFlight();

		Checkout.clickOnTravelerDetails();
		Checkout.enterFirstName("Mobiata");
		Checkout.enterLastName("Auto");
		Checkout.enterPhoneNumber("1112223333");
		Checkout.enterEmailAddress("aaa@aaa.com");
		Common.closeSoftKeyboard(Checkout.emailAddress());
		Checkout.enterDateOfBirth(1970, 1, 1);
		screenshot("Checkout_Traveler_Entered");
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
		Checkout.clickOnDone();

		screenshot("Slide_To_Purchase");
		Checkout.slideToPurchase();
		Checkout.enterCvv("111");
		screenshot("CVV");
		Checkout.clickBookButton();

		screenshot("Confirmation");
		Checkout.clickDoneBooking();
	}

	public void testBookHotelAndFlight() throws Throwable {
		screenshot("Launch");
		Launch.clickSearchButton();
		Launch.clickDestinationEditText();
		Launch.typeInDestinationEditText("Detroit, MI");
		Launch.clickSuggestion("Detroit, MI");

		Common.pressBack();
		Results.clickOriginButton();
		Results.typeInOriginEditText("San Francisco, CA");
		Results.clickSuggestion("San Francisco, CA");
		Results.clickSelectFlightDates();
		LocalDate startDate = LocalDate.now().plusDays(35);
		LocalDate endDate = LocalDate.now().plusDays(40);
		Results.clickDate(startDate, endDate);
		screenshot("Search");
		Results.clickSearchPopupDone();

		// Add Hotel to trip bucket
		Results.swipeUpHotelList();
		screenshot("Hotel_Search_Results");
		Results.clickHotelWithName("happy_path");
		screenshot("Hotel_Details");
		Results.clickAddHotel();

		// Add Flight to trip bucket
		Results.swipeUpFlightList();
		screenshot("Flight_Search_Results");
		Results.clickFlightAtIndex(1);
		screenshot("Flight_Details");
		Results.clickAddFlight();
		Results.clickFlightAtIndex(1);
		Results.clickAddFlight();

		screenshot("Trip_Bucket");

		// Book Hotel
		Results.clickBookHotel();

		screenshot("Start_Hotel_Checkout");

		Checkout.clickOnTravelerDetails();
		Checkout.enterFirstName("Mobiata");
		Checkout.enterLastName("Auto");
		Checkout.enterPhoneNumber("1112223333");
		Checkout.enterEmailAddress("aaa@aaa.com");
		Common.closeSoftKeyboard(Checkout.firstName());
		screenshot("Checkout_Traveler_Entered");
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
		Checkout.slideToPurchase();
		Checkout.enterCvv("111");
		screenshot("CVV");

		Checkout.clickBookButton();
		screenshot("Confirmation");

		Checkout.clickBookNextItem();

		// Book Flight
		screenshot("Start_Flight_Checkout");

		Checkout.clickOnTravelerDetails();
		Checkout.enterFirstName("Mobiata");
		Checkout.enterLastName("Auto");
		Checkout.enterPhoneNumber("1112223333");
		Checkout.enterEmailAddress("aaa@aaa.com");
		Common.closeSoftKeyboard(Checkout.emailAddress());
		Checkout.enterDateOfBirth(1970, 1, 1);
		screenshot("Checkout_Traveler_Entered");
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
		Checkout.clickOnDone();

		screenshot("Slide_To_Purchase");
		Checkout.slideToPurchase();
		Checkout.enterCvv("111");
		screenshot("CVV");

		Checkout.clickBookButton();
		screenshot("Confirmation");

		Checkout.clickDoneBooking();
	}
}
