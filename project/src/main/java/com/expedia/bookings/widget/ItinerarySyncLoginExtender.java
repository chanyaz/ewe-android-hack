package com.expedia.bookings.widget;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.ViewGroup;

import com.expedia.bookings.data.trips.ItineraryManager;
import com.expedia.bookings.interfaces.LoginExtenderListener;
import com.expedia.bookings.utils.LoginExtender;

public class ItinerarySyncLoginExtender extends LoginExtender {

	public ItinerarySyncLoginExtender() {
		super(null);
	}

	public ItinerarySyncLoginExtender(Bundle state) {
		super(state);
	}

	@Override
	public void onLoginComplete(Context context, LoginExtenderListener listener, ViewGroup extenderContainer) {
		((Activity)context).finish();
		ItineraryManager.getInstance().startSync(false, false, true);
	}

	@Override
	public void cleanUp() {
	}

	@Override
	public void setExtenderStatus(String status) {
	}

	@Override
	public LoginExtenderType getExtenderType() {
		return LoginExtenderType.ITINERARY_SYNC;
	}

	@Override
	protected Bundle getStateBundle() {
		//No real state
		return new Bundle();
	}

	@Override
	protected void restoreStateFromBundle(Bundle state) {
		//No real state
	}

}
