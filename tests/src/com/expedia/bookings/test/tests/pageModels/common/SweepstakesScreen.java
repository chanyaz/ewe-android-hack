package com.expedia.bookings.test.tests.pageModels.common;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.res.Resources;
import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.test.utils.TestPreferences;
import com.expedia.bookings.widget.TextView;

public class SweepstakesScreen extends ScreenActions {

	private static final int sNoThanksButtonID = R.id.no_thanks_button;
	private static final int sEnterButtonID = R.id.enter_button;
	private static final int sSweepstakesTitleTextViewID = R.id.enter_title_text_view;
	private static final int sSweepstakesTitleStringID = R.string.sweepstakes_enter;

	public SweepstakesScreen(Instrumentation instrumentation, Activity activity, Resources res,
			TestPreferences preferences) {
		super(instrumentation, activity, res, preferences);
	}

	// Object access

	public View noThanksButton() {
		return getView(sNoThanksButtonID);
	}

	public View enterButton() {
		return getView(sEnterButtonID);
	}

	public TextView titleTextView() {
		return (TextView) getView(sSweepstakesTitleTextViewID);
	}

	public String sweepstakesTitleString() {
		return mRes.getString(sSweepstakesTitleStringID);
	}

	// Object interaction

	public void clickNoThanksButton() {
		clickOnView(noThanksButton());
	}

	public void clickEnterButton() {
		clickOnView(enterButton());
	}

}
