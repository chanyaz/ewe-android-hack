package com.expedia.bookings.test.utils;

import android.util.Log;

import com.expedia.bookings.R;
import com.jayway.android.robotium.solo.Solo;
import com.mobiata.android.text.format.Time;
import com.mobiata.android.widget.CalendarDatePicker;
import com.mobiata.testutils.CalendarTouchUtils;
import com.mobiata.testutils.RobotiumWorkflowUtils;

public class HotelsTestingUtils {
	
	public static final String TAG = "HotelsTestingUtils";
	
	public static String getHotelName(Solo solo) {
		return RobotiumWorkflowUtils.getTextViewValue(solo, R.id.class.getFields(), R.id.name_text_view);
	}

	public static String getHotelFirstRoomPrice(Solo solo) {
		return RobotiumWorkflowUtils.getTextViewValue(solo, R.id.class.getFields(), R.id.price_text_view);
	}

	public static void clickInCityList(Solo solo, int line) {
		Log.d(TAG, "clickInCityList: " + line);
		//		ListView lv = (ListView) solo.getView(R.id.search_suggestions_list_view);
		//		solo.clickOnView(lv.getChildAt(line));
		//solo.sleep(5000);
	}

	public static void selectCity(Solo solo, String city) {
		Log.d(TAG, "selectCity()");
		RobotiumWorkflowUtils.enterText(solo, R.id.search_edit_text, city);
		RobotiumWorkflowUtils.waitForListViewToPopulate(solo, R.id.class.getFields());
		//RobotiumWorkflowUtils.waitForListViewToPopulate(solo, R.id.search_suggestions_list_view);
		//clickInCityList(solo, 0);
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
		solo.clickInList(0);
		solo.clickOnView(solo.getView(R.id.menu_select_hotel));	}

	public static void selectRoom(Solo solo) {
		Log.d(TAG, "selectRoom()");
		solo.clickInList(0);
	}

	public static void enterBookingInfo(Solo solo) {
		Log.d(TAG, "enterBookingInfo()");

		//need some padding here; without it, initial views are null to getView()
		solo.sleep(2000);

		RobotiumWorkflowUtils.enterText(solo, R.id.first_name_edit_text, "Jimmy");
		RobotiumWorkflowUtils.enterText(solo, R.id.last_name_edit_text, "James");
		RobotiumWorkflowUtils.enterText(solo, R.id.telephone_edit_text, "4155551212");
		RobotiumWorkflowUtils.enterText(solo, R.id.email_edit_text, "numb@nuts.com");
		RobotiumWorkflowUtils.enterText(solo, R.id.card_number_edit_text, "4111111111111111");
		RobotiumWorkflowUtils.enterText(solo, R.id.expiration_month_edit_text, "11");
		RobotiumWorkflowUtils.enterText(solo, R.id.expiration_year_edit_text, "14");
		RobotiumWorkflowUtils.enterText(solo, R.id.address1_edit_text, "1 Rincon Hill");
		RobotiumWorkflowUtils.enterText(solo, R.id.address2_edit_text, "Apt 4709");
		RobotiumWorkflowUtils.enterText(solo, R.id.city_edit_text, "San Francisco");
		//RobotiumWorkflowUtils.enterText(solo, R.id.state_edit_text, "CA");
		RobotiumWorkflowUtils.enterText(solo, R.id.postal_code_edit_text, "94107");

		//will always need to enter security code, regardless of login state
		RobotiumWorkflowUtils.enterText(solo, R.id.security_code_edit_text, "123");

	}

	public static void completeBooking(Solo solo, String environment) throws Exception {
		Log.d(TAG, "completeBooking()");
		Log.d(TAG, "environment: " + environment);

		solo.clickOnView(solo.getView(R.id.menu_book_now));
		
		if ( ! environment.equals("Production")) {
			RobotiumWorkflowUtils.waitForElement(solo, R.id.menu_new_search, 60);
		}
	}

	public static void leaveConfirmationPage(Solo solo, String environment) {
		Log.d(TAG, "leaveConfirmationPage()");
		if (environment != "Production") {
			solo.clickOnView(solo.getView(R.id.menu_new_search));
		}
		RobotiumWorkflowUtils.waitForElement(solo, R.id.search_edit_text, 60);
	}

	
}
