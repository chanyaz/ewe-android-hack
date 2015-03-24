package com.expedia.bookings.test.ui.phone.tests.cars;

import org.joda.time.DateTime;

import com.expedia.bookings.R;
import com.expedia.bookings.test.component.cars.CarViewModel;
import com.expedia.bookings.test.ui.phone.pagemodels.common.CVVEntryScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.CheckoutViewModel;
import com.expedia.bookings.test.ui.phone.pagemodels.common.LaunchScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.ScreenActions;
import com.expedia.bookings.test.ui.utils.EspressoUtils;
import com.expedia.bookings.test.ui.utils.PhoneTestCase;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

public class CarCheckoutErrorTests extends PhoneTestCase {

	private static final String CATEGORY = "Standard";
	private static final int CC_NOT_REQUIRED = 0;
	private static final int CC_REQUIRED = 1;
	private static final String EMAIL = "george@mobiata.com";

	public void testUnknownError() throws Throwable {
		performCarCheckout(CC_NOT_REQUIRED, "UnknownError");

		// Generic dialog
		screenshot("Oops Error Dialog");
		CarViewModel.errorScreen().check(matches(isDisplayed()));
		CarViewModel.errorText().check(matches(withText(R.string.oops)));
		CarViewModel.errorButton().perform(click());
		EspressoUtils.assertViewWithTextIsDisplayed("Slide to reserve");
		// TODO add test to support the retry behavior
		screenshot("Oops Error Dialog close");
	}

	public void testPriceChange() throws Throwable {
		performCarCheckout(CC_NOT_REQUIRED, "PriceChange");

		// Price change dialog
		screenshot("Price Change Dialog");
		CarViewModel.errorScreen().check(matches(isDisplayed()));
		CarViewModel.errorText().check(matches(withText(R.string.reservation_price_change)));
		CarViewModel.errorButton().perform(click());

		screenshot("Price Change Messaging");
		CarViewModel.checkoutTotalPrice().check(matches(withText("$125.18")));
		EspressoUtils.assertViewWithTextIsDisplayed("Price changed from $108.47");
		EspressoUtils.assertViewWithTextIsDisplayed("Slide to reserve");
	}

	public void testTripAlreadyBooked() throws Throwable {
		performCarCheckout(CC_NOT_REQUIRED, "AlreadyBooked");

		// Payment failed dialog
		screenshot("Trip Already Booked Dialog");
		CarViewModel.errorScreen().check(matches(isDisplayed()));
		CarViewModel.errorText().check(matches(withText(R.string.reservation_already_exists)));
		CarViewModel.errorButton().perform(click());

		screenshot("Car confirmation");
	}

	public void testPaymentFailed() throws Throwable {
		performCarCheckout(CC_REQUIRED, "PaymentFailed");

		// Payment failed dialog
		screenshot("Payment Failed Dialog");
		CarViewModel.errorScreen().check(matches(isDisplayed()));
		CarViewModel.errorText().check(matches(withText(R.string.reservation_payment_failed)));
		CarViewModel.errorButton().perform(click());

		screenshot("Payment Failed Messaging");
		// Should take you back to payment entry
		EspressoUtils.assertViewIsDisplayed(R.id.payment_info_card_view);
		CheckoutViewModel.pressClose();

		// TODO better assertions once error is handled better
		EspressoUtils.assertViewWithTextIsDisplayed("Slide to reserve");
	}

	public void testSessionTimeout() throws Throwable {
		performCarCheckout(CC_NOT_REQUIRED, "SessionTimeout");

		// Payment failed dialog
		screenshot("Trip Already Booked Dialog");
		CarViewModel.errorScreen().check(matches(isDisplayed()));
		CarViewModel.errorText().check(matches(withText(R.string.reservation_time_out)));
		CarViewModel.errorButton().perform(click());

		screenshot("Car Details");
		EspressoUtils.assertViewIsDisplayed(R.id.details);
	}

	private void performCarCheckout(int offer, String firstName) throws Throwable {
		screenshot("Launch");
		LaunchScreen.launchCars();
		final DateTime startDateTime = DateTime.now().withTimeAtStartOfDay();

		screenshot("Car Search");
		CarViewModel.pickupLocation().perform(typeText("SFO"));
		CarViewModel.selectPickupLocation(getInstrumentation(), "San Francisco, CA");
		CarViewModel.selectDateButton().perform(click());
		CarViewModel.selectDates(startDateTime.toLocalDate(), startDateTime.toLocalDate());
		CarViewModel.searchButton().perform(click());

		screenshot("Car Results");
		CarViewModel.selectCarCategory(CATEGORY);
		screenshot("Car Details");
		CarViewModel.selectCarOffer(offer);
		screenshot("Car Checkout");
		CarViewModel.travelerWidget().perform(click());
		screenshot("Car Checkout - traveler details");
		CarViewModel.firstName().perform(typeText(firstName));
		CarViewModel.lastName().perform(typeText("Test"));
		CarViewModel.email().perform(typeText(EMAIL));
		CarViewModel.phoneNumber().perform(typeText("4151234567"));
		screenshot("Car Checkout - traveler details entered");
		CarViewModel.checkoutToolbarDone().perform(click());

		if (offer == CC_REQUIRED) {
			enterPaymentInfoWithScreenshot();
			CheckoutViewModel.performSlideToPurchase();
			enterCVV("111");
		}
		else {
			screenshot("Car Checkout - ready to purchase");
			ScreenActions.delay(1);
			CheckoutViewModel.performSlideToPurchase();
		}
	}

	private void enterCVV(String cvv) throws Throwable {
		CVVEntryScreen.parseAndEnterCVV(cvv);
		CVVEntryScreen.clickBookButton();
		screenshot("Car_CVV");
	}

	private void enterPaymentInfoWithScreenshot() throws Throwable {
		EspressoUtils.assertViewIsDisplayed(R.id.payment_info_card_view);
		CheckoutViewModel.enterPaymentInfo();
		screenshot("Car_Checkout_Payment_Entered");
		CheckoutViewModel.clickDone();
	}

}
