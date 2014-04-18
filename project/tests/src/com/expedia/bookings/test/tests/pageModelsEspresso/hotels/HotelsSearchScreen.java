package com.expedia.bookings.test.tests.pageModelsEspresso.hotels;

import org.joda.time.LocalDate;

import static com.expedia.bookings.test.utilsEspresso.ViewActions.clickDates;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.matcher.RootMatchers.withDecorView;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.closeSoftKeyboard;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.typeText;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.SearchActivity;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.ScreenActions;
import com.expedia.bookings.test.utils.HotelsUserData;
import com.google.android.apps.common.testing.ui.espresso.ViewInteraction;

/**
 * Created by dmadan on 4/10/14.
 */
public class HotelsSearchScreen extends ScreenActions {
	// Top action bar
	private static final int SEARCH_EDIT_TEXT_ID = R.id.search_edit_text;
	private static final int CLEAR_SEARCH_EDIT_TEXT_ID = R.id.clear_search_button;
	private static final int GUESTS_BUTTON_ID = R.id.guests_button;
	private static final int GUEST_NUMBER_TEXTVIEW_ID = R.id.guests_text_view;
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
	private static final int MAP_BUTTON_ID = R.id.menu_select_change_view;

	// Strings
	private static final int ROOM_NO_LONGER_AVAILABLE_STRING_ID = R.string.e3_error_checkout_hotel_room_unavailable;
	private static final int SEARCHING_FOR_HOTELS_STRING_ID = R.string.progress_searching_hotels;
	private static final int PLEASE_TRY_DIFFERENT_STRING_ID = R.string.please_try_a_different_location_or_date;
	private static final int NO_HOTELS_AVAILABLE_TONIGHT_ID = R.string.no_hotels_availiable_tonight;
	private static final int DID_YOU_MEAN_STRING_ID = R.string.ChooseLocation;
	private static final int UNABLE_TO_DETERMINE_SEARCH_LOC_STRING_ID = R.string.geolocation_failed;

	// Fragments
	private static HotelsSortMenu mSortMenu;
	private static HotelsFilterMenu mFilterMenu;
	private static HotelsGuestPicker mGuestPicker;

	// Menu access

	public static HotelsSortMenu sortMenu() {
		if (mSortMenu == null) {
			mSortMenu = new HotelsSortMenu();
		}
		return mSortMenu;
	}

	public static HotelsFilterMenu filterMenu() {
		if (mFilterMenu == null) {
			mFilterMenu = new HotelsFilterMenu();
		}
		return mFilterMenu;
	}

	public static HotelsGuestPicker guestPicker() {
		if (mGuestPicker == null) {
			mGuestPicker = new HotelsGuestPicker();
		}
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

	public static ViewInteraction guestNumberTextView() {
		return onView(withId(GUEST_NUMBER_TEXTVIEW_ID));
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

	public static ViewInteraction mapButton() {
		return onView(withId(MAP_BUTTON_ID));
	}

	public static ViewInteraction searchingForHotels() {
		return onView(withText(SEARCHING_FOR_HOTELS_STRING_ID));
	}

	public static ViewInteraction roomNoLongerAvailable() {
		return onView(withText(ROOM_NO_LONGER_AVAILABLE_STRING_ID));
	}

	public static ViewInteraction noHotelsAvailableTonight() {
		return onView(withText(NO_HOTELS_AVAILABLE_TONIGHT_ID));
	}

	public static ViewInteraction pleaseTryADifferentLocationOrDate() {
		return onView(withText(PLEASE_TRY_DIFFERENT_STRING_ID));
	}

	public static ViewInteraction didYouMean() {
		return onView(withText(DID_YOU_MEAN_STRING_ID));
	}

	public static ViewInteraction unableToDetermineSearchLocation() {
		return onView(withText(UNABLE_TO_DETERMINE_SEARCH_LOC_STRING_ID));
	}

	// Object interaction

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

	public static void clickOnMapButton() {
		(mapButton()).perform(click());
	}

	public static void clickDate(final LocalDate start, final LocalDate end) {
		calendarDatePicker().perform(clickDates(start, end));
	}

	public static void selectHotelFromList() {
		hotelResultsListView().perform(click());
	}

	public static void clickSuggestion(SearchActivity activity, HotelsUserData user) {
		onView(withText(user.getHotelSearchCity())).inRoot(withDecorView(not(is(activity.getWindow().getDecorView())))).perform(click());
	}
}




