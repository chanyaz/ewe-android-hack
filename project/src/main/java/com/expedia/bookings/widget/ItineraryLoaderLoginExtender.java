package com.expedia.bookings.widget;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.ViewGroup;

import com.expedia.bookings.interfaces.LoginExtenderListener;
import com.expedia.bookings.utils.LoginExtender;

public class ItineraryLoaderLoginExtender extends LoginExtender {

	public ItineraryLoaderLoginExtender() {
		super(null);
	}

	public ItineraryLoaderLoginExtender(Bundle state) {
		super(state);
	}

	@Override
	public void onLoginComplete(Context context, LoginExtenderListener listener, ViewGroup extenderContainer) {
		((Activity)context).finish();
	}

	@Override
	public void cleanUp() {
	}

	@Override
	public void setExtenderStatus(String status) {
	}

	@Override
	public LoginExtenderType getExtenderType() {
		return LoginExtenderType.ITINERARY_LOADER;
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
