package com.expedia.bookings.test.phone.pagemodels.common;

import android.support.test.espresso.ViewInteraction;
import com.expedia.bookings.R;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;

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
		editAmountTextView().perform(typeText(points));
	}

	public static void clearPoints() {
		clearPointsButton().perform(click());
	}

	public static void openCardPaymentSection() {
		cardPaymentOption().perform(click());
	}
}
