package com.expedia.bookings.test.phone.pagemodels.flights;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;

import android.support.test.espresso.ViewInteraction;

import static android.support.test.espresso.action.ViewActions.click;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

public class FlightLegScreen {

	public static ViewInteraction selectFlightButton() {
		return onView(withId(R.id.select_text_view));
	}

	public static ViewInteraction costBreakdownButtonView() {
		return onView(withId(R.id.price_section));
	}

	public static ViewInteraction costBreakdownDoneButton() {
		return onView(withId(R.id.done_button));
	}

	// Object interaction

	public static void clickSelectFlightButton() {
		Common.delay(1);
		selectFlightButton().perform(click());
	}

	public static void clickCostBreakdownButtonView() {
		costBreakdownButtonView().perform(click());
	}

	public static void clickCostBreakdownDoneButton() {
		costBreakdownDoneButton().perform(click());
	}
}
