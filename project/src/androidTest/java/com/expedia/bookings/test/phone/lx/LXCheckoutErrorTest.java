package com.expedia.bookings.test.phone.lx;

import java.util.concurrent.TimeUnit;

import org.joda.time.LocalDate;

import android.support.test.espresso.contrib.RecyclerViewActions;

import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.expedia.bookings.test.ui.phone.pagemodels.common.CVVEntryScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.CheckoutViewModel;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.LxTestCase;
import com.squareup.phrase.Phrase;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.espresso.ViewActions.waitFor;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.core.Is.is;

public class LXCheckoutErrorTest extends LxTestCase {

	public void testInvalidInput() throws Throwable {
		performLXCheckout("InvalidInput");

		// Invalid Input
		screenshot("Invalid Input Screen");

		LXScreen.checkoutErrorScreen().check(matches(isDisplayed()));
		LXScreen.checkoutErrorText().check(matches(withText(R.string.reservation_invalid_name)));
		LXScreen.checkoutErrorButton().perform(click());
		CheckoutViewModel.driverInfo().check(matches(isDisplayed()));
		CheckoutViewModel.pressClose();
		EspressoUtils.assertViewWithTextIsDisplayed("Slide to reserve");
	}

	public void testUnknownError() throws Throwable {
		performLXCheckout("UnknownError");

		// Generic dialog
		screenshot("Oops Error Dialog");
		LXScreen.checkoutErrorScreen().check(matches(isDisplayed()));
		LXScreen.checkoutErrorText()
			.check(matches(withText(Phrase.from(getActivity(), R.string.error_server_TEMPLATE).put("brand",
				BuildConfig.brand).format().toString())));
		LXScreen.checkoutErrorButton().perform(click());
		EspressoUtils.assertViewWithTextIsDisplayed("Security code for card ending in 1111");
	}

	public void testTripAlreadyBooked() throws Throwable {
		performLXCheckout("AlreadyBooked");
		screenshot("Trip Already Booked Dialog");
		LXScreen.checkoutErrorScreen().check(matches(isDisplayed()));
		LXScreen.checkoutErrorText().check(matches(withText(R.string.reservation_already_exists)));
		LXScreen.checkoutErrorButton().perform(click());
		screenshot("LX itins");
	}

	public void testSessionTimeout() throws Throwable {
		performLXCheckout("SessionTimeout");
		// Payment failed dialog
		screenshot("Session Timeout");
		LXScreen.checkoutErrorScreen().check(matches(isDisplayed()));
		LXScreen.checkoutErrorText().check(matches(withText(R.string.reservation_time_out)));
		LXScreen.checkoutErrorButton().perform(click());

		screenshot("LX Details");
		EspressoUtils.assertViewIsDisplayed(R.id.activity_gallery);
	}

	public void testPaymentFailed() throws Throwable {
		performLXCheckout("PaymentFailed");
		screenshot("Payment Failed Dialog");
		LXScreen.checkoutErrorScreen().check(matches(isDisplayed()));
		LXScreen.checkoutErrorText().check(matches(withText(R.string.reservation_payment_failed)));
		LXScreen.checkoutErrorButton().perform(click());
		screenshot("Payment Failed Messaging");
		// Should take you back to payment entry
		EspressoUtils.assertViewIsDisplayed(R.id.payment_info_card_view);
		CheckoutViewModel.pressClose();
		EspressoUtils.assertViewWithTextIsDisplayed("Slide to reserve");
	}

	public void testPriceChangeErrorMessageOnCVVScreen() throws Throwable {
		performLXCheckout("PriceChange");
		screenshot("Price Change Screen");
		LXScreen.checkoutErrorScreen().check(matches(isDisplayed()));
		LXScreen.checkoutErrorText().check(matches(withText(R.string.lx_error_price_changed)));
		//on click of the price change button we must come back to the Infosite Page
		LXScreen.checkoutErrorButton().perform(click());

		screenshot("Checkout after price change");
		CheckoutViewModel.enterPaymentInfo();
		CheckoutViewModel.clickDone();
		CheckoutViewModel.performSlideToPurchase();

		CVVEntryScreen.parseAndEnterCVV("111");
		CVVEntryScreen.clickBookButton();

		// this time click on the back button. Expected : we must come to the CVV Screen
		CheckoutViewModel.pressClose();
		onView(withId(R.id.lx_base_presenter)).inRoot(
			withDecorView(is(getActivity().getWindow().getDecorView())))
			.perform(waitFor((isDisplayed()), 10L, TimeUnit.SECONDS));
		onView(withId(R.id.signature_text_view)).check(matches(isDisplayed()));
		screenshot("CVV screen after price change");
	}

	private void performLXCheckout(String firstName) throws Throwable {
		final String ticketName = "2-Day";

		if (getLxIdlingResource().isInSearchEditMode()) {
			onView(allOf(withId(R.id.error_action_button), withText(R.string.edit_search))).perform(click());
			LXScreen.location().perform(typeText("San"));
			LXScreen.selectLocation("San Francisco, CA");
			LXScreen.selectDateButton().perform(click());
			LXScreen.selectDates(LocalDate.now(), null);
			LXScreen.searchButton().perform(click());
		}

		LXScreen.waitForSearchListDisplayed();
		LXScreen.searchList().perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
		LXScreen.waitForDetailsDisplayed();
		LXInfositeScreen.selectOffer("2-Day New York Pass").perform(click());
		LXInfositeScreen.bookNowButton("2-Day New York Pass").perform(scrollTo(), click());
		CheckoutViewModel.driverInfo().perform(click());
		CheckoutViewModel.firstName().perform(typeText(firstName));
		CheckoutViewModel.lastName().perform(typeText("Test"));
		CheckoutViewModel.email().perform(typeText("test@expedia.com"));
		CheckoutViewModel.phone().perform(typeText("4151234567"));
		CheckoutViewModel.clickDone();
		CheckoutViewModel.enterPaymentInfo();
		CheckoutViewModel.clickDone();
		CheckoutViewModel.performSlideToPurchase();

		CVVEntryScreen.parseAndEnterCVV("111");
		CVVEntryScreen.clickBookButton();
	}
}
