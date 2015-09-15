package com.expedia.bookings.test.phone.pagemodels.hotels;

import org.joda.time.LocalDate;

import android.app.Activity;
import android.support.test.espresso.DataInteraction;
import android.support.test.espresso.ViewInteraction;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.ViewActions;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.espresso.CustomMatchers.withHotelName;
import static com.expedia.bookings.test.espresso.ViewActions.clickDates;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.not;

public class HotelsSearchScreen {

	private static HotelsFilterMenu mFilterMenu = new HotelsFilterMenu();
	private static HotelsGuestPicker mGuestPicker = new HotelsGuestPicker();

	// Object access

	public static ViewInteraction searchEditText() {
		return onView(withId(R.id.search_edit_text));
	}

	private static ViewInteraction calendarDatePicker() {
		return onView(withId(R.id.dates_date_picker));
	}

	public static ViewInteraction calendarNumberTextView() {
		return onView(withId(R.id.dates_text_view));
	}

	public static ViewInteraction dateRangeTextView() {
		return onView(withId(R.id.search_date_range_text));
	}

	public static ViewInteraction pricingDescriptionTextView() {
		return onView(withId(R.id.lawyer_label_text_view));
	}

	public static ViewInteraction hotelResultsListView() {
		return onView(withId(android.R.id.list));
	}

	public static DataInteraction hotelListItem() {
		return onData(anything()).inAdapterView(withId(android.R.id.list));
	}

	// Object interaction

	public static void clickListItem(int index) {
		hotelListItem().atPosition(index).perform(click());
	}

	public static void enterSearchText(String text) {
		searchEditText().perform(typeText(text), closeSoftKeyboard());
	}

	public static void clickSearchEditText() {
		searchEditText().perform(click());
	}

	public static void clickToClearSearchEditText() {
		onView(withId(R.id.clear_search_button)).perform(click());
	}

	public static void clickOnGuestsButton() {
		onView(withId(R.id.guests_button)).perform(click());
	}

	public static void clickOnCalendarButton() {
		onView(withId(R.id.dates_button)).perform(click());
	}

	public static void clickOnSortButton() {
		onView(withId(R.id.menu_select_sort)).perform(click());
	}

	public static void clickOnFilterButton() {
		onView(withId(R.id.menu_select_filter)).perform(click());
	}

	public static void clickDate(final LocalDate start, final LocalDate end) {
		calendarDatePicker().perform(clickDates(start, end));
	}

	public static void clickDate(final LocalDate start) {
		calendarDatePicker().perform(ViewActions.clickDate(start));
	}

	public static void clickSuggestionAtIndex(Activity activity, int index) {
		onData(anything()).inRoot(withDecorView(not(is(activity.getWindow().getDecorView())))).atPosition(
			index).perform(click());
	}

	public static void clickSuggestionWithName(Activity activity, String city) {
		onView(withText(city)).inRoot(withDecorView(not(is(activity.getWindow().getDecorView())))).perform(click());
	}

	public static void clickHotelWithName(String hotelName) {
		onData(withHotelName(hotelName)).inAdapterView(withId(android.R.id.list)).perform(click());
	}

	public static void clickSearchButton() {
		onView(withId(R.id.search_button)).perform(click());
	}


	// ActionBar menu items

	public static HotelsFilterMenu filterMenu() {
		return mFilterMenu;
	}

	public static HotelsGuestPicker guestPicker() {
		return mGuestPicker;
	}

	public static void clickSortByPopularity() {
		onView(withText(R.string.sort_description_popular)).perform(click());
	}

	public static void clickSortByPrice() {
		onView(withText(R.string.sort_description_price)).perform(click());
	}

	public static void clickSortByUserRating() {
		onView(withText(R.string.sort_description_rating)).perform(click());
	}

	public static void clickSortByDistance() {
		onView(withText(R.string.sort_description_distance)).perform(click());
	}
}




