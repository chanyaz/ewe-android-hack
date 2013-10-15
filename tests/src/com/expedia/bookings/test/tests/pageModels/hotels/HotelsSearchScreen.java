package com.expedia.bookings.test.tests.pageModels.hotels;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.res.Resources;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.test.tests.pageModels.common.ScreenActions;
import com.expedia.bookings.test.utils.TestPreferences;
import com.mobiata.android.text.format.Time;
import com.mobiata.android.widget.CalendarDatePicker;
import com.expedia.bookings.test.utils.CalendarTouchUtils;

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
	private static final int SEARCHING_FOR_HOTELS_STRING_ID = R.string.search_for_hotels;
	private static final int PLEASE_TRY_DIFFERENT_STRING_ID = R.string.please_try_a_different_location_or_date;
	private static final int NO_HOTELS_AVAILABLE_TONIGHT_ID = R.string.no_hotels_availiable_tonight;
	private static final int DID_YOU_MEAN_STRING_ID = R.string.ChooseLocation;
	private static final int UNABLE_TO_DETERMINE_SEARCH_LOC_STRING_ID = R.string.geolocation_failed;

	// Fragments
	private HotelsSortMenu mSortMenu;
	private HotelsFilterMenu mFilterMenu;
	private HotelsGuestPicker mGuestPicker;

	public HotelsSearchScreen(Instrumentation instrumentation, Activity activity, Resources res,
			TestPreferences preferences) {
		super(instrumentation, activity, res, preferences);
	}

	// Menu access

	public HotelsSortMenu sortMenu() {
		if (mSortMenu == null) {
			mSortMenu = new HotelsSortMenu(mInstrumentation, getCurrentActivity(), mRes, mPreferences);
		}
		return mSortMenu;
	}

	public HotelsFilterMenu filterMenu() {
		if (mFilterMenu == null) {
			mFilterMenu = new HotelsFilterMenu(mInstrumentation, getCurrentActivity(), mRes, mPreferences);
		}
		return mFilterMenu;
	}

	public HotelsGuestPicker guestPicker() {
		if (mGuestPicker == null) {
			mGuestPicker = new HotelsGuestPicker(mInstrumentation, getCurrentActivity(), mRes, mPreferences);
		}
		return mGuestPicker;
	}

	// Object access

	public EditText searchEditText() {
		return (EditText) getView(SEARCH_EDIT_TEXT_ID);
	}

	public View clearSearchEditTextButton() {
		return getView(CLEAR_SEARCH_EDIT_TEXT_ID);
	}

	public View guestsButton() {
		return getView(GUESTS_BUTTON_ID);
	}

	public TextView guestNumberTextView() {
		return (TextView) getView(GUEST_NUMBER_TEXTVIEW_ID);
	}

	public View calendarButton() {
		return getView(CALENDAR_BUTTON_ID);
	}

	public TextView calendarNumberTextView() {
		return (TextView) getView(CALENDAR_TEXT_VIEW_ID);
	}

	public TextView dateRangeTextView() {
		return (TextView) getView(DATE_RANGE_TEXT_VIEW_ID);
	}

	public TextView pricingDescriptionTextView() {
		return (TextView) getView(PRICING_DESCRIPTION_TEXT_VIEW_ID);
	}

	public ListView hotelResultsListView() {
		return (ListView) getView(HOTEL_LIST_ID);
	}

	public View sortButton() {
		return getView(SORT_BUTTON_ID);
	}

	public View filterButton() {
		return getView(FILTER_BUTTON_ID);
	}

	public View mapButton() {
		return getView(MAP_BUTTON_ID);
	}

	public String searchingForHotels() {
		return getString(SEARCHING_FOR_HOTELS_STRING_ID);
	}

	public String roomNoLongerAvailable() {
		return getString(ROOM_NO_LONGER_AVAILABLE_STRING_ID);
	}

	public String noHotelsAvailableTonight() {
		return getString(NO_HOTELS_AVAILABLE_TONIGHT_ID);
	}

	public String pleaseTryADifferentLocationOrDate() {
		return getString(PLEASE_TRY_DIFFERENT_STRING_ID);
	}

	public String didYouMean() {
		return getString(DID_YOU_MEAN_STRING_ID);
	}

	public String unableToDetermineSearchLocation() {
		return getString(UNABLE_TO_DETERMINE_SEARCH_LOC_STRING_ID);
	}

	public HotelSearchResultRow getSearchResultRowModelFromIndex(int index) {
		return new HotelSearchResultRow(hotelResultsListView().getChildAt(index + 1));
	}

	public CalendarDatePicker calendarDatePicker() {
		return (CalendarDatePicker) getView(CALENDAR_DATE_PICKER_ID);
	}

	// Object interaction

	public void enterSearchText(String text) {
		typeText(searchEditText(), text);
	}

	public void clickSearchEditText() {
		clickOnView(searchEditText());
	}

	public void clickToClearSearchEditText() {
		clickOnView(clearSearchEditTextButton());
	}

	public void clickOnGuestsButton() {
		clickOnView(guestsButton());
	}

	public void clickOnCalendarButton() {
		clickOnView(calendarButton());
	}

	public void clickOnSortButton() {
		clickOnView(sortButton());
	}

	public void clickOnFilterButton() {
		clickOnView(filterButton());
	}

	public void clickOnMapButton() {
		clickOnView(mapButton());
	}

	public void selectHotelFromList(int index) {
		clickOnView(hotelResultsListView().getChildAt(index + 1));
	}

	public void clickDate(int offset) {
		delay();
		CalendarTouchUtils.selectDay(this, offset, CALENDAR_DATE_PICKER_ID);
	}

	public void clickDate(Time time) {
		delay();
		CalendarDatePicker cal = calendarDatePicker();
		CalendarTouchUtils.clickOnFutureMonthDay(this, cal, time);
	}

}
