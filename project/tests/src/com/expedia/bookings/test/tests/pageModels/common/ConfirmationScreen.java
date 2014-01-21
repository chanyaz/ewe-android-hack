package com.expedia.bookings.test.tests.pageModels.common;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.res.Resources;
import android.view.View;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.test.utils.TestPreferences;

public class ConfirmationScreen extends ScreenActions {

	private static final int DONE_BUTTON_ID = R.id.menu_done;
	private static final int ITIN_TEXT_VIEW_ID = R.id.itinerary_text_view;
	private static final int EMAIL_TEXT_VIEW_ID = R.id.email_text_view;
	private static final int CALL_EXPEDIA_TEXT_VIEW_ID = R.id.call_action_text_view;
	private static final int CALL_CUSTOMER_SUPPORT_STRING = R.string.call_expedia_customer_support;
	private static final int SHARE_VIA_EMAIL_TEXT_VIEW = R.id.share_action_text_view;
	private static final int ADD_TO_CALENDAR_TEXT_VIEW_ID = R.id.calendar_action_text_view;
	private static final int BOOKING_COMPLETE_STRING_ID = R.string.booking_complete;

	public ConfirmationScreen(Instrumentation instrumentation, Activity activity, Resources res,
			TestPreferences preferences) {
		super(instrumentation, activity, res, preferences);
	}

	// Object access

	public View doneButton() {
		return getView(DONE_BUTTON_ID);
	}

	public TextView itineraryTextView() {
		return (TextView) getView(ITIN_TEXT_VIEW_ID);
	}

	public TextView emailTextView() {
		return (TextView) getView(EMAIL_TEXT_VIEW_ID);
	}

	public TextView callExpediaTextView() {
		return (TextView) getView(CALL_EXPEDIA_TEXT_VIEW_ID);
	}

	public String callExpediaCustomerSupport() {
		return getString(CALL_CUSTOMER_SUPPORT_STRING);
	}

	public TextView shareViaEmailTextView() {
		return (TextView) getView(SHARE_VIA_EMAIL_TEXT_VIEW);
	}

	public TextView addToCalendarTextView() {
		return (TextView) getView(ADD_TO_CALENDAR_TEXT_VIEW_ID);
	}

	public String bookingComplete() {
		return getString(BOOKING_COMPLETE_STRING_ID);
	}

	// Object interaction

	public void clickDoneButton() {
		clickOnView(doneButton());
	}

	public void clickCallExpedia() {
		clickOnView(callExpediaTextView());
	}

	public void clickShareViaEmail() {
		clickOnView(shareViaEmailTextView());
	}

	public void clickAddToCalendar() {
		clickOnView(addToCalendarTextView());
	}

}
