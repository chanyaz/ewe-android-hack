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
		ViewInteraction searchButton = onView(allOf(withId(R.id.search_button), withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
		searchButton.perform(ViewActions.waitForViewToDisplay());
		return searchButton;
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
		selectFareOption("£203.0").perform(scrollTo(), click());
	}

	public static void clickAmenitiesLink(String fareClass) {
		selectAmenitiesLink(fareClass).perform(scrollTo(), click());
	}

	public static ViewInteraction selectAmenitiesLink(String fareClass) {
		return onView(
			Matchers.allOf(
				withId(R.id.amenities_link), Matchers.allOf(withText(R.string.amenities)),
				isDescendantOfA(Matchers.allOf(withId(R.id.details_fare_options))),
				hasSibling(Matchers.allOf(withId(R.id.fare_description), withText(fareClass))),
				withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE))
		);
	}

	public static ViewInteraction checkout() {
		return onView(withId(R.id.checkout_button));
	}

	public static ViewInteraction legInfo() {
		return onView(withId(R.id.rail_leg_container));
	}

	public static ViewInteraction detailsIcon() {
		return onView(Matchers.allOf(isDescendantOfA(withId(R.id.rail_leg_container)),
			withId(R.id.rail_leg_details_icon)));
	}

	public static ViewInteraction fareDesciptionInfo() {
		return onView(withId(R.id.fare_description_container));
	}
}
