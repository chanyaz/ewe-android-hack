package com.expedia.bookings.test.tests.pageModels.common;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.res.Resources;
import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.test.utils.TestPreferences;
import com.expedia.bookings.widget.TextView;

public class ConfirmationScreen extends ScreenActions {

	private static int sDoneButtonID = R.id.menu_done;
	private static int sItineraryTextViewID = R.id.itinerary_text_view;
	private static int sEmailTextViewID = R.id.email_text_view;
	private static int sCallExpediaTextViewID = R.id.call_action_text_view;
	private static int sCallExpediaStringID = R.string.call_expedia_customer_support;
	private static int sShareViaEmailTextViewID = R.id.share_action_text_view;
	private static int sAddToCalendarTextViewID = R.id.calendar_action_text_view;

	public ConfirmationScreen(Instrumentation instrumentation, Activity activity, Resources res,
			TestPreferences preferences) {
		super(instrumentation, activity, res, preferences);
	}

	// Object access

	public View doneButton() {
		return getView(sDoneButtonID);
	}

	public TextView itineraryTextView() {
		return (TextView) getView(sItineraryTextViewID);
	}

	public TextView emailTextView() {
		return (TextView) getView(sEmailTextViewID);
	}

	public TextView callExpediaTextView() {
		return (TextView) getView(sCallExpediaTextViewID);
	}

	public String callExpediaCustomerSupport() {
		return getString(sCallExpediaStringID);
	}

	public TextView shareViaEmailTextView() {
		return (TextView) getView(sShareViaEmailTextViewID);
	}

	public TextView addToCalendarTextView() {
		return (TextView) getView(sAddToCalendarTextViewID);
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
