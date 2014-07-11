package com.expedia.bookings.test.happy;

import org.joda.time.LocalDate;

import com.expedia.bookings.test.tests.pageModels.tablet.Checkout;
import com.expedia.bookings.test.tests.pageModels.tablet.Common;
import com.expedia.bookings.test.tests.pageModels.tablet.Launch;
import com.expedia.bookings.test.tests.pageModels.tablet.Results;
import com.expedia.bookings.test.utils.SpoonScreenshotUtils;
import com.expedia.bookings.test.utils.TabletTestCase;

public class TabletHappyPath extends TabletTestCase {

	public void testBookHotel() throws Throwable {
		SpoonScreenshotUtils.screenshot("Launch", getInstrumentation());
		Launch.clickSearchButton();
		Launch.clickDestinationEditText();
		Launch.typeInDestinationEditText("Detroit, MI");
		Launch.clickSuggestion("Detroit, MI");
		SpoonScreenshotUtils.screenshot("Search_Results", getInstrumentation());

		Results.swipeUpHotelList();
		Results.clickHotelWithName("happy_path");
		SpoonScreenshotUtils.screenshot("Details", getInstrumentation());
		Results.clickAddHotel();
		Results.clickBookHotel();

		Checkout.clickOnTravelerDetails();
		Checkout.enterFirstName("Mobiata");
		Checkout.enterLastName("Auto");
		Checkout.enterPhoneNumber("1112223333");
		Checkout.enterEmailAddress("aaa@aaa.com");
		Common.closeSoftKeyboard(Checkout.firstName());
		SpoonScreenshotUtils.screenshot("Checkout_Traveler_Entered", getInstrumentation());
		Checkout.clickOnDone();

		Checkout.clickOnEnterPaymentInformation();
		Checkout.enterCreditCardNumber("4111111111111111");
		Common.closeSoftKeyboard(Checkout.creditCardNumber());
		Checkout.setExpirationDate(2020, 12);
		Checkout.enterNameOnCard("Mobiata Auto");
		Checkout.enterPostalCode("95104");
		Common.closeSoftKeyboard(Checkout.postalCode());
		SpoonScreenshotUtils.screenshot("Checkout_Payment_Entered", getInstrumentation());
		Checkout.clickOnDone();

		SpoonScreenshotUtils.screenshot("Slide_To_Purchase", getInstrumentation());
		Checkout.slideToPurchase();
		Checkout.enterCvv("111");
		SpoonScreenshotUtils.screenshot("CVV", getInstrumentation());
		Checkout.clickBookButton();

		SpoonScreenshotUtils.screenshot("Confirmation", getInstrumentation());
		Checkout.clickDoneBooking();
	}

	public void testBookFlight() throws Throwable {
		SpoonScreenshotUtils.screenshot("Launch", getInstrumentation());
		Launch.clickSearchButton();
		Launch.clickDestinationEditText();
		Launch.typeInDestinationEditText("Detroit, MI");
		Launch.clickSuggestion("Detroit, MI");

		Results.clickOriginButton();
		Results.typeInOriginEditText("San Francisco, CA");
		Results.clickSuggestion("San Francisco, CA");
		Results.clickSelectFlightDates();
		LocalDate startDate = LocalDate.now().plusDays(35);
		Results.clickDate(startDate, null);
		SpoonScreenshotUtils.screenshot("Search", getInstrumentation());
		Results.clickSearchNow();
		Results.swipeUpFlightList();
		SpoonScreenshotUtils.screenshot("Search_Results", getInstrumentation());
		Results.clickFlightAtIndex(1);
		SpoonScreenshotUtils.screenshot("Details", getInstrumentation());
		Results.clickAddFlight();
		Results.clickBookFlight();

		Checkout.clickOnTravelerDetails();
		Checkout.enterFirstName("Mobiata");
		Checkout.enterLastName("Auto");
		Checkout.enterPhoneNumber("1112223333");
		Checkout.enterEmailAddress("aaa@aaa.com");
		Common.closeSoftKeyboard(Checkout.emailAddress());
		Checkout.enterDateOfBirth(1970, 1, 1);
		SpoonScreenshotUtils.screenshot("Checkout_Traveler_Entered", getInstrumentation());
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
		SpoonScreenshotUtils.screenshot("Checkout_Payment_Entered", getInstrumentation());
		Checkout.clickOnDone();

		SpoonScreenshotUtils.screenshot("Slide_To_Purchase", getInstrumentation());
		Checkout.slideToPurchase();
		Checkout.enterCvv("111");
		SpoonScreenshotUtils.screenshot("CVV", getInstrumentation());
		Checkout.clickBookButton();

		SpoonScreenshotUtils.screenshot("Confirmation", getInstrumentation());
		Checkout.clickDoneBooking();
	}

