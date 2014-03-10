package com.expedia.bookings.test.tests.pageModels.hotels;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.res.Resources;
import android.view.View;
import android.widget.ImageView;
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
	private static final int HOTEL_IMAGEVIEW_ID = R.id.thumbnail_image_view;

	private static final int RENOVATION_NOTICE_STRING_ID = R.string.renovation_notice;
	private static final int RENOVATION_NOTICE_DESC_STRING_ID = R.string.property_undergoing_renovations;
	private static final int RENOVATION_INFO_IMAGE_ID = R.id.construction_chevron;

	private static final int ADDITIONAL_FEES_STRING_ID = R.string.additional_fees;
	private static final int FEES_NOT_INCLUDED_STRING_ID = R.string.fee_not_included_in_total;
	private static final int ADDITIONAL_FEES_INFO_IMAGE_ID = R.id.resort_fees_chevron;

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

	public ImageView thumbnailImageView() {
		return (ImageView) getView(HOTEL_IMAGEVIEW_ID);
	}

	public String renovationNoticeString() {
		return getString(RENOVATION_NOTICE_STRING_ID);
	}

	public String propertyRenovationString() {
		return getString(RENOVATION_NOTICE_DESC_STRING_ID);
	}

	public ImageView renovationInfoButton() {
		return (ImageView) getView(RENOVATION_INFO_IMAGE_ID);
	}

	public String additionalFeesString() {
		return getString(ADDITIONAL_FEES_STRING_ID);
	}

	public String feesNotIncludedString() {
		return getString(FEES_NOT_INCLUDED_STRING_ID);
	}

	public ImageView additionalFeesInfoButton() {
		return (ImageView) getView(ADDITIONAL_FEES_INFO_IMAGE_ID);
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

	public void clickRenovationInfoButton() {
		clickOnView(renovationInfoButton());
	}

	public void clickAdditionalFeesInfoButton() {
		clickOnView(additionalFeesInfoButton());
	}

}
