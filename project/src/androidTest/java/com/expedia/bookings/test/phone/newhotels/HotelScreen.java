package com.expedia.bookings.test.phone.newhotels;

import org.hamcrest.Matcher;
import org.joda.time.LocalDate;

import android.support.test.espresso.DataInteraction;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.espresso.matcher.ViewMatchers;
import android.view.View;
import android.widget.ListView;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.SpoonScreenshotUtils;
import com.expedia.bookings.test.espresso.TabletViewActions;
import com.expedia.bookings.test.espresso.ViewActions;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withChild;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
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
		return onView(withId(R.id.hotel_location_autocomplete));
	}

	public static ViewInteraction clearButton() {
		return onView(withId(R.id.clear_location_button));
	}

	public static ViewInteraction etpAndFreeCancellationMessagingContainer() {
		return onView(withId(R.id.etp_and_free_cancellation_messaging_container));
	}

	public static ViewInteraction etpInfoText() {
		return onView(withId(R.id.etp_info_text));
	}

	public static ViewInteraction freeCancellation() {
		return onView(withId(R.id.free_cancellation));
	}

	public static ViewInteraction horizontalDividerBwEtpAndFreeCancellation() {
		return onView(withId(R.id.horizontal_divider_bw_etp_and_free_cancellation));
	}

	public static ViewInteraction etpPlaceholder() {
		return onView(withId(R.id.etp_placeholder));
	}

	public static ViewInteraction payNowAndLaterOptions() {
		return onView(withId(R.id.radius_pay_options));
	}

	public static ViewInteraction roomsContainer() {
		return onView(withId(R.id.room_container));
	}

	public static ViewInteraction resortFeesText() {
		return onView(withId(R.id.resort_fees_text));
	}

	public static ViewInteraction ratingContainer() {
		return onView(withId(R.id.rating_container));
	}

	public static ViewInteraction amenityContainer() {
		return onView(withId(R.id.amenities_table_row));
	}
	public static ViewInteraction commonAmenitiesText() {
		return onView(withId(R.id.common_amenities_text));
	}

	public static ViewInteraction renovationContainer() {
		return onView(withId(R.id.renovation_container));
	}

	public static void selectLocation(String hotel) throws Throwable {
		Common.delay(1);
		onView(withText(hotel))
			.inRoot(withDecorView(
				not(is(SpoonScreenshotUtils.getCurrentActivity(
				).getWindow().getDecorView()))))
			.perform(click());
	}

	public static DataInteraction suggestionView() throws Throwable {
		return onData(anything())
			.inAdapterView(isAssignableFrom(ListView.class))
			.inRoot(withDecorView(not(is(SpoonScreenshotUtils.getCurrentActivity().getWindow().getDecorView()))));
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

	public static void selectHotelWithName(String name) {
		hotelResultsList().perform(RecyclerViewActions.actionOnItem(withChild(withChild((withText(name)))), click()));
	}

	public static ViewInteraction addRoom() {
		return onView(
			allOf(
				withId(R.id.view_room_button), allOf(withText("Book")),
				isDescendantOfA(allOf(withId(R.id.collapsed_container))),
				withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE))
		);
	}

	public static ViewInteraction viewRoom(String roomName) {
		return onView(
			allOf(
				withId(R.id.view_room_button), allOf(withText("View Room")),
				isDescendantOfA(allOf(withId(R.id.collapsed_container),
					withChild(allOf(withId(R.id.room_type_text_view), withText(roomName))))),
				withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE))
		);
	}

	public static void clickAddRoom() {
		addRoom().perform(scrollTo(), click());
	}

	public static void clickViewRoom(String roomName) {
		viewRoom(roomName).perform(scrollTo(), click());
	}

	public static void clickPayLater() {
		onView(withId(R.id.radius_pay_later)).perform(click());
	}

	public static void clickPayNow() {
		onView(withId(R.id.radius_pay_now)).perform(click());
	}

	public static void clickRatingContainer() {
		onView(withId(R.id.rating_container)).perform(click());
	}

	public static void waitForResultsDisplayed() {
		hotelResultsList().perform(ViewActions.waitForViewToDisplay());
	}

	public static ViewInteraction resultsListItemView(Matcher<View> identifyingMatcher) {
		return ViewActions.recyclerItemView(identifyingMatcher, R.id.list_view);
	}

	public static void showCalendar() {
		calendar().check(matches((isDisplayed())));
	}
}
