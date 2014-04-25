package com.expedia.bookings.test.tests.tablet;

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

		Checkout.clickOnAddTraveler();
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

	@Override
	public void tearDown() throws Exception {
		super.tearDown();
		// These tests are only applicable to tablets
		if (ExpediaBookingApp.useTabletInterface(getInstrumentation().getTargetContext())) {
			if (mSuggestionResource != null) {
				IdlingResources.unregisterSuggestionResource(mSuggestionResource);
			}
		}
	}
}
