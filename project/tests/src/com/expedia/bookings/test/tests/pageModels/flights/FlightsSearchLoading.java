package com.expedia.bookings.test.tests.pageModels.flights;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.res.Resources;
import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.test.tests.pageModels.common.ScreenActions;
import com.expedia.bookings.test.utils.TestPreferences;

public class FlightsSearchLoading extends ScreenActions {

	private static final int sLoadingFlightsStringID = R.string.loading_flights;
	private static final int sLoadingDialogueViewID = R.id.message_text_view;

	public FlightsSearchLoading(Instrumentation instrumentation, Activity activity, Resources res,
			TestPreferences preferences) {
		super(instrumentation, activity, res, preferences);
	}

	public String getLoadingFlightsString() {
		return mRes.getString(sLoadingFlightsStringID);
	}

	public View getLoadingDialogueView() {
		return getView(sLoadingDialogueViewID);
	}

}
