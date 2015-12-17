package com.expedia.bookings.widget;

import android.content.Context;
import android.os.Bundle;
import android.view.ViewGroup;

import com.expedia.bookings.interfaces.LoginExtenderListener;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.utils.LoginExtender;

public class CheckoutLoginExtender extends LoginExtender {

	public CheckoutLoginExtender() {
		super(new Bundle());
	}

	public CheckoutLoginExtender(Bundle state) {
		super(state);
	}

	@Override
	public void onLoginComplete(Context context, LoginExtenderListener listener, ViewGroup extenderContainer) {
		listener.loginExtenderWorkComplete(this);
		Events.post(new Events.LoggedInSuccessful());
	}

	@Override
	public void cleanUp() {

	}

	@Override
	public void setExtenderStatus(String status) {

	}

	@Override
	public LoginExtenderType getExtenderType() {
		return LoginExtenderType.CHECKOUT;
	}

	@Override
	protected Bundle getStateBundle() {
		return new Bundle();
	}

	@Override
	protected void restoreStateFromBundle(Bundle state) {

	}
}
