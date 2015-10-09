package com.expedia.bookings.utils;

import android.content.Context;
import android.os.Bundle;
import android.view.ViewGroup;

import com.expedia.bookings.interfaces.LoginExtenderListener;
import com.expedia.bookings.widget.CheckoutLoginExtender;
import com.expedia.bookings.widget.ItineraryLoaderLoginExtender;
import com.expedia.bookings.widget.UserToTripAssocLoginExtender;

/**
 * This interface is for classes that want to do something after login. It was developed for itin so we can wait for itins on the login screen.
 * NOTE: This is not a view, and it does not have to contain a gui component (although if it is long running it should display an indicator at the very least)
 */
public abstract class LoginExtender {

	public static final String STATE_TYPE = "STATE_TYPE";
	public static final String STATE_DATA = "STATE_DATA";

	public enum LoginExtenderType {
		CHECKOUT,
		USER_TO_TRIP_ASSOC,
		ITINERARY_LOADER
	}

	/**
	 * Constructor that takes bundle generated by getStateBundle
	 * @param bundle
	 */
	public LoginExtender(Bundle bundle) {
		if (bundle != null) {
			restoreStateFromBundle(bundle);
		}
	}

	/**
	 * Builds the state bundle that is parseable by the static
	 * buildLoginExtenderFromState method (the bundle includes type)
	 * @return
	 */
	public Bundle buildStateBundle() {
		Bundle bundle = new Bundle();
		bundle.putString(STATE_TYPE, getExtenderType().name());
		bundle.putBundle(STATE_DATA, getStateBundle());
		return bundle;
	}

	/**
	 * This is a factory method that takes a bundle generated by buildStateBundle()
	 * @param state
	 * @return
	 */
	public static LoginExtender buildLoginExtenderFromState(Bundle state) {
		LoginExtenderType type = LoginExtenderType.valueOf(state.getString(STATE_TYPE));
		Bundle data = state.getBundle(STATE_DATA);
		switch (type) {
		case USER_TO_TRIP_ASSOC: {
			return new UserToTripAssocLoginExtender(data);
		}
		case ITINERARY_LOADER: {
			return new ItineraryLoaderLoginExtender(data);
		}
		case CHECKOUT:
			return new CheckoutLoginExtender(data);
		}
		return null;
	}

	/**
	 * The login has finished successfully, now is your time to do some stuff. If we have gui components add them to the extenderContainer
	 * @param fragment - the login fragment
	 * @param extenderContainer - a container set aside for views if the extender has a gui component
	 */
	public abstract void onLoginComplete(Context context, LoginExtenderListener listener, ViewGroup extenderContainer);

	/**
	 * We dont want to leak memory so we give you a chance to cleanup when the login fragment thinks cleaning is smart to do.
	 * At the time of this writting this gets called in onPause
	 */
	public abstract void cleanUp();

	/**
	 * Set status message from Extender
	 */
	public abstract void setExtenderStatus(String status);

	/**
	 * What type of login extender is this?
	 * @return
	 */
	public abstract LoginExtenderType getExtenderType();

	/**
	 * Generate a bundle that describes the state of the instance.
	 * 
	 * This is the bundle that will be handed to restoreStateFromBundle
	 * 
	 * @return
	 */
	protected abstract Bundle getStateBundle();

	/**
	 * Restore state from a bundle. This should restore state from the bundle
	 * generated by getStateBundle();
	 * 
	 * @param state
	 */
	protected abstract void restoreStateFromBundle(Bundle state);

}
