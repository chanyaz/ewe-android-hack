package com.expedia.bookings.test.ui.phone.pagemodels.flights;


import com.expedia.bookings.R;

import android.support.test.espresso.ViewInteraction;

import static android.support.test.espresso.action.ViewActions.click;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * Created by dmadan on 4/7/14.
 */
public class FlightLegScreen extends ScreenActions {
	private static final int SELECT_FLIGHT_BUTTON_ID = R.id.select_text_view;
	private static final int CANCEL_BUTTON_ID = R.id.cancel_button;
	private static final int BAGGAGE_FEE_INFO_VIEW_ID = R.id.fees_text_view;

// Object access

	public static ViewInteraction selectFlightButton() {
		return onView(withId(SELECT_FLIGHT_BUTTON_ID));
	}

	public static ViewInteraction cancelButton() {
		return onView(withId(CANCEL_BUTTON_ID));
	}

	public static ViewInteraction baggageFeeInfoTextView() {
		return onView(withId(BAGGAGE_FEE_INFO_VIEW_ID));
	}

	// Object interaction

	public static void clickSelectFlightButton() {
		selectFlightButton().perform(click());
	}

	public static void clickCancelButton() {
		cancelButton().perform(click());
	}

	public static void clickBaggageInfoView() {
		baggageFeeInfoTextView().perform(click());
	}
}
