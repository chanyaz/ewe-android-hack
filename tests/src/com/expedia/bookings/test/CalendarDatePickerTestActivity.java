package com.expedia.bookings.test;

import android.app.Activity;
import android.os.Bundle;
import com.expedia.bookings.R;

/**
 * This class is used solely for "unit" testing the calendar. Hosts the CalendarDatePicker widget standalone
 */
public class CalendarDatePickerTestActivity extends Activity {

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.fragment_dialog_calendar);
	}

}