	public void testBookHotelAndFlight() throws Throwable {
		SpoonScreenshotUtils.screenshot("Launch", getInstrumentation());
		Launch.clickSearchButton();
		Launch.clickDestinationEditText();
		Launch.typeInDestinationEditText("Detroit, MI");
		Launch.clickSuggestion("Detroit, MI");

		Results.clickOriginButton();
		Results.typeInOriginEditText("San Francisco, CA");
		Results.clickSuggestion("San Francisco, CA");
		Results.clickSelectFlightDates();
		LocalDate startDate = LocalDate.now().plusDays(35);
		LocalDate endDate = LocalDate.now().plusDays(40);
		Results.clickDate(startDate, endDate);
		SpoonScreenshotUtils.screenshot("Search", getInstrumentation());
		Results.clickSearchNow();

		// Add Hotel to trip bucket
		Results.swipeUpHotelList();
		SpoonScreenshotUtils.screenshot("Hotel_Search_Results", getInstrumentation());
		Results.clickHotelWithName("happy_path");
		SpoonScreenshotUtils.screenshot("Hotel_Details", getInstrumentation());
		Results.clickAddHotel();

		// Add Flight to trip bucket
		Results.swipeUpFlightList();
		SpoonScreenshotUtils.screenshot("Flight_Search_Results", getInstrumentation());
		Results.clickFlightAtIndex(1);
		SpoonScreenshotUtils.screenshot("Flight_Details", getInstrumentation());
		Results.clickAddFlight();
		Results.clickFlightAtIndex(1);
		Results.clickAddFlight();

		// Book Hotel
		Results.clickBookHotel();

		Checkout.clickOnTravelerDetails();
		Checkout.enterFirstName("Mobiata");
		Checkout.enterLastName("Auto");
		Checkout.enterPhoneNumber("1112223333");
		Checkout.enterEmailAddress("aaa@aaa.com");
		Common.closeSoftKeyboard(Checkout.firstName());
		SpoonScreenshotUtils.screenshot("Checkout_Traveler_Entered", getInstrumentation());
		Checkout.clickOnDone();

		Checkout.clickOnEnterPaymentInformation();
		Checkout.enterCreditCardNumber("4111111111111111");
		Common.closeSoftKeyboard(Checkout.creditCardNumber());
		Checkout.setExpirationDate(2020, 12);
		Checkout.enterNameOnCard("Mobiata Auto");
		Checkout.enterPostalCode("95104");
		Common.closeSoftKeyboard(Checkout.postalCode());
		SpoonScreenshotUtils.screenshot("Checkout_Payment_Entered", getInstrumentation());
		Checkout.clickOnDone();

		SpoonScreenshotUtils.screenshot("Slide_To_Purchase", getInstrumentation());
		Checkout.slideToPurchase();
		Checkout.enterCvv("111");
		SpoonScreenshotUtils.screenshot("CVV", getInstrumentation());
		Checkout.clickBookButton();

		Checkout.clickBookNextItem();

		// Book Flight
		Checkout.clickOnTravelerDetails();
		Checkout.enterFirstName("Mobiata");
		Checkout.enterLastName("Auto");
		Checkout.enterPhoneNumber("1112223333");
		Checkout.enterEmailAddress("aaa@aaa.com");
		Common.closeSoftKeyboard(Checkout.emailAddress());
		Checkout.enterDateOfBirth(1970, 1, 1);
		SpoonScreenshotUtils.screenshot("Checkout_Traveler_Entered", getInstrumentation());
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
		SpoonScreenshotUtils.screenshot("Checkout_Payment_Entered", getInstrumentation());
		// Postal code should come from hotel entry
		Checkout.clickOnDone();

		SpoonScreenshotUtils.screenshot("Slide_To_Purchase", getInstrumentation());
		Checkout.slideToPurchase();
		Checkout.enterCvv("111");
		SpoonScreenshotUtils.screenshot("CVV", getInstrumentation());
		Checkout.clickBookButton();

		SpoonScreenshotUtils.screenshot("Confirmation", getInstrumentation());
		Checkout.clickDoneBooking();
	}
}
