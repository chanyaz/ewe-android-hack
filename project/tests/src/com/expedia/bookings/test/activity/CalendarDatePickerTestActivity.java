package com.expedia.bookings.test.activity;

import android.app.Activity;
import android.os.Bundle;
import com.expedia.bookings.R;

/**
 * This class is used solely for "unit" testing the calendar. Hosts the CalendarDatePicker widget standalone
 */
public class CalendarDatePickerTestActivity extends Activity {

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.test_calendar_date_picker);
	}

}