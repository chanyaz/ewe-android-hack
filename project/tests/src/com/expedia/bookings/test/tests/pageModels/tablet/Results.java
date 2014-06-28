package com.expedia.bookings.test.tests.pageModels.tablet;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.joda.time.LocalDate;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.test.espresso.SuggestionAdapterViewProtocol;
import com.google.android.apps.common.testing.ui.espresso.DataInteraction;
import com.google.android.apps.common.testing.ui.espresso.ViewInteraction;
import com.google.android.apps.common.testing.ui.espresso.matcher.BoundedMatcher;
import com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers;

import static com.expedia.bookings.test.espresso.TabletViewActions.clickDates;
import static com.expedia.bookings.test.espresso.ViewActions.slowSwipeUp;
import static com.expedia.bookings.test.espresso.ViewActions.swipeUp;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onData;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.scrollTo;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.typeText;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isDisplayed;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withContentDescription;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withParent;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

public class Results {

	public static ViewInteraction hotelList() {
		return onView(withContentDescription("Hotel Search Results"));
	}

	public static ViewInteraction flightList() {
		return onView(withContentDescription("Flight Search Results"));
	}

	public static ViewInteraction calendarPicker() {
		return onView(withId(R.id.calendar_picker));
	}

	public static void clickTravelerButton() {
		onView(withId(R.id.traveler_btn)).perform(click());
	}

	public static void swipeUpHotelList() {
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

	public static void clickDate(final LocalDate start, final LocalDate end) {
		calendarPicker().perform(clickDates(start, end));
	}

	public static ViewInteraction originButton() {
		return onView(withId(R.id.origin_btn));
	}

	public static void clickOriginButton() {
		originButton().perform(click());
	}

	public static ViewInteraction originEditText() {
		return onView(withId(R.id.waypoint_edit_text));
	}

	public static void typeInOriginEditText(String text) {
		originEditText().perform(typeText(text));
	}

	public static void clickSuggestion(String text) {
		onData(allOf(is(instanceOf(String.class)), equalTo(text))) //
			.inAdapterView(allOf(withId(android.R.id.list), withParent(withParent(withId(R.id.suggestions_container))))) //
			.usingAdapterViewProtocol(SuggestionAdapterViewProtocol.getInstance()) //
			.perform(click());
	}

	public static void clickSelectFlightDates() {
		onView(withId(R.id.calendar_btn)).perform(click());
	}

	public static void clickSearchNow() {
		onView(withId(R.id.search_now_btn)).perform(click());
	}

	public static ViewInteraction addHotel() {
		return onView(allOf(withId(R.id.room_rate_button_add), withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
	}

	public static void clickAddHotel() {
		addHotel().perform(scrollTo(), click());
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

	public static DataInteraction flightAtIndex(int index) {
		return onData(anything()) //
			.inAdapterView(allOf(withContentDescription("Flight Search Results"), isDisplayed())) //
			.atPosition(index);
	}

	public static void clickFlightAtIndex(int index) {
		flightAtIndex(index).perform(click());
	}

	public static DataInteraction hotelAtIndex(int index) {
		return onData(anything()) //
			.inAdapterView(allOf(withContentDescription("Hotel Search Results"), isDisplayed())) //
			.atPosition(index);
	}

	public static void clickHotelAtIndex(int index) {
		hotelAtIndex(index).perform(click());
	}

	public static void clickHotelWithName(String hotelName) {
		onData(withHotelName(hotelName)).inAdapterView(withContentDescription("Hotel Search Results")).perform(click());
	}

	public static Matcher<Object> withHotelName(String expectedText) {
		checkNotNull(expectedText);
		return withHotelName(equalTo(expectedText));
	}

	public static Matcher<Object> withHotelName(final Matcher<String> textMatcher) {
		checkNotNull(textMatcher);
		return new BoundedMatcher<Object, Property>(Property.class) {
			@Override
			public boolean matchesSafely(Property property) {
				return textMatcher.matches(property.getName());
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("with item content: ");
				textMatcher.describeTo(description);
			}
		};
	}
}
