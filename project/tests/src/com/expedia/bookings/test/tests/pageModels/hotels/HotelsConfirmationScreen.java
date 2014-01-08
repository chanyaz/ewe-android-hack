package com.expedia.bookings.test.tests.pageModels.hotels;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.res.Resources;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.test.tests.pageModels.common.ConfirmationScreen;
import com.expedia.bookings.test.utils.TestPreferences;

public class HotelsConfirmationScreen extends ConfirmationScreen {

	private static final int SUMMARY_TEXT_VIEW_ID = R.id.stay_summary_text_view;
	private static final int HOTEL_NAME_TEXT_VIEW_ID = R.id.hotel_name_text_view;

	public HotelsConfirmationScreen(Instrumentation instrumentation, Activity activity, Resources res,
			TestPreferences preferences) {
		super(instrumentation, activity, res, preferences);
	}

	// Object access

	public TextView summaryTextView() {
		return (TextView) getView(SUMMARY_TEXT_VIEW_ID);
	}

	public TextView hotelNameTextView() {
		return (TextView) getView(HOTEL_NAME_TEXT_VIEW_ID);
	}
}
