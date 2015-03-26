package com.expedia.bookings.test.component.lx;

import java.util.concurrent.TimeUnit;

import org.joda.time.LocalDate;

import android.support.test.espresso.contrib.RecyclerViewActions;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.LXBaseActivity;
import com.expedia.bookings.test.component.lx.pagemodels.LXInfositePageModel;
import com.expedia.bookings.test.ui.phone.pagemodels.common.CVVEntryScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.CheckoutViewModel;
import com.expedia.bookings.test.ui.utils.EspressoUtils;
import com.expedia.bookings.test.ui.utils.PhoneTestCase;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

import static com.expedia.bookings.test.ui.espresso.ViewActions.clickOnFirstEnabled;
import static com.expedia.bookings.test.ui.espresso.ViewActions.waitFor;

public class LXCheckoutErrorTests extends PhoneTestCase {

	public LXCheckoutErrorTests() {
		super(LXBaseActivity.class);
	}

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
		screenshot("Oops Error Dialog close");
	}

	public void testTripAlreadyBooked() throws Throwable {
		performLXCheckout("AlreadyBooked");

		// Payment failed dialog
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

		// Payment failed dialog
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

	private void performLXCheckout(String firstName) throws Throwable {
		final String ticketName = "2-Day";

		screenshot("LX Search");
		LXViewModel.location().perform(typeText("San"));
		LXViewModel.selectLocation(getInstrumentation(), "San Francisco, CA");
		LXViewModel.selectDateButton().perform(click());
		LXViewModel.selectDates(LocalDate.now(), null);
		screenshot("LX Search Params Entered");
		LXViewModel.searchButton().perform(click());

		screenshot("LX Search Results");

		onView(withId(R.id.lx_search_results_list)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
		onView(withId(R.id.loading_details)).perform(waitFor(10L, TimeUnit.SECONDS));
		screenshot("LX Details");

		LXViewModel.detailsDateContainer().perform(scrollTo(),clickOnFirstEnabled());
		LXViewModel.selectTicketsButton("2-Day New York Pass").perform(scrollTo(), click());
		LXInfositePageModel.ticketAddButton(ticketName, "Adult").perform(scrollTo(), click());
		LXInfositePageModel.bookNowButton(ticketName).perform(scrollTo(), click());

		screenshot("LX Checkout Started");
		CheckoutViewModel.driverInfo().perform(click());
		CheckoutViewModel.firstName().perform(typeText(firstName));
		CheckoutViewModel.lastName().perform(typeText("Test"));
		CheckoutViewModel.email().perform(typeText("test@expedia.com"));
		CheckoutViewModel.phone().perform(typeText("4151234567"));
		CheckoutViewModel.clickDone();
		CheckoutViewModel.enterPaymentInfo();
		CheckoutViewModel.clickDone();
		screenshot("LX Checkout Ready");
		CheckoutViewModel.performSlideToPurchase();

		CVVEntryScreen.parseAndEnterCVV("111");
		screenshot("LX CVV");
		CVVEntryScreen.clickBookButton();

		screenshot("LX Checkout Started");
	}
}
