package com.expedia.bookings.test.utils;

import java.lang.reflect.Field;

import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.widget.AlwaysFilterAutoCompleteTextView;
import com.jayway.android.robotium.solo.Solo;
import com.mobiata.android.text.format.Time;
import com.mobiata.android.widget.CalendarDatePicker;
import com.mobiata.testutils.CalendarTouchUtils;
import com.mobiata.testutils.RobotiumWorkflowUtils;

public class HotelsTestingUtils {
	
	public static final String TAG = "HotelsTestingUtils";
	private static final int CITY_SUGGESTION_MAX_WAIT = 10;
	private static final int SEARCH_RESULTS_MAX_WAIT = 10;
	private static final int ROOMS_AND_RATES_MAX_WAIT = 10;
	private static final int VIEW_IS_VISIBLE = 0;


	public static void waitForElement(Solo solo, int rfileid, String logDescription ) {
		View v = solo.getView(rfileid);
		int visibility = -1;
		for (int i=0; i < 15; i++ ){
			visibility = v.getVisibility();
			if (visibility == VIEW_IS_VISIBLE) {
				Log.d(TAG, logDescription + " is visible, sleeping");
				solo.sleep(1000);
			} else {
				Log.d(TAG, logDescription + " is gone, moving on");
				break;
			}
		}
	}
	
	public static void waitForSearchResultsSpinner(Solo solo) {
		waitForElement(solo, R.id.search_progress_bar, "search progress spinner");
	}
	
	public static void waitForProgressBar(Solo solo) {
		waitForElement(solo, R.id.progress_bar, "progress bar");
	}

	
	public static void waitForListViewHelper(Solo solo, int listViewid) {
		Field[] fa = R.id.class.getFields(); 
		RobotiumWorkflowUtils.waitForListViewToPopulate(solo, fa);
	}
	
	public static String getHotelName(Solo solo) {
		return RobotiumWorkflowUtils.getTextViewValue(solo, R.id.class.getFields(), R.id.name_text_view);
	}

	public static String getHotelFirstRoomPrice(Solo solo) {
		return RobotiumWorkflowUtils.getTextViewValue(solo, R.id.class.getFields(), R.id.price_text_view);
	}

	public static void selectCity(Solo solo, String city) {
		Log.d(TAG, "selectCity()");
		RobotiumWorkflowUtils.enterText(solo, R.id.search_edit_text, city);
		solo.clickOnView(solo.getView(R.id.search_edit_text));
		
		AlwaysFilterAutoCompleteTextView mAlwaysFilterAutoCompleteTextView = 
				(AlwaysFilterAutoCompleteTextView) solo.getView(R.id.search_edit_text);

		for (int i=0; i < CITY_SUGGESTION_MAX_WAIT; i++) {
			Log.d(TAG, "city suggestion listadapter getCount(): " + mAlwaysFilterAutoCompleteTextView.getAdapter().getCount());
			if ( mAlwaysFilterAutoCompleteTextView.getAdapter().getCount() > 0 ) {
				break;
			} else {
				solo.sleep(1000);
			}
		}
		solo.clickOnText(city);
	}

	public static void setCalendar(Solo solo, Time reservationDate) {
		Log.d(TAG, "setCalendar()");
		solo.clickOnView(solo.getView(R.id.dates_button));
		CalendarDatePicker cal = (CalendarDatePicker) solo.getView(R.id.dates_date_picker);
		CalendarTouchUtils.clickOnFutureMonthDay(solo, cal, reservationDate);
		//search button
		solo.clickOnButton(0);
	}

	public static void selectHotel(Solo solo) {
		Log.d(TAG, "selectHotel()");
		solo.clickInList(2);
		solo.clickOnView(solo.getView(R.id.menu_select_hotel));
	}

	public static void selectRoom(Solo solo) {
		Log.d(TAG, "selectRoom()");
		solo.clickInList(0);
	}

	public static void enterBookingInfo(Solo solo) {
		Log.d(TAG, "enterBookingInfo()");

		// TODO: This needs to be rewritten now that tablet uses the same
		// checkout as phone
	}

	public static void completeBooking(Solo solo, String environment) throws Exception {
		Log.d(TAG, "completeBooking()");
		Log.d(TAG, "environment: " + environment);

		// Warning: CURRENTLY DOES NOT WORK, AS THIS IS NO LONGER A BUTTON
		// solo.clickOnView(solo.getView(R.id.menu_book_now));
		
		// Warning: this may not work due to us changing the hotel confirmation page
		if ( ! environment.equals("Production")) {
			RobotiumWorkflowUtils.waitForElement(solo, R.id.menu_done, 60);
		}
	}

	public static void leaveConfirmationPage(Solo solo, String environment) {
		Log.d(TAG, "leaveConfirmationPage()");
		if (environment != "Production") {
			// Warning: this may not work due to us changing the hotel confirmation page
			solo.clickOnView(solo.getView(R.id.menu_done));
		}
		RobotiumWorkflowUtils.waitForElement(solo, R.id.search_edit_text, 60);
	}

	
}
