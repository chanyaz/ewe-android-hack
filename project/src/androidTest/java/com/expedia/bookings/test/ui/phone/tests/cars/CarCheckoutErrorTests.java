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
		CarViewModel.alertDialog().check(matches(isDisplayed()));
		CarViewModel.alertDialogMessage().check(matches(withText(R.string.oops)));
		CarViewModel.alertDialogNeutralButton().perform(click());
		EspressoUtils.assertViewWithTextIsDisplayed("Slide to reserve");
		// TODO add test to support the retry behavior
		screenshot("Oops Error Dialog close");
	}

	public void testPriceChange() throws Throwable {
		performCarCheckout(CC_NOT_REQUIRED, "PriceChange");

		// Price change dialog
		screenshot("Price Change Dialog");
		CarViewModel.alertDialog().check(matches(isDisplayed()));
		CarViewModel.alertDialogMessage().check(matches(withText(R.string.reservation_price_change)));
		CarViewModel.alertDialogNeutralButton().perform(click());

		screenshot("Price Change Messaging");
		CarViewModel.checkoutTotalPrice().check(matches(withText("$125.18")));
		EspressoUtils.assertViewWithTextIsDisplayed("Price changed from $108.47");
		EspressoUtils.assertViewWithTextIsDisplayed("Slide to reserve");
	}

	public void testTripAlreadyBooked() throws Throwable {
		performCarCheckout(CC_NOT_REQUIRED, "AlreadyBooked");

		// Payment failed dialog
		screenshot("Trip Already Booked Dialog");
		CarViewModel.alertDialog().check(matches(isDisplayed()));
		CarViewModel.alertDialogMessage().check(matches(withText(R.string.reservation_already_exists)));
		CarViewModel.alertDialogNeutralButton().perform(click());

		screenshot("Car confirmation");
		EspressoUtils.assertViewWithTextIsDisplayed("Itinerary sent to");
	}

	public void testPaymentFailed() throws Throwable {
		// TODO use CC_REQUIRED, send user to Payment entry screen
		performCarCheckout(CC_NOT_REQUIRED, "PaymentFailed");

		// Payment failed dialog
		screenshot("Payment Failed Dialog");
		CarViewModel.alertDialog().check(matches(isDisplayed()));
		CarViewModel.alertDialogMessage().check(matches(withText(R.string.payment_failed)));
		CarViewModel.alertDialogNeutralButton().perform(click());

		screenshot("Payment Failed Messaging");
		// TODO better assertions once error is handled better
		EspressoUtils.assertViewWithTextIsDisplayed("Slide to reserve");
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

		if (offer == CC_REQUIRED) {
			CarViewModel.checkoutToolbarNext().perform(click());
			enterPaymentInfoWithScreenshot();
			enterCVV("111");
		}
		else {
			CarViewModel.checkoutToolbarDone().perform(click());
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
		CheckoutViewModel.pressClose();
	}

}
