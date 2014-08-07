package com.expedia.bookings.test.tests.pageModels.tablet;

import org.hamcrest.Matcher;

import android.view.View;

import com.expedia.bookings.R;
import com.google.android.apps.common.testing.ui.espresso.DataInteraction;
import com.google.android.apps.common.testing.ui.espresso.ViewInteraction;
import com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers;

import static com.expedia.bookings.test.espresso.ViewActions.slowSwipeUp;
import static com.expedia.bookings.test.utilsEspresso.CustomMatchers.listLengthGreaterThan;
import static com.expedia.bookings.test.utilsEspresso.CustomMatchers.withHotelName;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onData;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.scrollTo;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isDescendantOfA;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isDisplayed;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withContentDescription;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withParent;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;

public class Results {

	public static ViewInteraction hotelList() {
		return onView(withContentDescription("Hotel Search Results"));
	}

	public static ViewInteraction flightList() {
		return onView(flightListMatcher());
	}

	public static Matcher<View> flightListMatcher() {
		return allOf(withId(android.R.id.list), withListColumnContainer(), isDisplayed());
	}

	//to avoid matching multiple views on round-trip flight search results
	public static Matcher<View> withListColumnContainer() {
		return allOf(isDescendantOfA(allOf(withId(R.id.list_column_container), withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE))));
	}

	public static void swipeUpHotelList() {
		try {
			Thread.sleep(1500);
		}
		catch (Exception e) {
			// ignore
		}
		onView(withId(R.id.column_one_hotel_list)).perform(slowSwipeUp());
		// FIXME: OMG I HATE SLEEPS BUT WE NEED THIS FOR NOW
		try {
			Thread.sleep(1500);
		}
		catch (Exception e) {
			// ignore
		}
	}

	public static void swipeUpFlightList() {
		try {
			Thread.sleep(1500);
		}
		catch (Exception e) {
			// ignore
		}
		onView(withId(R.id.list_column_container)).perform(slowSwipeUp());
		try {
			Thread.sleep(1500);
		}
		catch (Exception e) {
			// ignore
		}
	}

	public static ViewInteraction actionUpButton() {
		return onView(withId(android.R.id.home));
	}

	public static void clickBookHotel() {
		onView(allOf(withId(R.id.book_button_text), withText("Book Hotel"))).perform(scrollTo(), click());
	}

	public static void clickAddFlight() {
		onView(allOf(withId(R.id.details_add_trip_button), isDisplayed())).perform(click());
	}

	public static void clickBookFlight() {
		onView(allOf(withId(R.id.book_button_text), withText("Book Flight"), isDisplayed())).perform(click());
	}

	public static void clickBookButton() {
		onView(allOf(withId(R.id.book_button_text), isDisplayed())).perform(click());
	}

	public static DataInteraction flightAtIndex(int index) {
		return onData(anything()) //
			.inAdapterView(flightListMatcher()) //
			.atPosition(index);
	}

	public static void clickFlightAtIndex(int index) {
		//list with one item has list count of 3,
		//so check for list length greater than 2 for non empty search result list
		onView(flightListMatcher()).check(matches(listLengthGreaterThan(2)));
		flightAtIndex(index).perform(click());

	}

	public static DataInteraction hotelAtIndex(int index) {
		return onData(anything()) //
			.inAdapterView(allOf(withId(android.R.id.list), withParent(withParent(withParent(withId(R.id.column_one_hotel_list)))), isDisplayed())) //
			.atPosition(index);
	}

	public static void clickHotelAtIndex(int index) {
		hotelAtIndex(index).perform(click());
	}

	public static void clickHotelWithName(String hotelName) {
		onData(withHotelName(hotelName)).inAdapterView(withContentDescription("Hotel Search Results")).perform(click());
	}
}
