package com.expedia.bookings.test.tests.pageModels.common;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.res.Resources;
import android.view.View;
import android.widget.EditText;

import com.expedia.bookings.R;
import com.expedia.bookings.test.utils.TestPreferences;

public class FindItineraryScreen extends ScreenActions {

	private static final int sHeaderTextViewID = R.id.itin_heading_textview;
	private static final int sFindItineraryButtonID = R.id.find_itinerary_button;
	private static final int sEmailAddressEditTextID = R.id.email_edit_text;
	private static final int sItinNumberEditTextID = R.id.itin_number_edit_text;

	public FindItineraryScreen(Instrumentation instrumentation, Activity activity, Resources res,
			TestPreferences preferences) {
		super(instrumentation, activity, res, preferences);
	}

	public String findItineraryHeaderText() {
		return mRes.getString(sHeaderTextViewID);
	}

	public View findItineraryButton() {
		return getView(sFindItineraryButtonID);
	}

	public EditText emailAddressEditText() {
		return (EditText) getView(sEmailAddressEditTextID);
	}

	public EditText itinNumberEditText() {
		return (EditText) getView(sItinNumberEditTextID);
	}

	public void clickFindItineraryButton() {
		clickOnView(findItineraryButton());
	}

}
