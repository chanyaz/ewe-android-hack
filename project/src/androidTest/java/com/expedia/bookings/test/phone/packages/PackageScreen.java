package com.expedia.bookings.test.phone.packages;

import java.util.concurrent.TimeUnit;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.joda.time.LocalDate;

import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.SpoonScreenshotUtils;
import com.expedia.bookings.test.espresso.TabletViewActions;
import com.expedia.bookings.test.espresso.ViewActions;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;

public class PackageScreen {

	public static ViewInteraction calendar() {
		return onView(withId(R.id.calendar));
	}

	public static ViewInteraction selectDateButton() {
		return onView(withId(R.id.select_date));
	}

	public static ViewInteraction destination() {
		return onView(allOf(isDescendantOfA(withId(R.id.flying_from)), withId(R.id.location_edit_text)));
	}

	public static ViewInteraction arrival() {
		return onView(allOf(isDescendantOfA(withId(R.id.flying_to)), withId(R.id.location_edit_text)));
	}

	public static ViewInteraction suggestionList() {
		return onView(withId(R.id.drop_down_list)).inRoot(withDecorView(
			not(Matchers.is(SpoonScreenshotUtils.getCurrentActivity(
			).getWindow().getDecorView()))));
	}

	public static void selectDates(LocalDate start, LocalDate end) {
		calendar().perform(TabletViewActions.clickDates(start, end));
	}

	public static void selectLocation(String hotel) throws Throwable {
		suggestionList().perform(ViewActions.waitForViewToDisplay());
		final Matcher<View> viewMatcher = hasDescendant(withText(hotel));

		suggestionList().perform(ViewActions.waitFor(viewMatcher, 10, TimeUnit.SECONDS));
		suggestionList().perform(RecyclerViewActions.actionOnItem(viewMatcher, click()));
	}

	public static ViewInteraction searchButton() {
		onView(withId(R.id.search_container)).perform(ViewActions.waitForViewToDisplay());
		return onView(allOf(withId(R.id.search_btn), isDescendantOfA(hasSibling(withId(R.id.search_container)))));
	}

	public static ViewInteraction errorDialog(String text) {
		return onView(withText(text))
			.inRoot(withDecorView(not(is(SpoonScreenshotUtils.getCurrentActivity().getWindow().getDecorView()))));
	}
}
