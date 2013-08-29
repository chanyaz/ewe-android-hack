package com.expedia.bookings.test.tests.pageModels;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.res.Resources;
import android.view.View;

import com.expedia.bookings.R;

public class FlightsSearchLoading extends ScreenActions {

	private static int sLoadingFlightsStringID = R.string.loading_flights;
	private static int sLoadingDialogueViewID = R.id.message_text_view;
	
	public FlightsSearchLoading(Instrumentation instrumentation, Activity activity, Resources res) {
		super(instrumentation, activity, res);
	}
	
	public String getLoadingFlightsString() {
		return mRes.getString(sLoadingFlightsStringID);
	}
	
	public View getLoadingDialogueView() {
		return getView(sLoadingDialogueViewID);
	}

}
