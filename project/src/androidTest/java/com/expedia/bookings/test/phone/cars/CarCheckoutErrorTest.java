package com.expedia.bookings.test.phone.cars;

import org.junit.Test;

import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.expedia.bookings.data.cars.CarCreateTripResponse;
import com.expedia.bookings.data.cars.SearchCarOffer;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.services.CarServices;
import com.expedia.bookings.test.espresso.CarTestCase;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.pagemodels.cars.CarScreen;
import com.expedia.bookings.test.pagemodels.common.CVVEntryScreen;
import com.expedia.bookings.test.pagemodels.common.CheckoutViewModel;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.squareup.phrase.Phrase;

import okio.Okio;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

public class CarCheckoutErrorTest extends CarTestCase {

	private static final int CC_NOT_REQUIRED = 0;
	private static final int CC_REQUIRED = 1;
	private static final String EMAIL = "george@mobiata.com";

	@Test
	public void testUnknownError() throws Throwable {
		performCarCheckout(CC_NOT_REQUIRED, "UnknownError");

		// Generic message
		screenshot("Default Error Message");
		CarScreen.checkoutErrorScreen().check(matches(isDisplayed()));
		CarScreen.checkoutErrorText()
			.check(matches(withText(Phrase.from(getActivity(), R.string.error_server_TEMPLATE).put("brand",
				BuildConfig.brand).format().toString())));
		CarScreen.checkoutErrorButton().perform(click());
		EspressoUtils.assertViewWithTextIsDisplayed("Slide to reserve");
		screenshot("Default Error Message Close");
	}

	@Test
	public void testPriceChange() throws Throwable {
		performCarCheckout(CC_NOT_REQUIRED, "PriceChange");

		// Price change message
		screenshot("Price Change Message");
		CarScreen.checkoutErrorScreen().check(matches(isDisplayed()));
		CarScreen.checkoutErrorText().check(matches(withText(R.string.reservation_price_change)));
		CarScreen.checkoutErrorButton().perform(click());

		screenshot("Price Change Resolution");
		CarScreen.checkoutTotalPrice().check(matches(withText("$125.18")));
		EspressoUtils.assertViewWithTextIsDisplayed("Price changed from $108.47");
		EspressoUtils.assertViewWithTextIsDisplayed("Slide to reserve");
	}

	@Test
	public void testInvalidInput() throws Throwable {
		performCarCheckout(CC_NOT_REQUIRED, "InvalidInput");

		// Invalid Input
		screenshot("Invalid Input Screen");
		CarScreen.checkoutErrorScreen().check(matches(isDisplayed()));
		CarScreen.checkoutErrorText().check(matches(withText(R.string.reservation_invalid_name)));

		CarScreen.checkoutErrorButton().perform(click());
		screenshot("Traveler Details");
		EspressoUtils.assertViewIsDisplayed(R.id.main_contact_info_card_view);

		CheckoutViewModel.pressClose();
		screenshot("Still Ready to Purchase");
		EspressoUtils.assertViewWithTextIsDisplayed("Slide to reserve");
	}

	@Test
	public void testTripAlreadyBooked() throws Throwable {
		performCarCheckout(CC_NOT_REQUIRED, "AlreadyBooked");

		// Payment failed message
		screenshot("Trip Already Booked Message");
		CarScreen.checkoutErrorScreen().check(matches(isDisplayed()));
		CarScreen.checkoutErrorText().check(matches(withText(R.string.reservation_already_exists)));

		CarScreen.checkoutErrorButton().perform(click());
		screenshot("Trips");
	}

	@Test
	public void testPaymentFailed() throws Throwable {
		performCarCheckout(CC_REQUIRED, "PaymentFailed");

		// Payment failed message
		screenshot("Payment Failed Message");
		CarScreen.checkoutErrorScreen().check(matches(isDisplayed()));
		CarScreen.checkoutErrorText().check(matches(withText(R.string.reservation_payment_failed)));
		CarScreen.checkoutErrorButton().perform(click());

		screenshot("Payment Failed Recourse");
		EspressoUtils.assertViewIsDisplayed(R.id.payment_info_card_view);

		CheckoutViewModel.pressClose();
		EspressoUtils.assertViewWithTextIsDisplayed("Slide to reserve");
		screenshot("Payment Failed Recourse Complete");
	}

	@Test
	public void testSessionTimeout() throws Throwable {
		performCarCheckout(CC_NOT_REQUIRED, "SessionTimeout");

		screenshot("Session Timeout");
		CarScreen.checkoutErrorScreen().check(matches(isDisplayed()));
		CarScreen.checkoutErrorText().check(matches(withText(R.string.reservation_time_out)));
	}

	/**
	 * Go to checkout as quickly as possible. Load up JSON from assets dir, and post the
	 * checkout display event.
	 *
	 * @param offer
	 * @param firstName
	 * @throws Throwable
	 */
	private void performCarCheckout(int offer, String firstName) throws Throwable {
		String offerFilename;
		String createFileName;
		if (offer == CC_REQUIRED) {
			offerFilename = "SearchCarOffer_CCRequired.json";
			createFileName = "CarCreateTripResponse_CCRequired.json";
		}
		else if (offer == CC_NOT_REQUIRED) {
			offerFilename = "SearchCarOffer_CCNotRequired.json";
			createFileName = "CarCreateTripResponse_CCNotRequired.json";
		}
		else {
			throw new RuntimeException("Car offer not valid");
		}
		SearchCarOffer searchCarOffer;
		CarCreateTripResponse carCreateTripResponse;
		Gson gson = CarServices.generateGson();

		String offerStr = Okio.buffer(Okio.source(getInstrumentation().getContext().getAssets().open(offerFilename))).readUtf8();
		String createStr = Okio.buffer(Okio.source(getInstrumentation().getContext().getAssets().open(createFileName))).readUtf8();
		searchCarOffer = gson.fromJson(offerStr, SearchCarOffer.class);
		carCreateTripResponse = gson.fromJson(createStr, CarCreateTripResponse.class);
		if ("PriceChange".equals(firstName)) {
			carCreateTripResponse.searchCarOffer = searchCarOffer;
		}
		CarScreen.waitForSearchScreen();
		Events.post(new Events.CarsShowCheckout(searchCarOffer.productKey, searchCarOffer.fare.total, searchCarOffer.isInsuranceIncluded, new LatLng(searchCarOffer.pickUpLocation.latitude, searchCarOffer.pickUpLocation.longitude)));
		CheckoutViewModel.waitForCheckout();

		CarScreen.travelerWidget().perform(click());
		CarScreen.firstName().perform(typeText(firstName));
		CarScreen.lastName().perform(typeText("Test"));
		CarScreen.email().perform(typeText(EMAIL));
		CarScreen.phoneNumber().perform(typeText("4151234567"));
		CarScreen.checkoutToolbarDone().perform(click());

		if (offer == CC_REQUIRED) {
			enterPaymentInfoWithScreenshot();
			CheckoutViewModel.performSlideToPurchase();
			enterCVV("111");
		}
		else {
			Common.delay(1);
			CheckoutViewModel.performSlideToPurchase();
		}
	}

	private void enterCVV(String cvv) throws Throwable {
		CVVEntryScreen.enterCVV(cvv);
		screenshot("Car CVV");
		CVVEntryScreen.clickBookButton();
	}

	private void enterPaymentInfoWithScreenshot() throws Throwable {
		EspressoUtils.assertViewIsDisplayed(R.id.payment_info_card_view);
		CheckoutViewModel.enterPaymentInfo();
		screenshot("Car Checkout Payment Entered");
	}

}
