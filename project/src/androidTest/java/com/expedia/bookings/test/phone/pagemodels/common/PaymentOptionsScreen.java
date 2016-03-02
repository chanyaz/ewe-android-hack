package com.expedia.bookings.test.phone.pagemodels.common;

import android.support.test.espresso.ViewInteraction;

import com.expedia.bookings.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;

/**
 * Created by mswami on 1/15/16.
 */
public class PaymentOptionsScreen {

	public static ViewInteraction totalPointsAvailableText() {
		return onView(withId(R.id.total_points_available_view));
	}

	public static ViewInteraction pointsAppliedText() {
		return onView(withId(R.id.message_view));
	}

	public static ViewInteraction pwpToggleSwitch() {
		return onView(withId(R.id.pwp_switch));
	}

	public static ViewInteraction currencySymbolIcon() {
		return onView(withId(R.id.currency_symbol_view));
	}

	public static ViewInteraction editAmountTextView() {
		return onView(withId(R.id.edit_amount_view));
	}

	public static ViewInteraction clearPointsButton() {
		return onView(withId(R.id.clear_btn));
	}

	public static ViewInteraction cardPaymentOption() {
		return onView(withId(R.id.payment_option_credit_debit));
	}

	public void turnPwpOff() {
		onView(allOf(withId(R.id.pwp_switch), withText("ON"))).perform(click());
	}

	public void turnPwpOn() {
		onView(allOf(withId(R.id.pwp_switch), withText("OFF"))).perform(click());
	}

	public static void enterAmountForPointsCalculation(String points) {
		editAmountTextView().perform(replaceText(points));
	}

	public static void clearPoints() {
		clearPointsButton().perform(click());
	}

	public static void openCardPaymentSection() {
		cardPaymentOption().perform(click());
	}

	public static void assertTextInEditAmountMatches(String amount) {
		onView(withId(R.id.pwp_edit_box_container)).check(matches(hasDescendant(allOf(withId(R.id.edit_amount_view),
			withText(amount)))));
	}

	public static void assertTotalDueAmountMatches(String amount) {
		onView(withId(R.id.section_payment_options_container))
			.check(matches(hasDescendant(allOf(withId(R.id.total_due_today_amount), withText(containsString(amount))))));
	}

	public static void assertTotalPointsAvailableMatches(String points) {
		onView(withId(R.id.section_payment_options_container))
			.check(matches(
				hasDescendant(allOf(withId(R.id.total_points_available_view), withText(containsString(points))))));
	}

	public static void assertPointsAppliedMatches(String points) {
		onView(withId(R.id.section_payment_options_container))
			.check(matches(
				hasDescendant(allOf(withId(R.id.message_view), withText(containsString(points))))));
	}

	public static void assertRemainingDueMatches(String amount) {
		onView(withId(R.id.section_payment_options_container))
			.check(matches(hasDescendant(allOf(withId(R.id.remaining_balance_amount), withText(containsString(amount))))));

	}

	public static void enterCardInfo() {
		openCardPaymentSection();
		CheckoutViewModel.enterPaymentDetails();
		CheckoutViewModel.pressClose();
	}
}
