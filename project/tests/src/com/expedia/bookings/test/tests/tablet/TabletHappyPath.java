package com.expedia.bookings.test.tests.tablet;

import org.joda.time.LocalDate;

import android.test.ActivityInstrumentationTestCase2;

import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.activity.SearchActivity;
import com.expedia.bookings.test.espresso.IdlingResources;
import com.expedia.bookings.test.espresso.IdlingResources.SuggestionResource;
import com.expedia.bookings.test.tests.pageModels.tablet.Checkout;
import com.expedia.bookings.test.tests.pageModels.tablet.Common;
import com.expedia.bookings.test.tests.pageModels.tablet.Launch;
import com.expedia.bookings.test.tests.pageModels.tablet.Results;
import com.expedia.bookings.test.tests.pageModels.tablet.Settings;

public class TabletHappyPath extends ActivityInstrumentationTestCase2 {

	public TabletHappyPath() {
		super(SearchActivity.class);
	}

	private SuggestionResource mSuggestionResource;

	@Override
	public void runTest() throws Throwable {
		// These tests are only applicable to tablets
		if (ExpediaBookingApp.useTabletInterface(getInstrumentation().getTargetContext())) {
			mSuggestionResource = new SuggestionResource();
			IdlingResources.registerSuggestionResource(mSuggestionResource);

			Settings.clearPrivateData(getInstrumentation());
			// Point to the mock server
			Settings.setCustomServer(getInstrumentation(), "mocke3.mobiata.com");

			// Espresso will not launch our activity for us, we must launch it via getActivity().
			getActivity();

			super.runTest();
		}
	}

	public void testBookHotel() throws InterruptedException {
		Launch.clickSearchButton();
		Launch.clickDestinationEditText();
		Launch.typeInDestinationEditText("Detroit, MI");
		Launch.clickSuggestion("Detroit, MI");

		Results.swipeUpHotelList();
		Results.clickHotelWithName("happy_path");
		Results.clickAddHotel();
		Results.clickBookHotel();

		Checkout.clickOnTravelerDetails();
		Checkout.enterFirstName("Mobiata");
		Checkout.enterLastName("Auto");
		Checkout.enterPhoneNumber("1112223333");
		Checkout.enterEmailAddress("aaa@aaa.com");
		Checkout.clickOnDone();

		Checkout.clickOnEnterPaymentInformation();
		Checkout.enterCreditCardNumber("4111111111111111");
		Common.closeSoftKeyboard(Checkout.creditCardNumber());
		Checkout.setExpirationDate(2020, 12);
		Checkout.enterNameOnCard("Mobiata Auto");
		Checkout.enterPostalCode("95104");
		Checkout.clickOnDone();

		Checkout.slideToPurchase();
		Checkout.enterCvv("111");
		Checkout.clickBookButton();

		Checkout.clickDoneBooking();
	}

	public void testBookFlight() throws InterruptedException {
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
		Results.clickSearchNow();
		Results.swipeUpFlightList();
		Results.clickFlightAtIndex(1);
		Results.clickAddFlight();
		Results.clickBookFlight();

		Checkout.clickOnTravelerDetails();
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
		Checkout.clickOnDone();

		Checkout.slideToPurchase();
		Checkout.enterCvv("111");
		Checkout.clickBookButton();

		Checkout.clickDoneBooking();
	}

	public void testBookHotelAndFlight() throws InterruptedException {
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
		Results.clickSearchNow();

		// Add Hotel to trip bucket
		Results.swipeUpHotelList();
		Results.clickHotelWithName("happy_path");
		Results.clickAddHotel();

		// Add Flight to trip bucket
		Results.swipeUpFlightList();
		Results.clickFlightAtIndex(1);
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
		Checkout.clickOnDone();

		Checkout.clickOnEnterPaymentInformation();
		Checkout.enterCreditCardNumber("4111111111111111");
		Common.closeSoftKeyboard(Checkout.creditCardNumber());
		Checkout.setExpirationDate(2020, 12);
		Checkout.enterNameOnCard("Mobiata Auto");
		Checkout.enterPostalCode("95104");
		Checkout.clickOnDone();

		Checkout.slideToPurchase();
		Checkout.enterCvv("111");
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
		// Postal code should come from hotel entry
		Checkout.clickOnDone();

		Checkout.slideToPurchase();
		Checkout.enterCvv("111");
		Checkout.clickBookButton();

		Checkout.clickDoneBooking();
	}

	@Override
	public void tearDown() throws Exception {
		super.tearDown();
		// These tests are only applicable to tablets
		if (ExpediaBookingApp.useTabletInterface(getInstrumentation().getTargetContext())) {
			if (mSuggestionResource != null) {
				IdlingResources.unregisterSuggestionResource(mSuggestionResource);
			}
			Common.pressBackOutOfApp();
		}
	}
}
