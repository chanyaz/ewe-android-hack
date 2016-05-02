package com.expedia.bookings.test.phone.packages;

import org.hamcrest.Matchers;
import org.joda.time.LocalDate;

import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.matcher.ViewMatchers;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.TabletViewActions;
import com.expedia.bookings.test.espresso.ViewActions;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;

public class RailScreen {

	public static ViewInteraction calendarButton() {
		return onView(withId(R.id.calendar_card));
	}

	public static ViewInteraction calendar() {
		return onView(withId(R.id.calendar));
	}

	public static void selectDates(LocalDate start, LocalDate end) {
		calendar().perform(TabletViewActions.clickDates(start, end));
	}

	public static ViewInteraction searchButton() {
		onView(withId(R.id.search_button_v2)).perform(ViewActions.waitForViewToDisplay());
		return onView(withId(R.id.search_button_v2));
	}

	public static ViewInteraction dialogDoneButton() {
		return onView(withId(android.R.id.button1));
	}

	public static void scrollToFareOptions() {
		onView(allOf(withId(R.id.details_fare_options))).perform(scrollTo());
	}

	public static ViewInteraction selectFareOption(String fareOption) {
		return onView(
			Matchers.allOf(
				withId(R.id.select_button), Matchers.allOf(withText(R.string.select)),
				isDescendantOfA(Matchers.allOf(withId(R.id.details_fare_options))),
				hasSibling(Matchers.allOf(withId(R.id.price), withText(fareOption))),
				withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE))
		);
	}

	public static void clickSelectFareOption() {
		selectFareOption("£ 54.30").perform(scrollTo(), click());
	}
}
