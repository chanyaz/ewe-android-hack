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

	private static int sHotelNameTextViewID = R.id.name_text_view;
	private static int sHotelLocationTextViewID = R.id.location_text_view;
	private static int sHotelRatingBarID = R.id.hotel_rating_bar;
	private static int sListViewID = android.R.id.list;
	private static int sBackButtonID = android.R.id.home;

	public HotelsRoomsRatesScreen(Instrumentation instrumentation, Activity activity, Resources res,
			TestPreferences preferences) {
		super(instrumentation, activity, res, preferences);
	}

	// Object access 

	public TextView hotelNameTextView() {
		return (TextView) getView(sHotelNameTextViewID);
	}

	public TextView hotelLocationTextView() {
		return (TextView) getView(sHotelLocationTextViewID);
	}

	public RatingBar hotelRatingBar() {
		return (RatingBar) getView(sHotelRatingBarID);
	}

	public ListView roomList() {
		return (ListView) getView(sListViewID);
	}

	public View backButton() {
		return getView(sBackButtonID);
	}

	// Object interaction

	public void clickBackButton() {
		clickOnView(backButton());
	}

	public void selectRoom(int index) {

		clickOnView(roomList().getChildAt(index));
	}

}
