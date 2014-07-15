package com.expedia.bookings.test.tests.localization;

import org.joda.time.LocalDate;

import android.test.ActivityInstrumentationTestCase2;
import android.view.KeyEvent;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.SearchActivity;
import com.expedia.bookings.test.espresso.IdlingResources;
import com.expedia.bookings.test.tests.pageModels.tablet.Checkout;
import com.expedia.bookings.test.tests.pageModels.tablet.Common;
import com.expedia.bookings.test.tests.pageModels.tablet.Launch;
import com.expedia.bookings.test.tests.pageModels.tablet.Results;
import com.expedia.bookings.test.tests.pageModels.tablet.Settings;
import com.mobiata.android.util.SettingUtils;
import com.expedia.bookings.test.utils.SpoonScreenshotUtils;

/**
 * Created by dmadan on 7/3/14.
 */
public class MarketingSweepTablet extends ActivityInstrumentationTestCase2<SearchActivity> {

	public MarketingSweepTablet() {
		super(SearchActivity.class);
	}

	private IdlingResources.SuggestionResource mSuggestionResource;

	@Override
	public void runTest() throws Throwable {
		if (Common.isTablet(getInstrumentation())) {
			Settings.clearPrivateData(getInstrumentation());
			SettingUtils.save(getInstrumentation().getTargetContext(), R.string.preference_which_api_to_use_key, "Production");
			mSuggestionResource = new IdlingResources.SuggestionResource();
			IdlingResources.registerSuggestionResource(mSuggestionResource);

			// Espresso will not launch our activity for us, we must launch it via getActivity().
			getActivity();

			super.runTest();
		}
	}

	public void testBookHotel() throws Throwable {
		SpoonScreenshotUtils.screenshot("Launch", getInstrumentation());
		Launch.clickSearchButton();
		Launch.clickDestinationEditText();
		Launch.clickSuggestionAtPosition(0);

		SpoonScreenshotUtils.screenshot("Search", getInstrumentation());
		Results.swipeUpHotelList();
		SpoonScreenshotUtils.screenshot("Search_Results", getInstrumentation());
		Results.clickHotelAtIndex(1);
		SpoonScreenshotUtils.screenshot("Details", getInstrumentation());
		Results.clickAddHotel();
		Results.clickBookButton();

		Checkout.clickLoginButton();
		Common.closeSoftKeyboard(Checkout.loginButton());
		SpoonScreenshotUtils.screenshot("Login", getInstrumentation());
		Common.pressBack();

		SpoonScreenshotUtils.screenshot("Checkout1", getInstrumentation());
		Checkout.clickOnTravelerDetails();
		Checkout.enterFirstName("Mobiata");
		Checkout.enterLastName("Auto");
		Checkout.enterPhoneNumber("1112223333");
		Checkout.enterEmailAddress("aaa@aaa.com");
		Common.closeSoftKeyboard(Checkout.firstName());
		SpoonScreenshotUtils.screenshot("Traveler_Details", getInstrumentation());
		Checkout.clickOnDone();

		SpoonScreenshotUtils.screenshot("Checkout2", getInstrumentation());
		Checkout.clickOnEnterPaymentInformation();
		Checkout.enterCreditCardNumber("4111111111111111");
		Common.closeSoftKeyboard(Checkout.creditCardNumber());
		Checkout.setExpirationDate(2020, 12);
		Checkout.enterNameOnCard("Mobiata Auto");
		Checkout.enterPostalCode("95104");
		Common.closeSoftKeyboard(Checkout.postalCode());
		SpoonScreenshotUtils.screenshot("Payment_Details", getInstrumentation());
		Checkout.clickOnDone();

		SpoonScreenshotUtils.screenshot("Checkout3", getInstrumentation());
		try {
			Checkout.clickIAcceptButton();
		}
		catch (Exception e) {
			//no I accept button
		}
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
		Launch.clickSuggestionAtPosition(0);

		Results.clickOriginButton();
		Results.typeInOriginEditText("New York, USA");
		getInstrumentation().sendCharacterSync(KeyEvent.KEYCODE_SPACE);
		Results.clickSuggestionAtPosition(0);
		Results.clickSelectFlightDates();
		LocalDate startDate = LocalDate.now().plusDays(45);
		Results.clickDate(startDate, null);
		SpoonScreenshotUtils.screenshot("Search", getInstrumentation());
		Results.clickSearchPopupDone();
		Results.swipeUpFlightList();
		SpoonScreenshotUtils.screenshot("Search_Results", getInstrumentation());
		Results.clickFlightAtIndex(1);
		SpoonScreenshotUtils.screenshot("Details", getInstrumentation());
		Results.clickAddFlight();
		Results.clickBookButton();

		Checkout.clickLoginButton();
		Common.closeSoftKeyboard(Checkout.loginButton());
		SpoonScreenshotUtils.screenshot("Login", getInstrumentation());
		Common.pressBack();

		SpoonScreenshotUtils.screenshot("Checkout1", getInstrumentation());
		Checkout.clickOnTravelerDetails();
		Checkout.enterFirstName("Mobiata");
		Checkout.enterLastName("Auto");
		Checkout.enterPhoneNumber("1112223333");
		Common.closeSoftKeyboard(Checkout.phoneNumber());
		Checkout.enterDateOfBirth(1970, 1, 1);
		Checkout.enterEmailAddress("aaa@aaa.com");
		Common.closeSoftKeyboard(Checkout.emailAddress());
		SpoonScreenshotUtils.screenshot("Traveler_Details", getInstrumentation());
		Checkout.clickOnDone();

		SpoonScreenshotUtils.screenshot("Checkout2", getInstrumentation());
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
		SpoonScreenshotUtils.screenshot("Payment_Details", getInstrumentation());
		Checkout.clickOnDone();

		SpoonScreenshotUtils.screenshot("Checkout3", getInstrumentation());
		try {
			Checkout.clickIAcceptButton();
		}
		catch (Exception e) {
			//no I accept button
		}
		Checkout.slideToPurchase();
		Checkout.enterCvv("111");
		SpoonScreenshotUtils.screenshot("CVV", getInstrumentation());
		Checkout.clickBookButton();
		SpoonScreenshotUtils.screenshot("Confirmation", getInstrumentation());
		Checkout.clickDoneBooking();
	}

	@Override
	public void tearDown() throws Exception {
		super.tearDown();
		if (Common.isTablet(getInstrumentation())) {
			if (mSuggestionResource != null) {
				IdlingResources.unregisterSuggestionResource(mSuggestionResource);
			}
			Common.pressBackOutOfApp();
		}
	}
}
