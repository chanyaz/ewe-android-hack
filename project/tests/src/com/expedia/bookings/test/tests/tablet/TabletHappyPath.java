package com.expedia.bookings.test.tests.tablet;

import org.joda.time.LocalDate;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.test.ActivityInstrumentationTestCase2;

import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.activity.SearchActivity;
import com.expedia.bookings.test.tests.pageModels.tablet.Checkout;
import com.expedia.bookings.test.tests.pageModels.tablet.Common;
import com.expedia.bookings.test.tests.pageModels.tablet.IdlingResources;
import com.expedia.bookings.test.tests.pageModels.tablet.IdlingResources.SuggestionResource;
import com.expedia.bookings.test.tests.pageModels.tablet.Launch;
import com.expedia.bookings.test.tests.pageModels.tablet.Results;
import com.expedia.bookings.test.tests.pageModels.tablet.Settings;
import com.google.android.apps.common.testing.ui.espresso.Espresso;
import com.mobiata.android.Log;

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
		Checkout.enterDateOfBirth(1970, 1, 1);
		Checkout.clickOnDone();

		Checkout.clickOnEnterPaymentInformation();
		Checkout.setExpirationDate(2020, 12);
		Checkout.enterCreditCardNumber("4111111111111111\n");
		Checkout.enterNameOnCard("Mobiata Auto\n");
		Checkout.enterAddress1("123 Main St.\n");
		Checkout.enterAddress2("Apt. 1\n");
		Checkout.enterCity("Madison\n");
		Checkout.enterState("WI\n");
		Checkout.enterPostalCode("53704\n");
		Checkout.clickOnDone();

		Checkout.slideToPurchase();
		Checkout.enterCvv("111");
		Checkout.clickBookButton();

		Checkout.clickDoneBooking();
	}

	public void testBookHotelFlight() throws InterruptedException {
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

		// Add Hotel to trip bucket
		Results.swipeUpHotelList();
		Results.clickHotelWithName("happy_path");
		Results.clickAddHotel();

		// Add Flight to trip bucket
		Results.swipeUpFlightList();
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
		Checkout.enterDateOfBirth(1970, 1, 1);
		Checkout.clickOnDone();

		Checkout.clickOnEnterPaymentInformation();
		Checkout.setExpirationDate(2020, 12);
		Checkout.enterCreditCardNumber("4111111111111111\n");
		Checkout.enterNameOnCard("Mobiata Auto\n");
		Checkout.enterAddress1("123 Main St.\n");
		Checkout.enterAddress2("Apt. 1\n");
		Checkout.enterCity("Madison\n");
		Checkout.enterState("WI\n");
		Checkout.enterPostalCode("53704\n");
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
			try {
				while (true) {
					Espresso.pressBack();
				}
			}
			catch (Exception e) {
				// Ignore
				Log.v("Pressed back a bunch of times: ", e);
			}
		}
	}
}
