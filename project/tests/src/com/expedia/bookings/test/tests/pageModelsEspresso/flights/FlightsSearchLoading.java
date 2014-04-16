package com.expedia.bookings.test.tests.pageModelsEspresso.flights;

import com.expedia.bookings.R;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.ScreenActions;
import com.google.android.apps.common.testing.ui.espresso.ViewInteraction;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;

/**
 * Created by dmadan on 4/7/14.
 */
public class FlightsSearchLoading extends ScreenActions {
	private static final int sLoadingFlightsStringID = R.string.loading_flights;
	private static final int sLoadingDialogueViewID = R.id.message_text_view;

	public static ViewInteraction getLoadingFlightsString() {
		return onView(withText(sLoadingFlightsStringID));
	}

	public static ViewInteraction getLoadingDialogueView() {
		return onView(withId(sLoadingDialogueViewID));
	}

}
