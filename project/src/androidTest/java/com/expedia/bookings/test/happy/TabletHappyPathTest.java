package com.expedia.bookings.test.happy;

import org.joda.time.LocalDate;

import com.expedia.bookings.test.ui.tablet.pagemodels.Checkout;
import com.expedia.bookings.test.ui.tablet.pagemodels.Common;
import com.expedia.bookings.test.ui.tablet.pagemodels.HotelDetails;
import com.expedia.bookings.test.ui.tablet.pagemodels.Launch;
import com.expedia.bookings.test.ui.tablet.pagemodels.Results;
import com.expedia.bookings.test.ui.tablet.pagemodels.Search;
import com.expedia.bookings.test.espresso.TabletTestCase;

public class TabletHappyPathTest extends TabletTestCase {

	public void testBookHotel() throws Throwable {
		screenshot("Launch");
		Launch.clickSearchButton();
		Launch.clickDestinationEditText();
		Launch.typeInDestinationEditText("Detroit, MI");
		Launch.clickSuggestion("Detroit, MI");
		screenshot("Search_Results");

		Results.swipeUpHotelList();
		Results.clickHotelWithName("happypath");
		screenshot("Details");
		HotelDetails.clickAddHotel();
		screenshot("Trip_Bucket");
		Results.clickBookHotel();

		Checkout.clickOnEmptyTravelerDetails();
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

		Search.clickOriginButton();
		Search.typeInOriginEditText("San Francisco, CA");
		Search.clickSuggestion("San Francisco, CA");
		Search.clickSelectFlightDates();
		LocalDate startDate = LocalDate.now().plusDays(35);
		Search.clickDate(startDate, null);
		screenshot("Search");
		Search.clickSearchPopupDone();
		screenshot("Search_Results");
		Results.swipeUpFlightList();
		screenshot("Flight_Search_Results");
		Results.clickFlightAtIndex(1);
		screenshot("Details");
		Results.clickAddFlight();
		screenshot("Trip_Bucket");
		Results.clickBookFlight();

		Checkout.clickOnEmptyTravelerDetails();
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

		// Add Flight to trip bucket
		Results.swipeUpFlightList();
		screenshot("Flight_Search_Results");
		Results.clickFlightAtIndex(1);
		screenshot("Flight_Details_Departure");
		Results.clickAddFlight();
		Results.clickFlightAtIndex(1);
		screenshot("Flight_Details_Return");
		Results.clickAddFlight();

		screenshot("Trip_Bucket");

		// Book Hotel
		Results.clickBookHotel();

		screenshot("Start_Hotel_Checkout");

		Checkout.clickOnEmptyTravelerDetails();
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
