package com.expedia.bookings.test.phone.newhotels;

import org.joda.time.LocalDate;

import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.espresso.matcher.ViewMatchers;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.SpoonScreenshotUtils;
import com.expedia.bookings.test.espresso.TabletViewActions;
import com.expedia.bookings.test.ui.phone.pagemodels.common.ScreenActions;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

public class HotelScreen {

	public static ViewInteraction calendar() {
		return onView(withId(R.id.calendar));
	}

	public static ViewInteraction selectDateButton() {
		return onView(withId(R.id.select_date));
	}

	public static ViewInteraction location() {
		return onView(withId(R.id.hotel_location));
	}

	public static void selectLocation(String hotel) throws Throwable {
		ScreenActions.delay(1);
		onView(withText(hotel))
			.inRoot(withDecorView(
				not(is(SpoonScreenshotUtils.getCurrentActivity(
				).getWindow().getDecorView()))))
			.perform(click());
	}

	public static void selectDates(LocalDate start, LocalDate end) {
		calendar().perform(TabletViewActions.clickDates(start, end));
	}

	public static ViewInteraction searchButton() {
		return onView(allOf(withId(R.id.search_btn), isDescendantOfA(hasSibling(withId(R.id.search_container)))));
	}

	public static ViewInteraction hotelResultsList() {
		return onView(withId(R.id.list_view));
	}

	public static void selectHotel(int position) {
		hotelResultsList().perform(RecyclerViewActions.actionOnItemAtPosition(position, click()));
	}

	public static ViewInteraction addRoom() {
		return onView(
			allOf(
				withId(R.id.view_room_button), allOf(withText("Book")),
				isDescendantOfA(allOf(withId(R.id.collapsed_container))),
				withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE))
		);
	}

	public static void clickAddRoom() {
		addRoom().perform(scrollTo(), click());
	}

}
