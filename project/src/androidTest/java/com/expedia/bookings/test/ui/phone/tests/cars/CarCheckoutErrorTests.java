package com.expedia.bookings.test.ui.phone.tests.cars;

import com.expedia.bookings.R;
import com.expedia.bookings.data.cars.CarCreateTripResponse;
import com.expedia.bookings.data.cars.SearchCarOffer;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.services.CarServices;
import com.expedia.bookings.test.component.cars.CarViewModel;
import com.expedia.bookings.test.ui.phone.pagemodels.common.CVVEntryScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.CheckoutViewModel;
import com.expedia.bookings.test.ui.phone.pagemodels.common.ScreenActions;
import com.expedia.bookings.test.ui.utils.CarTestCase;
import com.expedia.bookings.test.ui.utils.EspressoUtils;
import com.google.gson.Gson;
import com.mobiata.android.util.IoUtils;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

public class CarCheckoutErrorTests extends CarTestCase {

	private static final int CC_NOT_REQUIRED = 0;
	private static final int CC_REQUIRED = 1;
	private static final String EMAIL = "george@mobiata.com";

	public void testUnknownError() throws Throwable {
		performCarCheckout(CC_NOT_REQUIRED, "UnknownError");

		// Generic message
		screenshot("Default Error Message");
		CarViewModel.checkoutErrorScreen().check(matches(isDisplayed()));
		CarViewModel.checkoutErrorText().check(matches(withText(R.string.error_server)));
		CarViewModel.checkoutErrorButton().perform(click());
		EspressoUtils.assertViewWithTextIsDisplayed("Slide to reserve");
		// TODO add test to support the retry behavior
		screenshot("Default Error Message Close");
	}

	public void testPriceChange() throws Throwable {
		performCarCheckout(CC_NOT_REQUIRED, "PriceChange");

		// Price change message
		screenshot("Price Change Message");
		CarViewModel.checkoutErrorScreen().check(matches(isDisplayed()));
		CarViewModel.checkoutErrorText().check(matches(withText(R.string.reservation_price_change)));
		CarViewModel.checkoutErrorButton().perform(click());

		screenshot("Price Change Resolution");
		CarViewModel.checkoutTotalPrice().check(matches(withText("$125.18")));
		EspressoUtils.assertViewWithTextIsDisplayed("Price changed from $108.47");
		EspressoUtils.assertViewWithTextIsDisplayed("Slide to reserve");
	}

	public void testInvalidInput() throws Throwable {
		performCarCheckout(CC_NOT_REQUIRED, "InvalidInput");

		// Invalid Input
		screenshot("Invalid Input Screen");
		CarViewModel.checkoutErrorScreen().check(matches(isDisplayed()));
		CarViewModel.checkoutErrorText().check(matches(withText(R.string.reservation_invalid_name)));

		CarViewModel.checkoutErrorButton().perform(click());
		screenshot("Traveler Details");
		EspressoUtils.assertViewIsDisplayed(R.id.main_contact_info_card_view);

		CheckoutViewModel.pressClose();
		screenshot("Still Ready to Purchase");
		EspressoUtils.assertViewWithTextIsDisplayed("Slide to reserve");
	}

	public void testTripAlreadyBooked() throws Throwable {
		performCarCheckout(CC_NOT_REQUIRED, "AlreadyBooked");

		// Payment failed message
		screenshot("Trip Already Booked Message");
		CarViewModel.checkoutErrorScreen().check(matches(isDisplayed()));
		CarViewModel.checkoutErrorText().check(matches(withText(R.string.reservation_already_exists)));

		CarViewModel.checkoutErrorButton().perform(click());
		screenshot("Trips");
	}

	public void testPaymentFailed() throws Throwable {
		performCarCheckout(CC_REQUIRED, "PaymentFailed");

		// Payment failed message
		screenshot("Payment Failed Message");
		CarViewModel.checkoutErrorScreen().check(matches(isDisplayed()));
		CarViewModel.checkoutErrorText().check(matches(withText(R.string.reservation_payment_failed)));
		CarViewModel.checkoutErrorButton().perform(click());

		screenshot("Payment Failed Recourse");
		EspressoUtils.assertViewIsDisplayed(R.id.payment_info_card_view);

		CheckoutViewModel.pressClose();
		EspressoUtils.assertViewWithTextIsDisplayed("Slide to reserve");
		screenshot("Payment Failed Recourse Complete");
	}

	public void testSessionTimeout() throws Throwable {
		performCarCheckout(CC_NOT_REQUIRED, "SessionTimeout");

		screenshot("Session Timeout");
		CarViewModel.checkoutErrorScreen().check(matches(isDisplayed()));
		CarViewModel.checkoutErrorText().check(matches(withText(R.string.reservation_time_out)));

		// TODO find a good way to verify this behavior without increasing test time
		//		CarViewModel.checkoutErrorButton().perform(click());
		//		screenshot("Car Details");
		//		EspressoUtils.assertViewIsDisplayed(R.id.details);
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

		String offerStr = IoUtils.convertStreamToString(
			getInstrumentation().getContext().getAssets().open(offerFilename));
		String createStr = IoUtils.convertStreamToString(
			getInstrumentation().getContext().getAssets().open(createFileName));
		searchCarOffer = gson.fromJson(offerStr, SearchCarOffer.class);
		carCreateTripResponse = gson.fromJson(createStr, CarCreateTripResponse.class);
		if ("PriceChange".equals(firstName)) {
			carCreateTripResponse.searchCarOffer = searchCarOffer;
		}
		Events.post(new Events.CarsShowCheckout(searchCarOffer));

		CarViewModel.travelerWidget().perform(click());
		CarViewModel.firstName().perform(typeText(firstName));
		CarViewModel.lastName().perform(typeText("Test"));
		CarViewModel.email().perform(typeText(EMAIL));
		CarViewModel.phoneNumber().perform(typeText("4151234567"));
		CarViewModel.checkoutToolbarDone().perform(click());

		if (offer == CC_REQUIRED) {
			enterPaymentInfoWithScreenshot();
			CheckoutViewModel.performSlideToPurchase();
			enterCVV("111");
		}
		else {
			ScreenActions.delay(1);
			CheckoutViewModel.performSlideToPurchase();
		}
	}

	private void enterCVV(String cvv) throws Throwable {
		CVVEntryScreen.parseAndEnterCVV(cvv);
		screenshot("Car CVV");
		CVVEntryScreen.clickBookButton();
	}

	private void enterPaymentInfoWithScreenshot() throws Throwable {
		EspressoUtils.assertViewIsDisplayed(R.id.payment_info_card_view);
		CheckoutViewModel.enterPaymentInfo();
		screenshot("Car Checkout Payment Entered");
		CheckoutViewModel.clickDone();
	}

}
