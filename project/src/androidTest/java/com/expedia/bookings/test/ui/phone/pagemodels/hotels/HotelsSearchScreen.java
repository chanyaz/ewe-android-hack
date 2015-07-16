package com.expedia.bookings.test.ui.phone.pagemodels.hotels;

import org.joda.time.LocalDate;

import android.app.Activity;
import android.support.test.espresso.DataInteraction;
import android.support.test.espresso.ViewInteraction;

import com.expedia.bookings.R;
import com.expedia.bookings.test.ui.espresso.ViewActions;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.ui.espresso.CustomMatchers.withHotelName;
import static com.expedia.bookings.test.ui.espresso.ViewActions.clickDates;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.not;

public class HotelsSearchScreen {
	// Top action bar
	private static final int SEARCH_EDIT_TEXT_ID = R.id.search_edit_text;
	private static final int CLEAR_SEARCH_EDIT_TEXT_ID = R.id.clear_search_button;
	private static final int GUESTS_BUTTON_ID = R.id.guests_button;
	private static final int CALENDAR_BUTTON_ID = R.id.dates_button;
	private static final int CALENDAR_TEXT_VIEW_ID = R.id.dates_text_view;
	private static final int CALENDAR_DATE_PICKER_ID = R.id.dates_date_picker;

	// List header
	private static final int DATE_RANGE_TEXT_VIEW_ID = R.id.search_date_range_text;
	private static final int PRICING_DESCRIPTION_TEXT_VIEW_ID = R.id.lawyer_label_text_view;

	// Hotel list
	private static final int HOTEL_LIST_ID = android.R.id.list;

	// Bottom action bar
	private static final int SORT_BUTTON_ID = R.id.menu_select_sort;
	private static final int FILTER_BUTTON_ID = R.id.menu_select_filter;

	// Fragments
	private static HotelsSortMenu mSortMenu = new HotelsSortMenu();
	private static HotelsFilterMenu mFilterMenu = new HotelsFilterMenu();
	private static HotelsGuestPicker mGuestPicker = new HotelsGuestPicker();

	// Menu access

	public static HotelsSortMenu sortMenu() {
		return mSortMenu;
	}

	public static HotelsFilterMenu filterMenu() {
		return mFilterMenu;
	}

	public static HotelsGuestPicker guestPicker() {
		return mGuestPicker;
	}
	// Object access

	public static ViewInteraction searchEditText() {
		return onView(withId(SEARCH_EDIT_TEXT_ID));
	}

	public static ViewInteraction clearSearchEditTextButton() {
		return onView(withId(CLEAR_SEARCH_EDIT_TEXT_ID));
	}

	public static ViewInteraction calendarDatePicker() {
		return onView(withId(CALENDAR_DATE_PICKER_ID));
	}

	public static ViewInteraction guestsButton() {
		return onView(withId(GUESTS_BUTTON_ID));
	}

	public static ViewInteraction calendarButton() {
		return onView(withId(CALENDAR_BUTTON_ID));
	}

	public static ViewInteraction calendarNumberTextView() {
		return onView(withId(CALENDAR_TEXT_VIEW_ID));
	}

	public static ViewInteraction dateRangeTextView() {
		return onView(withId(DATE_RANGE_TEXT_VIEW_ID));
	}

	public static ViewInteraction pricingDescriptionTextView() {
		return onView(withId(PRICING_DESCRIPTION_TEXT_VIEW_ID));
	}

	public static ViewInteraction hotelResultsListView() {
		return onView(withId(HOTEL_LIST_ID));
	}

	public static ViewInteraction sortButton() {
		return onView(withId(SORT_BUTTON_ID));
	}

	public static ViewInteraction filterButton() {
		return onView(withId(FILTER_BUTTON_ID));
	}

	public static DataInteraction hotelListItem() {
		return onData(anything()).inAdapterView(withId(android.R.id.list));
	}

	// Object interaction

	public static void clickListItem(int index) {
		hotelListItem().atPosition(index).perform(click());
	}

	public static void enterSearchText(String text) {
		(searchEditText()).perform(typeText(text), closeSoftKeyboard());
	}

	public static void clickSearchEditText() {
		(searchEditText()).perform(click());
	}

	public static void clickToClearSearchEditText() {
		(clearSearchEditTextButton()).perform(click());
	}

	public static void clickOnGuestsButton() {
		(guestsButton()).perform(click());
	}

	public static void clickOnCalendarButton() {
		(calendarButton()).perform(click());
	}

	public static void clickOnSortButton() {
		(sortButton()).perform(click());
	}

	public static void clickOnFilterButton() {
		(filterButton()).perform(click());
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
}




