package com.expedia.bookings.test.phone.flights;

import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.contrib.RecyclerViewActions;

import com.expedia.bookings.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static com.expedia.bookings.test.espresso.ViewActions.waitForViewToDisplay;
import static org.hamcrest.CoreMatchers.allOf;

public class FlightsScreen {

	public static ViewInteraction outboundFlightList() {
		return onView(allOf(withId(R.id.list_view), isDescendantOfA(withId(R.id.widget_flight_outbound))));
	}

	public static ViewInteraction inboundFlightList() {
		return onView(allOf(withId(R.id.list_view), isDescendantOfA(withId(R.id.widget_flight_inbound))));
	}

	public static ViewInteraction selectFlight(ViewInteraction list, int index) {
		list.perform(waitForViewToDisplay());
		int adjustPosition = 2;
		return list.perform(RecyclerViewActions.actionOnItemAtPosition(index + adjustPosition, click()));
	}

	public static ViewInteraction selectOutboundFlight() {
		return onView(allOf(withId(R.id.select_flight_button), isDescendantOfA(withId(R.id.widget_flight_outbound))));
	}

	public static ViewInteraction selectInboundFlight() {
		return onView(allOf(withId(R.id.select_flight_button), isDescendantOfA(withId(R.id.widget_flight_inbound))));
	}
}
