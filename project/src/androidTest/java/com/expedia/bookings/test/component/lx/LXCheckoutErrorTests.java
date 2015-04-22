package com.expedia.bookings.test.component.lx;

import java.util.concurrent.TimeUnit;

import org.hamcrest.CoreMatchers;
import org.joda.time.LocalDate;

import android.support.test.espresso.contrib.RecyclerViewActions;

import com.expedia.bookings.R;
import com.expedia.bookings.test.component.lx.pagemodels.LXInfositePageModel;
import com.expedia.bookings.test.ui.phone.pagemodels.common.CVVEntryScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.CheckoutViewModel;
import com.expedia.bookings.test.ui.utils.EspressoUtils;
import com.expedia.bookings.test.ui.utils.LxTestCase;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.ui.espresso.ViewActions.waitFor;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.core.Is.is;

public class LXCheckoutErrorTests extends LxTestCase {

	public void testInvalidInput() throws Throwable {
		performLXCheckout("InvalidInput");

		// Invalid Input
		screenshot("Invalid Input Screen");

		LXViewModel.checkoutErrorScreen().check(matches(isDisplayed()));
		LXViewModel.checkoutErrorText().check(matches(withText(R.string.reservation_invalid_name)));
		LXViewModel.checkoutErrorButton().perform(click());
		CheckoutViewModel.driverInfo().check(matches(isDisplayed()));
		CheckoutViewModel.pressClose();
		EspressoUtils.assertViewWithTextIsDisplayed("Slide to reserve");
	}

	public void testUnknownError() throws Throwable {
		performLXCheckout("UnknownError");

		// Generic dialog
		screenshot("Oops Error Dialog");
		LXViewModel.checkoutErrorScreen().check(matches(isDisplayed()));
		LXViewModel.checkoutErrorText().check(matches(withText(R.string.error_server)));
		LXViewModel.checkoutErrorButton().perform(click());
		EspressoUtils.assertViewWithTextIsDisplayed("Security code for card ending in 1111");
	}

	public void testTripAlreadyBooked() throws Throwable {
		performLXCheckout("AlreadyBooked");
		screenshot("Trip Already Booked Dialog");
		LXViewModel.checkoutErrorScreen().check(matches(isDisplayed()));
		LXViewModel.checkoutErrorText().check(matches(withText(R.string.reservation_already_exists)));
		LXViewModel.checkoutErrorButton().perform(click());
		screenshot("LX itins");
	}

	public void testSessionTimeout() throws Throwable {
		performLXCheckout("SessionTimeout");
		// Payment failed dialog
		screenshot("Session Timeout");
		LXViewModel.checkoutErrorScreen().check(matches(isDisplayed()));
		LXViewModel.checkoutErrorText().check(matches(withText(R.string.reservation_time_out)));
		LXViewModel.checkoutErrorButton().perform(click());

		screenshot("LX Details");
		EspressoUtils.assertViewIsDisplayed(R.id.activity_gallery);
	}

	public void testPaymentFailed() throws Throwable {
		performLXCheckout("PaymentFailed");
		screenshot("Payment Failed Dialog");
		LXViewModel.checkoutErrorScreen().check(matches(isDisplayed()));
		LXViewModel.checkoutErrorText().check(matches(withText(R.string.reservation_payment_failed)));
		LXViewModel.checkoutErrorButton().perform(click());
		screenshot("Payment Failed Messaging");
		// Should take you back to payment entry
		EspressoUtils.assertViewIsDisplayed(R.id.payment_info_card_view);
		CheckoutViewModel.pressClose();
		EspressoUtils.assertViewWithTextIsDisplayed("Slide to reserve");
	}

	public void testPriceChangeErrorMessageOnCVVScreen() throws Throwable {
		performLXCheckout("PriceChange");
		screenshot("Price Change Screen");
		LXViewModel.checkoutErrorScreen().check(matches(isDisplayed()));
		LXViewModel.checkoutErrorText().check(matches(withText(R.string.lx_error_price_changed)));
		//on click of the price change button we must come back to the Infosite Page
		LXViewModel.checkoutErrorButton().perform(click());
		screenshot("Infosite Page after price change");
		onView(CoreMatchers.allOf(withId(R.id.section_title), withText(
			R.string.highlights_activity_details))).check(matches(
			isDisplayed()));
		//click book now button again so that we can test if upon click of Back button we must reach back to CVV Screen
		LXInfositePageModel.bookNowButton("2-Day New York Pass").perform(scrollTo(), click());
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
			LXViewModel.location().perform(typeText("San"));
			LXViewModel.selectLocation(getInstrumentation(), "San Francisco, CA");
			LXViewModel.selectDateButton().perform(click());
			LXViewModel.selectDates(LocalDate.now(), null);
			LXViewModel.searchButton().perform(click());
		}

		LXViewModel.waitForSearchListDisplayed();
		LXViewModel.searchList().perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
		LXViewModel.waitForDetailsDisplayed();
		LXInfositePageModel.bookNowButton("2-Day New York Pass").perform(scrollTo(), click());
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
