package com.expedia.bookings.test.tests.tablet;

import org.joda.time.LocalDate;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;

import com.expedia.bookings.activity.SearchActivity;
import com.expedia.bookings.test.tests.pageModels.tablet.Checkout;
import com.expedia.bookings.test.tests.pageModels.tablet.Launch;
import com.expedia.bookings.test.tests.pageModels.tablet.Results;
import com.mobiata.android.Log;

public class TabletHappyPath extends ActivityInstrumentationTestCase2 {

	public TabletHappyPath() {
		super(SearchActivity.class);
		Launch.registerSuggestionResource();
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();

		Intent intent = new Intent();
		intent.putExtra("isAutomation", true);
		setActivityIntent(intent);

		// Espresso will not launch our activity for us, we must launch it via getActivity().
		getActivity();
	}

	public void testBookHotel() throws InterruptedException {
		Launch.clickSearchButton();
		Launch.clickDestinationEditText();
		Launch.typeInDestinationEditText("Detroit, MI");
		Launch.clickSuggestion("Detroit, MI");

		Results.clickHotelWithName("Corktown Inn");
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
		Launch.unregisterSuggestionResource();
	}
}
