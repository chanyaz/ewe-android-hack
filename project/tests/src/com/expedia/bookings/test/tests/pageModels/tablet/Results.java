package com.expedia.bookings.test.tests.pageModels.tablet;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Property;
import com.google.android.apps.common.testing.ui.espresso.DataInteraction;
import com.google.android.apps.common.testing.ui.espresso.ViewInteraction;
import com.google.android.apps.common.testing.ui.espresso.matcher.BoundedMatcher;
import com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.joda.time.LocalDate;

import static com.expedia.bookings.test.utils.EspressoUtils.swipeUp;
import static com.expedia.bookings.test.utils.ViewActions.clickDates;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onData;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.scrollTo;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withContentDescription;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

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

	public static void swipeUpHotelList() {
		hotelList().perform(swipeUp());
	}

	public static ViewInteraction sortAndFilterButton() {
		return onView(allOf(withText(R.string.Sort_and_Filter), isCompletelyDisplayed()));
	}

	public static void swipeUpFlightList() {
		flightList().perform(swipeUp());
	}

	public static ViewInteraction actionUpButton() {
		return onView(withId(android.R.id.home));
	}

	public static void clickDate(final LocalDate start, final LocalDate end) {
		calendarPicker().perform(clickDates(start, end));
	}

	public static void clickSearchNow() {
		onView(withId(R.id.search_now_btn)).perform(click());
	}

	public static void clickAddHotel() {
		onView(allOf(withId(R.id.room_rate_button_add), withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE))).perform(scrollTo(), click());
	}

	public static void clickBookHotel() {
		onView(allOf(withId(R.id.book_button_text), withText("Book Hotel"))).perform(click());
	}

	public static void clickAddFlight() {
		onView(withId(R.id.details_add_trip_button)).perform(click());
	}

	public static void clickBookFlight() {
		onView(allOf(withId(R.id.book_button_text), withText("Book Flight"))).perform(click());
	}

	public static void clickFlightAtIndex(int index) {
		onData(anything()).inAdapterView(withContentDescription("Flight Search Results")).atPosition(index).perform(click());
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
