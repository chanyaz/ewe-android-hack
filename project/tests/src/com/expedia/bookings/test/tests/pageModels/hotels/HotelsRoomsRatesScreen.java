package com.expedia.bookings.test.tests.pageModels.hotels;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.res.Resources;
import android.view.View;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.test.tests.pageModels.common.ScreenActions;
import com.expedia.bookings.test.utils.TestPreferences;

public class HotelsRoomsRatesScreen extends ScreenActions {

	private static final int HOTEL_NAME_TEXT_VIEW_ID = R.id.name_text_view;
	private static final int HOTEL_LOCATION_TEXT_VIEW_ID = R.id.location_text_view;
	private static final int HOTEL_RATING_BAR_ID = R.id.hotel_rating_bar;
	private static final int LIST_VIEW_ID = android.R.id.list;
	private static final int BACK_BUTTON_ID = android.R.id.home;
	private static final int FINDING_AVAILABLE_ROOMS_STRING_ID = R.string.room_rates_loading;
	private static final int SELECT_A_ROOM_STRING_ID = R.string.select_a_room_instruction;

	public HotelsRoomsRatesScreen(Instrumentation instrumentation, Activity activity, Resources res,
			TestPreferences preferences) {
		super(instrumentation, activity, res, preferences);
	}

	// Object access 

	public TextView hotelNameTextView() {
		return (TextView) getView(HOTEL_NAME_TEXT_VIEW_ID);
	}

	public TextView hotelLocationTextView() {
		return (TextView) getView(HOTEL_LOCATION_TEXT_VIEW_ID);
	}

	public RatingBar hotelRatingBar() {
		return (RatingBar) getView(HOTEL_RATING_BAR_ID);
	}

	public ListView roomList() {
		return (ListView) getView(LIST_VIEW_ID);
	}

	public View backButton() {
		return getView(BACK_BUTTON_ID);
	}

	public String findingAvailableRooms() {
		return getString(FINDING_AVAILABLE_ROOMS_STRING_ID);
	}

	public String selectARoom() {
		return getString(SELECT_A_ROOM_STRING_ID);
	}

	// Object interaction

	public void clickBackButton() {
		clickOnView(backButton());
	}

	public void selectRoom(int index) {
		clickOnView(roomList().getChildAt(index));
	}

	public RoomsAndRatesRow getRowModelAtIndex(int index) {
		View row = roomList().getChildAt(index);
		return new RoomsAndRatesRow(row);
	}

}
