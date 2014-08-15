package com.expedia.bookings.test.phone.pagemodels.flights;


import com.expedia.bookings.R;
import com.expedia.bookings.test.phone.pagemodels.common.ScreenActions;
import com.google.android.apps.common.testing.ui.espresso.ViewInteraction;

import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;

/**
 * Created by dmadan on 4/7/14.
 */
public class FlightLegScreen extends ScreenActions {
	private static final int SELECT_FLIGHT_BUTTON_ID = R.id.select_text_view;
	private static final int LEFT_TEXT_VIEW_ID = R.id.left_text_view;
	private static final int RIGHT_TEXT_VIEW_ID = R.id.right_text_view;
	private static final int AIRLINE_TEXT_VIEW_ID = R.id.airline_text_view;
	private static final int DEPARTURE_TIME_TEXT_VIEW_ID = R.id.departure_time_text_view;
	private static final int ARRIVAL_TIME_TEXT_VIEW_ID = R.id.arrival_time_text_view;
	private static final int PRICE_TEXT_VIEW_ID = R.id.price_text_view;
	private static final int CANCEL_BUTTON_ID = R.id.cancel_button;
	private static final int BAGGAGE_FEE_INFO_VIEW_ID = R.id.fees_text_view;
	private static final int CHECKING_PRICE_CHANGES_VIEW_ID = R.string.loading_flight_details;
	private static final int BAGGAGE_FEES_STRING_ID = R.string.baggage_fees;
	private static final int DETAILS_TEXT_VIEW_ID = R.id.details_text_view;

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

	public static ViewInteraction checkingForPriceChangesString() {
		return onView(withText(CHECKING_PRICE_CHANGES_VIEW_ID));
	}

	public static ViewInteraction durationTextView() {
		return onView(withId(LEFT_TEXT_VIEW_ID));
	}

	public static ViewInteraction rightHeaderView() {
		return onView(withId(RIGHT_TEXT_VIEW_ID));
	}

	public static ViewInteraction airlineTextView() {
		return onView(withId(AIRLINE_TEXT_VIEW_ID));
	}

	public static ViewInteraction departureTimeTextView() {
		return onView(withId(DEPARTURE_TIME_TEXT_VIEW_ID));
	}

	public static ViewInteraction arrivalTimeTextView() {
		return onView(withId(ARRIVAL_TIME_TEXT_VIEW_ID));
	}

	public static ViewInteraction priceTextView() {
		return onView(withId(PRICE_TEXT_VIEW_ID));
	}

	public static ViewInteraction baggageFees() {
		return onView(withText(BAGGAGE_FEES_STRING_ID));
	}

	public static ViewInteraction detailsTextView() {
		return (onView(withId(DETAILS_TEXT_VIEW_ID)));
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
