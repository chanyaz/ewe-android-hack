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
import com.mobiata.testutils.CalendarTouchUtils;

public class HotelsSearchScreen extends ScreenActions {

	// Top action bar
	private static final int sSearchEditTextID = R.id.search_edit_text;
	private static final int sClearSearchEditTextButtonID = R.id.clear_search_button;
	private static final int sGuestsButtonID = R.id.guests_button;
	private static final int sGuestNumberTextViewID = R.id.guests_text_view;
	private static final int sCalendarButtonID = R.id.dates_button;
	private static final int sCalendarTextViewID = R.id.dates_text_view;
	private static final int sCalendarDatePickerID = R.id.dates_date_picker;

	// List header
	private static final int sDateRangeTextViewID = R.id.search_date_range_text;
	private static final int sPricingDescriptionTextViewID = R.id.lawyer_label_text_view;

	// Hotel list
	private static final int sHotelListID = android.R.id.list;

	// Bottom action bar
	private static final int sSortButtonID = R.id.menu_select_sort;
	private static final int sFilterButtonID = R.id.menu_select_filter;
	private static final int sMapButtonID = R.id.menu_select_change_view;

	// Strings

	private static final int sSearchingForHotelsStringID = R.string.search_for_hotels;

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
		return (EditText) getView(sSearchEditTextID);
	}

	public View clearSearchEditTextButton() {
		return getView(sClearSearchEditTextButtonID);
	}

	public View guestsButton() {
		return getView(sGuestsButtonID);
	}

	public TextView guestNumberTextView() {
		return (TextView) getView(sGuestNumberTextViewID);
	}

	public View calendarButton() {
		return getView(sCalendarButtonID);
	}

	public TextView calendarNumberTextView() {
		return (TextView) getView(sCalendarTextViewID);
	}

	public TextView dateRangeTextView() {
		return (TextView) getView(sDateRangeTextViewID);
	}

	public TextView pricingDescriptionTextView() {
		return (TextView) getView(sPricingDescriptionTextViewID);
	}

	public ListView hotelResultsListView() {
		return (ListView) getView(sHotelListID);
	}

	public View sortButton() {
		return getView(sSortButtonID);
	}

	public View filterButton() {
		return getView(sFilterButtonID);
	}

	public View mapButton() {
		return getView(sMapButtonID);
	}

	public String searchingForHotels() {
		return getString(sSearchingForHotelsStringID);
	}

	// Object interaction

	public void enterSearchText(String text) {
		enterText(searchEditText(), text);
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
		CalendarTouchUtils.selectDay(this, offset, sCalendarDatePickerID);
	}
}
